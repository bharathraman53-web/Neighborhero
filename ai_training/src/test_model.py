import os
import joblib

def main():
    print("=== Testing Saved NLP Model Artifacts ===")

    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    artifacts_dir = os.path.join(base_dir, "artifacts")

    model_path = os.path.join(artifacts_dir, "model.joblib")
    vectorizer_path = os.path.join(artifacts_dir, "vectorizer.joblib")

    if not os.path.exists(model_path) or not os.path.exists(vectorizer_path):
        print("Error: Artifacts not found. Please run train.py first.")
        return

    classifier = joblib.load(model_path)
    vectorizer = joblib.load(vectorizer_path)

    sample_requests = [
        "Need someone to buy fresh apples and milk",
        "Looking for a calculus textbook for college",
        "Fixing a leaking bathroom sink faucet",
        "Need a ride to the doctor appointment",
        "Dog walking for golden retriever on Saturday",
        "Help sorting recycling bins in garage",
        "borrow chemistry study guide",
        "organic milk and bread run",
        "unclog kitchen sink drain",
        "lift to airport tomorrow 6am",
        "walk my puppy 30 mins",
        "rake leaves in backyard"
    ]

    X_tfidf = vectorizer.transform(sample_requests)
    predictions = classifier.predict(X_tfidf)
    probabilities = classifier.predict_proba(X_tfidf)

    for req, pred, prob in zip(sample_requests, predictions, probabilities):
        max_prob = max(prob) * 100
        print(f"\nRequest: \"{req}\"")
        print(f" -> Predicted Category: {pred} (Confidence: {max_prob:.2f}%)")

    print("\n=== Test Complete ===")

if __name__ == "__main__":
    main()
