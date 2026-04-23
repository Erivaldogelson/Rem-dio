package com.erivaldogelson.remedios.ocr

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.erivaldogelson.remedios.domain.model.OcrSuggestion
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MedicationTextRecognizer(
    private val context: Context,
    private val parser: MedicationOcrParser = MedicationOcrParser(),
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeFromUri(uri: Uri): OcrSuggestion {
        val image = InputImage.fromFilePath(context, uri)
        val text = recognizer.process(image).await()
        return parser.parse(text.text)
    }
}

class MedicationScanAnalyzer(
    private val parser: MedicationOcrParser = MedicationOcrParser(),
    private val onSuggestion: (OcrSuggestion) -> Unit,
) : ImageAnalysis.Analyzer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    @Volatile
    private var busy = false

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        if (busy) {
            imageProxy.close()
            return
        }
        busy = true
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { text ->
                onSuggestion(parser.parse(text.text))
            }
            .addOnFailureListener {
                onSuggestion(OcrSuggestion(rawText = ""))
            }
            .addOnCompleteListener {
                busy = false
                imageProxy.close()
            }
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
}
