import os
import json
import pandas as pd
import numpy as np
import joblib
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, classification_report, confusion_matrix

def export_model_to_json(vectorizer, classifier, output_path):
    stop_words_list = sorted(list(vectorizer.get_stop_words())) if vectorizer.get_stop_words() is not None else []
    export_data = {
        "version": "1.1",
        "classes": classifier.classes_.tolist(),
        "vocabulary": {k: int(v) for k, v in vectorizer.vocabulary_.items()},
        "idf": vectorizer.idf_.tolist(),
        "token_pattern": vectorizer.token_pattern,
        "lowercase": vectorizer.lowercase,
        "ngram_range": list(vectorizer.ngram_range),
        "stop_words": stop_words_list,
        "norm": vectorizer.norm,
        "use_idf": vectorizer.use_idf,
        "smooth_idf": vectorizer.smooth_idf,
        "sublinear_tf": vectorizer.sublinear_tf,
        "coefficients": classifier.coef_.tolist(),
        "intercept": classifier.intercept_.tolist()
    }
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(export_data, f, ensure_ascii=False, indent=2)
    print(f"Comprehensive model parameters exported to JSON at {output_path}")

def main():
    print("=== NeighborHero NLP Model Training & Comprehensive JSON Export ===")

    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    dataset_path = os.path.join(base_dir, "dataset", "request_dataset.csv")
    artifacts_dir = os.path.join(base_dir, "artifacts")

    os.makedirs(artifacts_dir, exist_ok=True)

    print(f"Loading dataset from {dataset_path}...")
    df = pd.read_csv(dataset_path)
    print(f"Dataset shape: {df.shape}")
    print("Category distribution:")
    print(df['category'].value_counts())

    X = df['text'].values
    y = df['category'].values

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    print(f"Training samples: {len(X_train)}, Test samples: {len(X_test)}")

    print("Fitting TF-IDF vectorizer on training data...")
    vectorizer = TfidfVectorizer(max_features=2500, ngram_range=(1, 2), stop_words='english', norm='l2')
    X_train_tfidf = vectorizer.fit_transform(X_train)
    X_test_tfidf = vectorizer.transform(X_test)

    print("Training Logistic Regression classifier...")
    classifier = LogisticRegression(random_state=42, max_iter=1000, C=2.0)
    classifier.fit(X_train_tfidf, y_train)

    print("\n--- Evaluation Results on Test Set ---")
    y_pred = classifier.predict(X_test_tfidf)

    acc = accuracy_score(y_test, y_pred)
    macro_prec = precision_score(y_test, y_pred, average='macro', zero_division=0)
    macro_rec = recall_score(y_test, y_pred, average='macro', zero_division=0)
    macro_f1 = f1_score(y_test, y_pred, average='macro', zero_division=0)
    weighted_f1 = f1_score(y_test, y_pred, average='weighted', zero_division=0)

    print(f"Test Accuracy: {acc:.4f} ({acc*100:.2f}%)")
    print(f"Macro Precision: {macro_prec:.4f}")
    print(f"Macro Recall: {macro_rec:.4f}")
    print(f"Macro F1-Score: {macro_f1:.4f}")
    print(f"Weighted F1-Score: {weighted_f1:.4f}\n")

    print("Classification Report:")
    print(classification_report(y_test, y_pred, zero_division=0))

    conf_mat = confusion_matrix(y_test, y_pred, labels=classifier.classes_)
    print("Confusion Matrix:")
    print(f"Classes: {classifier.classes_.tolist()}")
    print(conf_mat)

    # Persist evaluation metrics to JSON
    report_dict = classification_report(y_test, y_pred, output_dict=True, zero_division=0)
    eval_summary = {
        "dataset_size": len(df),
        "train_samples": len(X_train),
        "test_samples": len(X_test),
        "classes": classifier.classes_.tolist(),
        "accuracy": float(acc),
        "macro_precision": float(macro_prec),
        "macro_recall": float(macro_rec),
        "macro_f1": float(macro_f1),
        "weighted_f1": float(weighted_f1),
        "per_category_metrics": {cls: report_dict[cls] for cls in classifier.classes_ if cls in report_dict},
        "confusion_matrix": conf_mat.tolist()
    }
    with open(os.path.join(artifacts_dir, "evaluation_report.json"), "w", encoding="utf-8") as f:
        json.dump(eval_summary, f, indent=2)

    model_path = os.path.join(artifacts_dir, "model.joblib")
    vectorizer_path = os.path.join(artifacts_dir, "vectorizer.joblib")
    json_export_path = os.path.join(artifacts_dir, "model_weights.json")

    joblib.dump(classifier, model_path)
    joblib.dump(vectorizer, vectorizer_path)
    export_model_to_json(vectorizer, classifier, json_export_path)

    print(f"\nModel saved to {model_path}")
    print(f"Vectorizer saved to {vectorizer_path}")
    print(f"JSON Export saved to {json_export_path}")
    print("=== Training & Export Complete ===")

if __name__ == "__main__":
    main()
