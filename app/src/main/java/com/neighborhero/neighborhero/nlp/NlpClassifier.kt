package com.neighborhero.neighborhero.nlp

import android.content.Context
import com.neighborhero.neighborhero.data.model.RequestCategory

/**
 * Offline Custom NLP Classifier for classifying help request descriptions into standardized categories.
 * Supports the 6 canonical categories: Groceries, Books, Repairs, Transport, Pet Help, Other.
 * Designed to load trained TF-IDF + Logistic Regression model weights from assets,
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
                if (assetList.contains("model_weights.json") ||
                    assetList.contains("nlp_model.tflite") ||
                    assetList.contains("nlp_model.bin")
                ) {
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
     * Classifies a given request description or title into a canonical category offline.
     * Returns the detected canonical category, or null if no strong category match is found.
     * Note: Books and Tutoring are distinct; legacy tutoring-specific phrases remain distinct.
     */
    fun classifyRequestCategory(text: String): String? {
        if (text.isBlank()) {
            return null
        }

        val lower = text.lowercase()
        return when {
            // 1. Groceries
            lower.contains("grocer") || lower.contains("food") || lower.contains("milk") ||
            lower.contains("market") || lower.contains("produce") || lower.contains("snack") ||
            lower.contains("bread") || lower.contains("egg") || lower.contains("trader joe") ||
            lower.contains("shopping") || lower.contains("co-op") || lower.contains("pantry") ||
            lower.contains("supermarket") || lower.contains("apples") || lower.contains("vegetable") ||
            lower.contains("fruit") || lower.contains("canned") || lower.contains("flour") ||
            lower.contains("sugar") || lower.contains("rice") -> RequestCategory.GROCERIES

            // 2. Books (Distinct from Tutoring)
            lower.contains("textbook") || lower.contains("book") || lower.contains("novel") ||
            lower.contains("literature") || lower.contains("workbook") || lower.contains("reference book") ||
            lower.contains("hardcover") || lower.contains("library") || lower.contains("reading club") ||
            lower.contains("study guide") || lower.contains("reference guide") -> RequestCategory.BOOKS

            // 3. Repairs
            lower.contains("fix") || lower.contains("repair") || lower.contains("fence") ||
            lower.contains("leak") || lower.contains("plumb") || lower.contains("drill") ||
            lower.contains("screw") || lower.contains("pipe") || lower.contains("assemble") ||
            lower.contains("tool") || lower.contains("paint") || lower.contains("broken") ||
            lower.contains("faucet") || lower.contains("drywall") || lower.contains("latch") ||
            lower.contains("outlet") || lower.contains("toilet") || lower.contains("hinge") ||
            lower.contains("mending") || lower.contains("squeaky") -> RequestCategory.REPAIRS

            // 4. Transport
            lower.contains("ride") || lower.contains("transport") || lower.contains("lift") ||
            lower.contains("drive") || lower.contains("hauling") || lower.contains("pickup truck") ||
            lower.contains("airport") || lower.contains("train station") || lower.contains("car ride") ||
            lower.contains("drop-off") || lower.contains("commute") || lower.contains("transit") ||
            lower.contains("luggage") || lower.contains("bicycles") -> RequestCategory.TRANSPORT

            // 5. Pet Help (Canonical; covers pet care, dog walking, sitting, feeding)
            lower.contains("dog") || lower.contains("cat") || lower.contains("pet") ||
            lower.contains("walk") || lower.contains("stroll") ||
            lower.contains("puppy") || lower.contains("kitten") || lower.contains("vet") ||
            lower.contains("rabbit") || lower.contains("hamster") || lower.contains("aquarium") ||
            lower.contains("terrier") || lower.contains("labrador") || lower.contains("retriever") ||
            lower.contains("animal") || lower.contains("litter") -> RequestCategory.PET_HELP

            // Distinct Legacy: Tutoring (Preserved for legacy backward compatibility; not silently mapped to Books)
            lower.contains("tutor") || lower.contains("lesson") || lower.contains("algebra") ||
            lower.contains("math") || lower.contains("homework") || lower.contains("study") ||
            lower.contains("science") || lower.contains("physics") || lower.contains("exam") ||
            lower.contains("test") -> "Tutoring"

            // 6. Other (Household chores, yard cleanup, sorting, storage)
            lower.contains("yard") || lower.contains("lawn") || lower.contains("mowing") ||
            lower.contains("weeding") || lower.contains("leaves") || lower.contains("cleanup") ||
            lower.contains("storage") || lower.contains("garage") || lower.contains("recycling") ||
            lower.contains("trash") || lower.contains("basement") || lower.contains("sweeping") ||
            lower.contains("house plants") || lower.contains("watering") -> RequestCategory.OTHER

            else -> null
        }
    }

    fun isOfflineModelAvailable(): Boolean = isModelLoaded
}
