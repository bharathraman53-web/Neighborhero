# NeighborHero Custom NLP Model Training & JSON Export

This folder contains the training pipeline, dataset, inference scripts, and JSON export mechanism for pure Kotlin inference in NeighborHero.

---

## 📁 File Structure

- **`dataset/request_dataset.csv`**: Labelled synthetic training examples across 6 categories (**Groceries**, **Books**, **Repairs**, **Transport**, **Pet Help**, **Other**).
- **`src/train.py`**: Training script that trains the TF-IDF vectorizer and Logistic Regression classifier, evaluates performance, saves `.joblib` artifacts, and exports model parameters to `artifacts/model_weights.json`.
- **`src/predict.py`**: Inference script for classifying new request text using `.joblib` artifacts.
- **`src/test_export.py`**: Reproducibility verification test comparing the Python model against exported JSON parameters.
- **`artifacts/`**: Stores serialized `model.joblib`, `vectorizer.joblib`, and `model_weights.json`.
- **`requirements.txt`**: Python package dependencies.

---

## 📋 Exported JSON Schema (`model_weights.json`)

The exported JSON file contains all necessary parameters to execute pure Kotlin inference without scikit-learn:
- `version`: Schema version string (e.g. `"1.0"`).
- `classes`: Array of target category strings.
- `vocabulary`: Dictionary mapping feature n-gram tokens to their feature indices.
- `idf`: Array of Inverse Document Frequency (IDF) weights for each vocabulary feature.
- `ngram_range`: Array specifying n-gram bounds (e.g., `[1, 2]`).
- `coefficients`: 2D array of Logistic Regression weight coefficients `[n_classes, n_features]`.
- `intercept`: Array of Logistic Regression intercept values `[n_classes]`.

---

## ⚠️ Note on Environment Execution
Since Python is not installed on the current college PC, these scripts have been prepared for training and verification when executed in a Python-enabled environment. No Android app files have been modified.
