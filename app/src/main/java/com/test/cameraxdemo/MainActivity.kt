package com.test.cameraxdemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var imageAnalysis: ImageAnalysis

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var savePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        savePath = filesDir.absolutePath + System.currentTimeMillis() + ".jpg"

        previewView = findViewById(R.id.previewView)

        checkPermission()

        val takePhotoBtn = findViewById<Button>(R.id.takePhotoBtn)
        takePhotoBtn.setOnClickListener {
            val metadata = ImageCapture.Metadata()
            metadata.isReversedHorizontal = false
            val outputFileOption =
                ImageCapture.OutputFileOptions.Builder(File(savePath)).setMetadata(metadata).build()
            imageCapture.takePicture(
                outputFileOption,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.i("tag", "保存路径：${outputFileResults.savedUri.toString()}")
                    }


                    override fun onError(exception: ImageCaptureException) {
                        Log.i("tag", "拍照出错了")
                    }
                }
            )
        }
    }


    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            initCamera()
        }
    }

    private lateinit var camera: Camera

    private fun initCamera() {
        previewView.post {
            imageCapture =
                ImageCapture.Builder().setTargetRotation(previewView.display.rotation).build()

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(previewView.width, previewView.height))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraProviderFuture.addListener({
                cameraProvider = cameraProviderFuture.get()
                setupCamera()
            }, ContextCompat.getMainExecutor(this))
        }
    }

    private fun setupCamera() {
        cameraProvider.let {
            it.unbindAll()
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
            val preview = Preview.Builder().build()
            camera = it.bindToLifecycle(this, cameraSelector, imageCapture, preview)

            preview.setSurfaceProvider(previewView.surfaceProvider)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCamera()
            }
        }
    }


}