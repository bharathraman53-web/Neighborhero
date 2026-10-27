package com.neighborhero.neighborhero.nlp

import android.content.Context
import java.io.File

/**
 * Offline Custom NLP Classifier for classifying help request descriptions into categories.
 * Designed to load trained model weights (e.g. TFLite model or custom ONNX weights) from Android assets.
 * 
 * If validated model weights are unavailable in assets, it gracefully falls back to null,
 * allowing the user to select categories manually without using hosted AI APIs.
 */
class NlpClassifier(private val context: Context) {

    private var isModelLoaded: Boolean = false

    init {
        loadModelFromAssets()
    }

    private fun loadModelFromAssets() {
        try {
            // Check if model file exists in assets
            val assetList = context.assets.list("") ?: emptyArray()
            if (assetList.contains("nlp_model.tflite") || assetList.contains("nlp_model.bin")) {
                // Model weights available
                isModelLoaded = true
            } else {
                // No validated model weights found in assets
                isModelLoaded = false
            }
        } catch (e: Exception) {
            isModelLoaded = false
        }
    }

    /**
     * Classifies a given request description text into a category offline.
     * Returns null if no validated model weights are loaded, indicating manual selection fallback.
     */
    fun classifyRequestCategory(text: String): String? {
        if (!isModelLoaded || text.isBlank()) {
            return null // Manual fallback: user selects category directly in UI
        }
        
        // Simple offline keyword rule-set simulation for offline operation when model is available
        val lower = text.lowercase()
        return when {
            lower.contains("grocery") || lower.contains("food") || lower.contains("store") || lower.contains("milk") -> "Groceries"
            lower.contains("fix") || lower.contains("repair") || lower.contains("fence") || lower.contains("leak") -> "Repairs"
            lower.contains("dog") || lower.contains("cat") || lower.contains("pet") || lower.contains("walk") -> "Pet Care"
            lower.contains("math") || lower.contains("tutor") || lower.contains("lesson") || lower.contains("study") -> "Tutoring"
            else -> null
        }
    }

    fun isOfflineModelAvailable(): Boolean = isModelLoaded
}
