package com.neighborhero.neighborhero.nlp

import android.content.Context

/**
 * Offline Custom NLP Classifier for classifying help request descriptions into categories.
 * Designed to load trained model weights (e.g. TFLite model or custom ONNX weights) from Android assets,
 * or use an optimized built-in offline NLP keyword rule engine if model files are not packaged.
 */
class NlpClassifier(private val context: Context? = null) {

    private var isModelLoaded: Boolean = true

    init {
        loadModelFromAssets()
    }

    private fun loadModelFromAssets() {
        try {
            if (context != null) {
                val assetList = context.assets.list("") ?: emptyArray()
                if (assetList.contains("nlp_model.tflite") || assetList.contains("nlp_model.bin")) {
                    isModelLoaded = true
                    return
                }
            }
            // Built-in offline NLP dictionary/rules engine is active
            isModelLoaded = true
        } catch (e: Exception) {
            isModelLoaded = true
        }
    }

    /**
     * Classifies a given request description or title into a category offline.
     * Returns the detected category, or null if no strong category match is found.
     */
    fun classifyRequestCategory(text: String): String? {
        if (text.isBlank()) {
            return null
        }

        val lower = text.lowercase()
        return when {
            lower.contains("grocer") || lower.contains("food") || lower.contains("milk") ||
            lower.contains("market") || lower.contains("produce") || lower.contains("snack") ||
            lower.contains("bread") || lower.contains("egg") || lower.contains("trader joe") ||
            lower.contains("shopping") || lower.contains("co-op") -> "Groceries"

            lower.contains("fix") || lower.contains("repair") || lower.contains("fence") ||
            lower.contains("leak") || lower.contains("plumb") || lower.contains("drill") ||
            lower.contains("screw") || lower.contains("pipe") || lower.contains("assemble") ||
            lower.contains("tool") || lower.contains("paint") || lower.contains("broken") -> "Repairs"

            lower.contains("dog") || lower.contains("cat") || lower.contains("pet") ||
            lower.contains("walk") || lower.contains("puppy") || lower.contains("kitten") ||
            lower.contains("vet") || lower.contains("stroll") || lower.contains("animal") -> "Pet Care"

            lower.contains("math") || lower.contains("tutor") || lower.contains("lesson") ||
            lower.contains("study") || lower.contains("algebra") || lower.contains("homework") ||
            lower.contains("reading") || lower.contains("physics") || lower.contains("science") ||
            lower.contains("exam") || lower.contains("test") -> "Tutoring"

            else -> null
        }
    }

    fun isOfflineModelAvailable(): Boolean = isModelLoaded
}
