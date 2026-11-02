import os
import argparse
import joblib

def main():
    parser = argparse.ArgumentParser(description="Predict category for a NeighborHero help request.")
    parser.add_argument("--text", type=str, required=True, help="Help request description text.")
    args = parser.parse_args()

    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    artifacts_dir = os.path.join(base_dir, "artifacts")

    model_path = os.path.join(artifacts_dir, "model.joblib")
    vectorizer_path = os.path.join(artifacts_dir, "vectorizer.joblib")

    if not os.path.exists(model_path) or not os.path.exists(vectorizer_path):
        print("Error: Trained model artifacts not found. Please run train.py first.")
        return

    classifier = joblib.load(model_path)
    vectorizer = joblib.load(vectorizer_path)

    X_input = [args.text]
    X_tfidf = vectorizer.transform(X_input)

    prediction = classifier.predict(X_tfidf)[0]
    probabilities = classifier.predict_proba(X_tfidf)[0]
    classes = classifier.classes_

    print("\n=== Prediction Result ===")
    print(f"Request Text: \"{args.text}\"")
    print(f"Predicted Category: {prediction}")
    print("\nClass Probabilities:")
    for cls, prob in sorted(zip(classes, probabilities), key=lambda x: x[1], reverse=True):
        print(f"  - {cls}: {prob * 100:.2f}%")

if __name__ == "__main__":
    main()
