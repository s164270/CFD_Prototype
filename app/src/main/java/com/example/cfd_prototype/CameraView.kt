package com.example.cfd_prototype

import android.content.Context
import android.icu.util.UniversalTimeScale.toLong
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Composable
@ExperimentalGetImage fun CameraView(inventoryViewModel: InventoryViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor()}
    val barcodeScanner: BarcodeScanner = remember{BarcodeScanning.getClient()}
    val previewView = remember { PreviewView(context) }

    /*
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }*/

    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        AndroidView( { previewView }, modifier = Modifier.fillMaxSize()) {

            // ---
            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            // ---

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
                    .build()
                    .also { it.setAnalyzer(cameraExecutor, BarcodeAnalyzer(barcodeScanner) { code ->
                        if(inventoryViewModel.ready) {
                            vib.cancel()
                            vib.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                        inventoryViewModel.addWithBarcode(code)
                    }) }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA //CameraSelector.Builder().requireLensFacing(lensFacing).build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )

                } catch (e: java.lang.Exception) {
                    Log.e("BARCODETEST", "Error binding use cases", e)
                }
            }, ContextCompat.getMainExecutor(context))

        }


        DashedLineBox(modifier = Modifier.fillMaxSize())
        Button(onClick = { /*TODO*/ }) {
            Text(text = "TEST BUTTON")
        }

    }
}

@Composable
fun DashedLineBox(modifier: Modifier = Modifier) {
    Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
        val lb = (size.width*0.1).toFloat()
        val tb = (size.height*0.1).toFloat()
        val topLeft = Offset(x = lb, y = tb)
        val rectSize = Size((size.width - (2*size.width*0.1)).toFloat(), (size.height - (2*size.height*0.1)).toFloat())

        drawRoundRect(
            brush = SolidColor(Color.LightGray),
            topLeft = topLeft,
            size = rectSize,
            style = Stroke(width = 10f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(50f, 50f),0f))
        )
    })
}

@ExperimentalGetImage private class BarcodeAnalyzer(val barcodeScanner: BarcodeScanner, val addBarcode: (code: Long) -> Unit) : ImageAnalysis.Analyzer
{
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            // Pass image to an ML Kit Vision API
            // ...
            barcodeScanner.process(image).addOnCompleteListener {
                if(it.isSuccessful) {
                    for(barcode in it.result) {
                        Log.d("BARCODETEST", "FOUND A BARCODE I GUESS")
                        Log.d("BARCODETEST", barcode.rawValue.toString())

                        if(barcode != null && barcode.rawValue != null && barcode.rawValue!!.isDigitsOnly()) {
                            addBarcode(barcode.rawValue!!.toLong())
                        }
                    }
                }
                else {
                    Log.d("BARCODETEST", "NO BARCODES FOUND I GUESS")
                }
                imageProxy.close()
            }
        }
    }
}

/*
private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}*/




