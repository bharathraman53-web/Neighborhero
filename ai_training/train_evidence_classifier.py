"""
NeighborHero Evidence Classifier Training and TFLite Export
Phase 2: MobileNetV2 Transfer Learning for Image Evidence Domain Relevance
Includes Laptop / Computer / Electronics Repair Specialization & Parity Verification

Classes:
  0: groceries
  1: pet_help
  2: repairs
  3: unrelated
"""

import os
import sys
import csv
import json
import numpy as np
from PIL import Image

import tensorflow as tf
import keras
from keras import layers

# Configure reproducible seeds
np.random.seed(42)
tf.random.set_seed(42)

CLASS_NAMES = ["groceries", "pet_help", "repairs", "unrelated"]
CLASS_TO_IDX = {name: idx for idx, name in enumerate(CLASS_NAMES)}

def load_split_data(dataset_dir, manifest_rows, split_name):
    """
    Load images and labels for a specific split defined in manifest.csv.
    Images are loaded as RGB float32 arrays in range [0, 255] with shape (224, 224, 3).
    """
    split_rows = [r for r in manifest_rows if r['split'] == split_name]
    images = []
    labels = []
    metadata = []

    print(f"Loading '{split_name}' split ({len(split_rows)} items)...")
    for r in split_rows:
        img_path = os.path.join(dataset_dir, r['filename'])
        if not os.path.exists(img_path):
            raise FileNotFoundError(f"Image not found: {img_path}")
        
        with Image.open(img_path) as img:
            rgb = img.convert('RGB')
            # Resize with LANCZOS to 224x224
            resized = rgb.resize((224, 224), Image.Resampling.LANCZOS)
            arr = np.array(resized, dtype=np.float32)
        
        images.append(arr)
        labels.append(CLASS_TO_IDX[r['category']])
        
        # Tag laptop repair images
        is_laptop = False
        if r['category'] == 'repairs':
            try:
                num = int(r['filename'].split('_')[-1].split('.')[0])
                if num >= 42:
                    is_laptop = True
            except Exception:
                pass

        metadata.append({
            "filename": r['filename'],
            "category": r['category'],
            "split": split_name,
            "sha256": r['sha256'],
            "is_laptop_repair": is_laptop
        })

    images_np = np.stack(images, axis=0)
    labels_np = np.array(labels, dtype=np.int32)
    return images_np, labels_np, metadata

def build_model(weights_path=None):
    """
    Build MobileNetV2 transfer learning model:
    - Input: (224, 224, 3) float32 in [0, 255]
    - Rescaling layer: maps [0, 255] to [-1, 1] via (x / 127.5) - 1.0
    - Pretrained MobileNetV2 backbone (ImageNet)
    - GlobalAveragePooling2D
    - Dense(128, activation='relu')
    - Dropout(0.3)
    - Dense(4, activation='softmax')
    """
    inputs = keras.Input(shape=(224, 224, 3), name="image_input")
    
    # Preprocessing layer: maps [0, 255] to [-1, 1]
    rescaled = layers.Rescaling(scale=1.0 / 127.5, offset=-1.0, name="mobilenet_v2_preprocess")(inputs)
    
    # Pretrained backbone
    base_model = keras.applications.MobileNetV2(
        input_shape=(224, 224, 3),
        include_top=False,
        weights=None
    )
    if weights_path and os.path.exists(weights_path):
        base_model.load_weights(weights_path)
        print(f"Loaded pretrained ImageNet weights from: {weights_path}")
    else:
        raise FileNotFoundError(f"Pretrained weights not found: {weights_path}")
        
    base_model.trainable = False
    
    features = base_model(rescaled, training=False)
    pooled = layers.GlobalAveragePooling2D(name="global_average_pooling")(features)
    dense1 = layers.Dense(128, activation="relu", name="dense_128")(pooled)
    dropped = layers.Dropout(0.3, name="dropout_0_3")(dense1)
    outputs = layers.Dense(len(CLASS_NAMES), activation="softmax", name="evidence_classifier_output")(dropped)
    
    model = keras.Model(inputs=inputs, outputs=outputs, name="neighborhero_evidence_classifier")
    return model, base_model

