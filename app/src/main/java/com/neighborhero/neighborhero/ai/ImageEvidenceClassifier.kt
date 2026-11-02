package com.neighborhero.neighborhero.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.neighborhero.neighborhero.data.model.RequestCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Result status for offline task-evidence image verification.
 * Adheres strictly to safety rules: never claims proof of completion or identity.
 */
enum class EvidenceAssessmentStatus {
    LIKELY_RELEVANT,
    POSSIBLE_MISMATCH,
    NEEDS_REVIEW,
    BYPASS_UNSUPPORTED,
    ERROR
}

/**
 * Three user-friendly AI image-matching statuses without percentages or technical jargon.
 */
enum class AiMatchStatus(val title: String, val advisory: String) {
    MATCHED("Matched", "The image appears relevant to the requested task."),
    PARTIALLY_MATCHED("Partially Matched", "The image may be relevant, but needs closer inspection."),
    NOT_MATCHED("Not Matched", "The image does not appear relevant to the requested task.")
}

/**
 * Encapsulates the output of the on-device evidence domain relevance classifier.
 */
data class EvidenceClassificationResult(
    val status: EvidenceAssessmentStatus,
    val predictedCategory: String?,
    val confidence: Float,
    val classProbabilities: Map<String, Float>,
    val displayMessage: String,
    val isBypassed: Boolean = false,
    val matchStatus: AiMatchStatus = when (status) {
        EvidenceAssessmentStatus.LIKELY_RELEVANT -> AiMatchStatus.MATCHED
        EvidenceAssessmentStatus.NEEDS_REVIEW, EvidenceAssessmentStatus.BYPASS_UNSUPPORTED -> AiMatchStatus.PARTIALLY_MATCHED
        EvidenceAssessmentStatus.POSSIBLE_MISMATCH, EvidenceAssessmentStatus.ERROR -> AiMatchStatus.NOT_MATCHED
    }
) {
    val matchTitle: String get() = matchStatus.title
    val matchAdvisory: String get() = matchStatus.advisory
}

/**
 * On-device TensorFlow Lite image evidence relevance classifier.
 * Uses MobileNetV2 transfer learning trained on task-completion demonstration images.
 * Runs 100% locally without network requirements.
 */
