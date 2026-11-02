import os
import json
import re
import numpy as np
import joblib

def tokenize(text, lowercase, token_pattern, stop_words):
    if lowercase:
        text = text.lower()
    tokens = re.findall(token_pattern, text)
    if stop_words:
        tokens = [t for t in tokens if t not in stop_words]
    return tokens

def generate_ngrams(tokens, ngram_range):
    min_n, max_n = ngram_range
    ngrams = []
    n = len(tokens)
    for k in range(min_n, max_n + 1):
        for i in range(n - k + 1):
            ngram = " ".join(tokens[i:i+k])
            ngrams.append(ngram)
    return ngrams

def json_transform(texts, json_data):
    vocab = json_data["vocabulary"]
    idf = np.array(json_data["idf"])
    token_pattern = json_data["token_pattern"]
    lowercase = json_data["lowercase"]
    ngram_range = tuple(json_data["ngram_range"])
    stop_words = set(json_data["stop_words"])
    norm = json_data["norm"]
    use_idf = json_data["use_idf"]

    n_samples = len(texts)
    n_features = len(vocab)
    X = np.zeros((n_samples, n_features))

    for idx, text in enumerate(texts):
        tokens = tokenize(text, lowercase, token_pattern, stop_words)
        ngrams = generate_ngrams(tokens, ngram_range)

        term_counts = {}
        for ng in ngrams:
            if ng in vocab:
                term_counts[vocab[ng]] = term_counts.get(vocab[ng], 0) + 1

        for feat_idx, count in term_counts.items():
            X[idx, feat_idx] = count

    if use_idf:
        X = X * idf

    if norm == 'l2':
        row_norms = np.linalg.norm(X, axis=1, keepdims=True)
        row_norms[row_norms == 0.0] = 1.0
        X = X / row_norms

    return X

def json_decision_function(texts, json_data):
    X = json_transform(texts, json_data)
    coef = np.array(json_data["coefficients"])
    intercept = np.array(json_data["intercept"])
    scores = np.dot(X, coef.T) + intercept
    return scores

def json_predict(texts, json_data):
    scores = json_decision_function(texts, json_data)
    classes = json_data["classes"]

    if len(classes) == 2:
        preds = np.where(scores >= 0, classes[1], classes[0])
        return preds
    else:
        pred_indices = np.argmax(scores, axis=1)
        return np.array([classes[i] for i in pred_indices])

def main():
    print("=== Comprehensive Reproducibility Test: Python Model vs JSON Inference ===")
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    artifacts_dir = os.path.join(base_dir, "artifacts")

    json_path = os.path.join(artifacts_dir, "model_weights.json")
    model_path = os.path.join(artifacts_dir, "model.joblib")
    vectorizer_path = os.path.join(artifacts_dir, "vectorizer.joblib")

    if not os.path.exists(json_path) or not os.path.exists(model_path) or not os.path.exists(vectorizer_path):
        print("Note: Artifacts not found (Python environment unavailable / un-trained).")
        print("Once training has been executed, this test will verify exact numerical parity.")
        return

    with open(json_path, 'r', encoding='utf-8') as f:
        json_data = json.load(f)

    classifier = joblib.load(model_path)
    vectorizer = joblib.load(vectorizer_path)

    test_sentences = [
        "Need help picking up milk and eggs from the grocery store",
        "Calculus 101 textbook needed for engineering college course",
        "Fix leaking kitchen faucet dripping constantly",
        "Need ride to hospital medical appointment on Thursday",
        "Dog walking assistance needed for Golden Retriever this weekend",
        "General yard cleanup and gathering fallen leaves",
        "Can someone grab bread and apples from supermarket?",
        "Borrowing organic chemistry reference guide for exam",
        "Repair broken fence latch in backyard",
        "Lift to train station with heavy luggage",
        "Cat feeding and litter cleaning service needed",
        "Shoveling snow from driveway after blizzard"
    ]

    X_test_tfidf = vectorizer.transform(test_sentences)
    orig_preds = classifier.predict(X_test_tfidf)
    json_preds = json_predict(test_sentences, json_data)

    orig_scores = classifier.decision_function(X_test_tfidf)
    json_scores = json_decision_function(test_sentences, json_data)

    print("\n--- 1. Prediction Category Parity ---")
    all_match = True
    for idx, (text, orig, js) in enumerate(zip(test_sentences, orig_preds, json_preds)):
        match = (orig == js)
        if not match:
            all_match = False
        print(f"[{idx+1:2d}] \"{text}\"")
        print(f"     scikit-learn: {orig:<12} | JSON: {js:<12} | Match: {match}")

    assert all_match, "Assertion Error: JSON-reconstructed predictions did not match original scikit-learn model predictions!"
    print("\n[PASS] All 12/12 predicted classes match identically.")

    print("\n--- 2. Numerical Score Parity (Tolerance = 1e-4) ---")
    abs_diffs = np.abs(orig_scores - json_scores)
    max_diff = np.max(abs_diffs)
    mean_diff = np.mean(abs_diffs)

    print(f"Max absolute score difference:  {max_diff:.8e}")
    print(f"Mean absolute score difference: {mean_diff:.8e}")

    TOLERANCE = 1e-4
    assert max_diff < TOLERANCE, f"Numerical discrepancy exceeds tolerance {TOLERANCE}: {max_diff}"
    print(f"[PASS] Maximum score difference ({max_diff:.2e}) is well within tolerance ({TOLERANCE:.2e}).")

    print("\n=== SUCCESS: Full Category and Numerical Score Parity Verified! ===")

if __name__ == "__main__":
    main()