def calculate_metrics(y_true, y_pred, class_names):
    """
    Calculate precision, recall, F1, macro F1, and confusion matrix using NumPy.
    """
    n_classes = len(class_names)
    cm = np.zeros((n_classes, n_classes), dtype=int)
    for t, p in zip(y_true, y_pred):
        cm[t, p] += 1
    
    per_class = {}
    f1_list = []
    
    for i, cname in enumerate(class_names):
        tp = cm[i, i]
        fp = np.sum(cm[:, i]) - tp
        fn = np.sum(cm[i, :]) - tp
        support = np.sum(cm[i, :])
        
        prec = float(tp / (tp + fp)) if (tp + fp) > 0 else 0.0
        rec = float(tp / (tp + fn)) if (tp + fn) > 0 else 0.0
        f1 = float(2 * (prec * rec) / (prec + rec)) if (prec + rec) > 0 else 0.0
        
        f1_list.append(f1)
        per_class[cname] = {
            "precision": round(prec, 4),
            "recall": round(rec, 4),
            "f1_score": round(f1, 4),
            "support": int(support),
            "true_positives": int(tp),
            "false_positives": int(fp),
            "false_negatives": int(fn)
        }
    
    macro_f1 = float(np.mean(f1_list))
    total_acc = float(np.sum(np.diag(cm)) / np.sum(cm)) if np.sum(cm) > 0 else 0.0
    
    return {
        "accuracy": round(total_acc, 4),
        "macro_f1": round(macro_f1, 4),
        "per_class": per_class,
        "confusion_matrix": cm.tolist()
    }