class ImageEvidenceClassifier(
    private val context: Context? = null,
    private val modelPath: String = "evidence_classifier.tflite",
    private val labelsPath: String = "labels.txt"
) : Closeable {

    companion object {
        const val INPUT_WIDTH = 224
        const val INPUT_HEIGHT = 224
        const val INPUT_CHANNELS = 3
        const val BYTES_PER_FLOAT = 4

        // Relevance thresholds
        const val THRESHOLD_RELEVANT = 0.60f
        const val THRESHOLD_UNRELATED_ALERT = 0.50f

        // Supported task categories for visual evidence verification
        val SUPPORTED_CATEGORIES = setOf(
            RequestCategory.GROCERIES.lowercase(),
            RequestCategory.REPAIRS.lowercase(),
            RequestCategory.PET_HELP.lowercase(),
            "pet care" // legacy alias
        )
    }

    private var interpreter: Interpreter? = null
    var labels: List<String> = listOf("groceries", "pet_help", "repairs", "unrelated")
        private set

    init {
        if (context != null) {
            loadModelAndLabels()
        }
    }

    private fun loadModelAndLabels() {
        val ctx = context ?: return
        try {
            labels = loadLabels(ctx, labelsPath)
            val modelBuffer = loadModelByteBuffer(ctx, modelPath)
            val options = Interpreter.Options().apply {
                setNumThreads(2)
            }
            val tflite = Interpreter(modelBuffer, options)
            
            // Verify input tensor specifications
            val inputTensor = tflite.getInputTensor(0)
            val inputShape = inputTensor.shape()
            val inputType = inputTensor.dataType()
            
            // Expected: [1, 224, 224, 3] float32
            if (inputType != DataType.FLOAT32) {
                System.err.println("Warning: expected FLOAT32 input tensor, found $inputType")
            }
            if (inputShape.size >= 4 && (inputShape[1] != INPUT_WIDTH || inputShape[2] != INPUT_HEIGHT)) {
                System.err.println("Warning: unexpected input tensor dimensions: ${inputShape.contentToString()}")
            }

            interpreter = tflite
        } catch (e: Exception) {
            System.err.println("Error initializing ImageEvidenceClassifier: ${e.message}")
            interpreter = null
        }
    }

    private fun loadLabels(context: Context, path: String): List<String> {
        return try {
            context.assets.open(path).bufferedReader().useLines { lines ->
                lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
            }
        } catch (e: Exception) {
            listOf("groceries", "pet_help", "repairs", "unrelated")
        }
    }

    private fun loadModelByteBuffer(context: Context, path: String): ByteBuffer {
        return try {
            val fileDescriptor = context.assets.openFd(path)
            FileInputStream(fileDescriptor.fileDescriptor).channel.use { channel ->
                channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fileDescriptor.startOffset,
                    fileDescriptor.declaredLength
                )
            }
        } catch (e: Exception) {
            // Fallback for compressed assets or mock test contexts
            val bytes = context.assets.open(path).use { it.readBytes() }
            val directBuffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
            directBuffer.put(bytes)
            directBuffer.rewind()
            directBuffer
        }
    }

    /**
     * Evaluates whether a given task category is supported by the visual classifier.
     */
    fun isCategorySupported(taskCategory: String?): Boolean {
        if (taskCategory.isNullOrBlank()) return false
        val norm = taskCategory.trim().lowercase()
        return SUPPORTED_CATEGORIES.contains(norm)
    }

    /**
     * Classifies a photo submitted as task completion proof.
     * Evaluates domain relevance against the intended task category.
     */
    suspend fun classifyEvidence(
        photoBytes: ByteArray?,
        taskCategory: String?
    ): EvidenceClassificationResult = withContext(Dispatchers.Default) {
        if (photoBytes == null || photoBytes.isEmpty()) {
            return@withContext EvidenceClassificationResult(
                status = EvidenceAssessmentStatus.ERROR,
                predictedCategory = null,
                confidence = 0f,
                classProbabilities = emptyMap(),
                displayMessage = "No proof image provided."
            )
        }

        val bitmap = try {
            BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
        } catch (e: Exception) {
            null
        }

        if (bitmap == null) {
            return@withContext EvidenceClassificationResult(
                status = EvidenceAssessmentStatus.ERROR,
                predictedCategory = null,
                confidence = 0f,
                classProbabilities = emptyMap(),
                displayMessage = "Failed to decode photo for visual analysis."
            )
        }

        classifyBitmap(bitmap, taskCategory)
    }

    suspend fun classifyBitmap(
        bitmap: Bitmap?,
        taskCategory: String?
    ): EvidenceClassificationResult = withContext(Dispatchers.Default) {
        if (bitmap == null) {
            return@withContext EvidenceClassificationResult(
                status = EvidenceAssessmentStatus.ERROR,
                predictedCategory = null,
                confidence = 0f,
                classProbabilities = emptyMap(),
                displayMessage = "No valid image provided."
            )
        }

        val categoryName = taskCategory?.trim() ?: RequestCategory.OTHER

        // Check unsupported category bypass
        if (!isCategorySupported(categoryName)) {
            return@withContext EvidenceClassificationResult(
                status = EvidenceAssessmentStatus.BYPASS_UNSUPPORTED,
                predictedCategory = null,
                confidence = 0f,
                classProbabilities = emptyMap(),
                displayMessage = "Visual AI is not applicable for this category ($categoryName). Submission proceeds directly to requester confirmation.",
                isBypassed = true
            )
        }

        val tflite = interpreter
        if (tflite == null) {
            return@withContext EvidenceClassificationResult(
                status = EvidenceAssessmentStatus.ERROR,
                predictedCategory = null,
                confidence = 0f,
                classProbabilities = emptyMap(),
                displayMessage = "Image evidence classifier is not available on this device."
            )
        }

        try {
            // Resize image to 224x224 RGB
            val scaledBitmap = if (bitmap.width == INPUT_WIDTH && bitmap.height == INPUT_HEIGHT) {
                bitmap
            } else {
                Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, true)
            }

            // Prepare float32 input buffer (1, 224, 224, 3) with raw RGB [0..255]
            val inputBuffer = ByteBuffer.allocateDirect(1 * INPUT_WIDTH * INPUT_HEIGHT * INPUT_CHANNELS * BYTES_PER_FLOAT)
            inputBuffer.order(ByteOrder.nativeOrder())

            val pixels = IntArray(INPUT_WIDTH * INPUT_HEIGHT)
            scaledBitmap.getPixels(pixels, 0, INPUT_WIDTH, 0, 0, INPUT_WIDTH, INPUT_HEIGHT)

            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                inputBuffer.putFloat(r.toFloat())
                inputBuffer.putFloat(g.toFloat())
                inputBuffer.putFloat(b.toFloat())
            }

            val numClasses = if (labels.isNotEmpty()) labels.size else 4
            val outputBuffer = Array(1) { FloatArray(numClasses) }

            tflite.run(inputBuffer, outputBuffer)

            val rawProbabilities = outputBuffer[0]
            evaluateAssessment(categoryName, rawProbabilities, labels)
        } catch (e: Exception) {
            EvidenceClassificationResult(
                status = EvidenceAssessmentStatus.ERROR,
                predictedCategory = null,
                confidence = 0f,
                classProbabilities = emptyMap(),
                displayMessage = "Inference error: ${e.message}"
            )
        }
    }

    /**
     * Evaluates model probability outputs against the target task category.
     * Pure function for predictable assessment logic and unit testing.
     */
    fun evaluateAssessment(
        taskCategory: String?,
        rawProbabilities: FloatArray,
        labelList: List<String> = labels
    ): EvidenceClassificationResult {
        val categoryName = taskCategory?.trim() ?: RequestCategory.OTHER

        // Check unsupported category bypass
        if (!isCategorySupported(categoryName)) {
            return EvidenceClassificationResult(
                status = EvidenceAssessmentStatus.BYPASS_UNSUPPORTED,
                predictedCategory = null,
                confidence = 0f,
                classProbabilities = emptyMap(),
                displayMessage = "Visual AI is not applicable for this category ($categoryName). Submission proceeds directly to requester confirmation.",
                isBypassed = true
            )
        }

        val probMap = mutableMapOf<String, Float>()
        for (i in rawProbabilities.indices) {
            val label = labelList.getOrElse(i) { "class_$i" }
            probMap[label] = rawProbabilities[i]
        }

        val topIndex = rawProbabilities.indices.maxByOrNull { rawProbabilities[it] } ?: 0
        val topClass = labelList.getOrElse(topIndex) { "unknown" }
        val topConfidence = rawProbabilities.getOrElse(topIndex) { 0f }

        // Determine target model class for this request
        val targetClass = when (categoryName.lowercase()) {
            "groceries" -> "groceries"
            "repairs" -> "repairs"
            "pet help", "pet care" -> "pet_help"
            else -> "unrelated"
        }

        val formattedTopClass = formatClassName(topClass)
        val confidencePct = (topConfidence * 100).toInt()

        val (status, message) = when {
            // Top class matches target category and meets high-confidence threshold
            topClass == targetClass && topConfidence >= THRESHOLD_RELEVANT -> {
                EvidenceAssessmentStatus.LIKELY_RELEVANT to
                        "Image appears likely relevant to $categoryName."
            }

            // Top class matches target category but has lower confidence
            topClass == targetClass -> {
                EvidenceAssessmentStatus.NEEDS_REVIEW to
                        "Image may be relevant to $categoryName, but visual features are ambiguous. Requester review recommended."
            }

            // Top prediction is unrelated scenery
            topClass == "unrelated" -> {
                if (topConfidence >= THRESHOLD_UNRELATED_ALERT) {
                    EvidenceAssessmentStatus.POSSIBLE_MISMATCH to
                            "Photo may not show completion evidence for $categoryName (unrelated image detected). Requester will review your submission."
                } else {
                    EvidenceAssessmentStatus.NEEDS_REVIEW to
                            "Image content is ambiguous. Requester confirmation required."
                }
            }

            // Top prediction is a different supported category (e.g. repairs photo on groceries task)
            else -> {
                EvidenceAssessmentStatus.POSSIBLE_MISMATCH to
                        "Photo appears closer to $formattedTopClass than $categoryName. Requester will review your submission."
            }
        }

        return EvidenceClassificationResult(
            status = status,
            predictedCategory = formattedTopClass,
            confidence = topConfidence,
            classProbabilities = probMap,
            displayMessage = message
        )
    }

    fun formatClassName(rawName: String): String {
        return when (rawName.lowercase()) {
            "groceries" -> RequestCategory.GROCERIES
            "pet_help" -> RequestCategory.PET_HELP
            "repairs" -> RequestCategory.REPAIRS
            "unrelated" -> "Unrelated"
            else -> rawName.replaceFirstChar { it.uppercase() }
        }
    }

    override fun close() {
        try {
            interpreter?.close()
        } catch (_: Exception) {}
        interpreter = null
    }
}