def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    dataset_dir = os.path.join(base_dir, "evidence_dataset")
    manifest_path = os.path.join(dataset_dir, "manifest.csv")
    artifacts_dir = os.path.join(base_dir, "artifacts")
    os.makedirs(artifacts_dir, exist_ok=True)
    
    labels_path = os.path.join(artifacts_dir, "labels.txt")
    tflite_f32_path = os.path.join(artifacts_dir, "evidence_classifier.tflite")
    tflite_f16_path = os.path.join(artifacts_dir, "evidence_classifier_float16.tflite")
    report_path = os.path.join(artifacts_dir, "vision_evaluation_report.json")
    checkpoint_path = os.path.join(artifacts_dir, "best_evidence_model.keras")
    assets_dir = os.path.join(base_dir, "..", "app", "src", "main", "assets")
    
    print("=" * 60)
    print("NeighborHero Image AI — Evidence Classifier Retraining")
    print(f"TensorFlow Version: {tf.__version__}")
    print(f"Keras Version:      {keras.__version__}")
    print(f"Classes ({len(CLASS_NAMES)}):    {CLASS_NAMES}")
    print("=" * 60)
    
    # 1. Write labels.txt
    with open(labels_path, "w", encoding="utf-8") as f:
        for cname in CLASS_NAMES:
            f.write(f"{cname}\n")
    print(f"Saved labels: {labels_path}")
    
    # 2. Load manifest
    with open(manifest_path, "r", encoding="utf-8") as f:
        manifest_rows = list(csv.DictReader(f))
    print(f"Read {len(manifest_rows)} rows from manifest.")
    
    # 3. Load splits
    X_train, y_train, meta_train = load_split_data(dataset_dir, manifest_rows, "train")
    X_val, y_val, meta_val = load_split_data(dataset_dir, manifest_rows, "validation")
    X_test, y_test, meta_test = load_split_data(dataset_dir, manifest_rows, "test")
    
    print(f"\nSplit Shapes:")
    print(f"  Train:      X={X_train.shape}, y={y_train.shape} (laptops: {sum(1 for m in meta_train if m['is_laptop_repair'])})")
    print(f"  Validation: X={X_val.shape}, y={y_val.shape} (laptops: {sum(1 for m in meta_val if m['is_laptop_repair'])})")
    print(f"  Test:       X={X_test.shape}, y={y_test.shape} (laptops: {sum(1 for m in meta_test if m['is_laptop_repair'])})")
    
    # 4. Build and compile model
    weights_path = os.path.join(base_dir, "mobilenet_v2_weights.h5")
    model, base_model = build_model(weights_path=weights_path)
    
    # Data augmentation for training dataset
    data_augmentation = keras.Sequential([
        layers.RandomFlip("horizontal"),
        layers.RandomRotation(0.08),
        layers.RandomZoom(0.08),
        layers.RandomContrast(0.08)
    ], name="data_augmentation")

    train_ds = tf.data.Dataset.from_tensor_slices((X_train, y_train))
    train_ds = train_ds.shuffle(buffer_size=200, seed=42)
    train_ds = train_ds.map(lambda x, y: (data_augmentation(x, training=True), y), num_parallel_calls=tf.data.AUTOTUNE)
    train_ds = train_ds.batch(16).prefetch(tf.data.AUTOTUNE)
    
    val_ds = tf.data.Dataset.from_tensor_slices((X_val, y_val)).batch(16).prefetch(tf.data.AUTOTUNE)

    # Phase 1: Train Head (frozen backbone)
    print("\n--- Phase 1: Training Classification Head (Frozen Backbone) ---")
    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=1e-3),
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"]
    )
    
    history_phase1 = model.fit(
        train_ds,
        validation_data=val_ds,
        epochs=12,
        verbose=1
    )
    
    # Phase 2: Fine-Tuning (unfreeze top 30 layers)
    print("\n--- Phase 2: Fine-Tuning MobileNetV2 Top Layers ---")
    base_model.trainable = True
    for layer in base_model.layers[:-30]:
        layer.trainable = False
        
    print(f"Trainable layers in model: {len(model.trainable_weights)}")
    
    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=1e-4),
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"]
    )
    
    callbacks = [
        keras.callbacks.EarlyStopping(
            monitor="val_accuracy",
            patience=8,
            restore_best_weights=True,
            verbose=1
        ),
        keras.callbacks.ModelCheckpoint(
            filepath=checkpoint_path,
            monitor="val_accuracy",
            save_best_only=True,
            verbose=1
        )
    ]
    
    history_phase2 = model.fit(
        train_ds,
        validation_data=val_ds,
        epochs=18,
        callbacks=callbacks,
        verbose=1
    )
    
    # 7. Evaluate on untouched Test Set
    print("\n" + "=" * 60)
    print("Evaluating Best Model on Untouched Test Dataset...")
    print("=" * 60)
    
    test_probs = model.predict(X_test, verbose=0)
    test_preds = np.argmax(test_probs, axis=1)
    
    test_metrics = calculate_metrics(y_test, test_preds, CLASS_NAMES)
    print(f"\nTest Accuracy: {test_metrics['accuracy'] * 100:.2f}%")
    print(f"Test Macro F1: {test_metrics['macro_f1']:.4f}")
    print("\nPer-Class Classification Metrics:")
    for cname, m in test_metrics['per_class'].items():
        print(f"  {cname:<12} -> Precision: {m['precision']:.4f} | Recall: {m['recall']:.4f} | F1: {m['f1_score']:.4f} (Support: {m['support']})")
    
    print("\nConfusion Matrix (Rows: Ground Truth, Columns: Predicted):")
    header = "       " + " ".join([f"{c[:5]:>7}" for c in CLASS_NAMES])
    print(header)
    for i, row in enumerate(test_metrics['confusion_matrix']):
        row_str = " ".join([f"{val:>7}" for val in row])
        print(f"{CLASS_NAMES[i][:6]:<6} {row_str}")
    
    # Specific test on Laptop / Electronics Repair Images
    print("\n" + "=" * 60)
    print("SPECIFIC EVALUATION ON LAPTOP / TECH REPAIR TEST IMAGES:")
    print("=" * 60)
    laptop_indices = [i for i, m in enumerate(meta_test) if m['is_laptop_repair']]
    laptop_correct = 0
    laptop_results = []
    
    for idx in laptop_indices:
        fname = meta_test[idx]['filename']
        pred_label = CLASS_NAMES[test_preds[idx]]
        conf = float(test_probs[idx, test_preds[idx]])
        all_p = {CLASS_NAMES[k]: round(float(test_probs[idx, k]), 4) for k in range(len(CLASS_NAMES))}
        is_ok = (pred_label == "repairs")
        if is_ok:
            laptop_correct += 1
        status = "MATCHED (CORRECT)" if is_ok else "MISMATCH"
        print(f"  [{status}] {fname:<25} -> Pred: {pred_label:<10} ({conf*100:.1f}%) | Scores: {all_p}")
        laptop_results.append({
            "filename": fname,
            "predicted_category": pred_label,
            "is_correct_repair": is_ok,
            "confidence": conf,
            "scores": all_p
        })
    
    laptop_acc = (laptop_correct / len(laptop_indices)) if laptop_indices else 0.0
    print(f"\nLaptop Repair Accuracy: {laptop_correct}/{len(laptop_indices)} ({laptop_acc*100:.1f}%)")
    
    # Detail all individual test predictions
    test_image_results = []
    for i in range(len(y_test)):
        fname = meta_test[i]['filename']
        true_name = CLASS_NAMES[y_test[i]]
        pred_name = CLASS_NAMES[test_preds[i]]
        conf = float(test_probs[i, test_preds[i]])
        correct = bool(true_name == pred_name)
        probs_dict = {CLASS_NAMES[k]: round(float(test_probs[i, k]), 4) for k in range(len(CLASS_NAMES))}
        test_image_results.append({
            "filename": fname,
            "true_category": true_name,
            "predicted_category": pred_name,
            "confidence": round(conf, 4),
            "is_correct": correct,
            "probabilities": probs_dict
        })
    
    # 8. Export Float32 TFLite Model
    print("\n" + "=" * 60)
    print("Exporting Float32 TensorFlow Lite Model...")
    print("=" * 60)
    converter_f32 = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_f32_bytes = converter_f32.convert()
    with open(tflite_f32_path, "wb") as f:
        f.write(tflite_f32_bytes)
    f32_size_mb = len(tflite_f32_bytes) / (1024 * 1024)
    print(f"Exported Float32 TFLite: {tflite_f32_path} ({f32_size_mb:.2f} MB)")
    
    # 9. Attempt Float16 Quantization Export
    f16_exported = False
    f16_size_mb = 0.0
    try:
        print("\nAttempting Float16 Quantization Export...")
        converter_f16 = tf.lite.TFLiteConverter.from_keras_model(model)
        converter_f16.optimizations = [tf.lite.Optimize.DEFAULT]
        converter_f16.target_spec.supported_types = [tf.float16]
        tflite_f16_bytes = converter_f16.convert()
        with open(tflite_f16_path, "wb") as f:
            f.write(tflite_f16_bytes)
        f16_size_mb = len(tflite_f16_bytes) / (1024 * 1024)
        print(f"Exported Float16 TFLite: {tflite_f16_path} ({f16_size_mb:.2f} MB)")
        f16_exported = True
    except Exception as e:
        print(f"Float16 Quantization conversion warning/failed: {e}")
    
    # 10. Copy exported model and labels to Android Assets
    if os.path.exists(assets_dir):
        dest_tflite = os.path.join(assets_dir, "evidence_classifier.tflite")
        dest_labels = os.path.join(assets_dir, "labels.txt")
        with open(tflite_f32_path, "rb") as sf, open(dest_tflite, "wb") as df:
            df.write(sf.read())
        with open(labels_path, "r", encoding="utf-8") as sf, open(dest_labels, "w", encoding="utf-8") as df:
            df.write(sf.read())
        print(f"\nSynchronized model and labels to Android assets:\n  -> {dest_tflite}\n  -> {dest_labels}")
    
    # 11. TFLite Verification & Parity Comparison on Test Set
    print("\n" + "=" * 60)
    print("Verifying Keras vs TFLite Parity on Test Dataset...")
    print("=" * 60)
    
    interpreter = tf.lite.Interpreter(model_path=tflite_f32_path)
    interpreter.allocate_tensors()
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
    
    input_index = input_details[0]['index']
    output_index = output_details[0]['index']
    
    parity_mismatches = 0
    max_prob_diff = 0.0
    tflite_predictions = []
    
    for i in range(len(X_test)):
        sample = np.expand_dims(X_test[i], axis=0) # shape (1, 224, 224, 3)
        interpreter.set_tensor(input_index, sample)
        interpreter.invoke()
        tflite_out = interpreter.get_tensor(output_index)[0]
        
        keras_out = test_probs[i]
        diff = float(np.max(np.abs(keras_out - tflite_out)))
        if diff > max_prob_diff:
            max_prob_diff = diff
        
        tflite_pred_idx = int(np.argmax(tflite_out))
        keras_pred_idx = int(np.argmax(keras_out))
        
        if tflite_pred_idx != keras_pred_idx:
            parity_mismatches += 1
            print(f"  Parity Mismatch at {meta_test[i]['filename']}: Keras={CLASS_NAMES[keras_pred_idx]} vs TFLite={CLASS_NAMES[tflite_pred_idx]}")
        
        tflite_predictions.append({
            "filename": meta_test[i]['filename'],
            "keras_pred": CLASS_NAMES[keras_pred_idx],
            "tflite_pred": CLASS_NAMES[tflite_pred_idx],
            "max_abs_diff": round(diff, 6),
            "tflite_probs": {CLASS_NAMES[k]: round(float(tflite_out[k]), 4) for k in range(len(CLASS_NAMES))}
        })
    
    print(f"Parity Check Result:")
    print(f"  Total Test Images Evaluated: {len(X_test)}")
    print(f"  Prediction Category Mismatches: {parity_mismatches} (0 is perfect)")
    print(f"  Max Absolute Probability Difference: {max_prob_diff:.6e}")
    print(f"  Inference Success Rate: 100%")
    
    # 12. Compile and save JSON report
    h1 = history_phase1.history
    h2 = history_phase2.history
    
    combined_loss = [round(float(v), 4) for v in h1.get("loss", []) + h2.get("loss", [])]
    combined_val_loss = [round(float(v), 4) for v in h1.get("val_loss", []) + h2.get("val_loss", [])]
    combined_acc = [round(float(v), 4) for v in h1.get("accuracy", []) + h2.get("accuracy", [])]
    combined_val_acc = [round(float(v), 4) for v in h1.get("val_accuracy", []) + h2.get("val_accuracy", [])]
    
    final_report = {
        "task": "NeighborHero Evidence Classifier Training and Export",
        "phase": "improved_vision_retraining",
        "classes": CLASS_NAMES,
        "splits": {
            "train_samples": int(len(X_train)),
            "validation_samples": int(len(X_val)),
            "test_samples": int(len(X_test)),
            "total_samples": int(len(manifest_rows))
        },
        "model_architecture": {
            "backbone": "MobileNetV2 (ImageNet pretrained, top 30 layers fine-tuned)",
            "weights": "imagenet",
            "input_shape": [224, 224, 3],
            "preprocessing": "[-1, 1] via Rescaling(1/127.5, -1.0)",
            "head": "GAP -> Dense(128, relu) -> Dropout(0.3) -> Dense(4, softmax)",
            "optimizer": "Phase 1: Adam(1e-3), Phase 2: Adam(1e-4)",
            "loss": "sparse_categorical_crossentropy"
        },
        "training_history": {
            "loss": combined_loss,
            "val_loss": combined_val_loss,
            "accuracy": combined_acc,
            "val_accuracy": combined_val_acc
        },
        "test_metrics": test_metrics,
        "laptop_repair_test_metrics": {
            "total_laptop_test_images": len(laptop_indices),
            "correct_repairs_count": laptop_correct,
            "accuracy": round(laptop_acc, 4),
            "details": laptop_results
        },
        "artifacts": {
            "labels_file": labels_path,
            "keras_checkpoint": checkpoint_path,
            "tflite_float32": {
                "path": tflite_f32_path,
                "size_mb": round(f32_size_mb, 2)
            },
            "tflite_float16": {
                "exported": f16_exported,
                "path": tflite_f16_path if f16_exported else None,
                "size_mb": round(f16_size_mb, 2) if f16_exported else None
            }
        },
        "parity_verification": {
            "samples_tested": len(X_test),
            "category_mismatches": parity_mismatches,
            "max_probability_difference": round(max_prob_diff, 6),
            "individual_comparisons": tflite_predictions
        },
        "test_predictions": test_image_results
    }
    
    with open(report_path, "w", encoding="utf-8") as f:
        json.dump(final_report, f, indent=2)
    print(f"\nSaved complete report: {report_path}")
    print("=" * 60)
    print("Retraining and TFLite Export Successfully Completed!")
    print("=" * 60)

if __name__ == "__main__":
    main()
