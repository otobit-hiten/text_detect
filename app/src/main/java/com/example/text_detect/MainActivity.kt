package com.example.text_detect
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.IOException


class MainActivity : AppCompatActivity() {
    var mCameraView: SurfaceView? = null
    var mTextView: TextView? = null
    var mCameraSource: CameraSource? = null
    companion object {
         const val TAG = "MainActivity"
         const val requestPermissionID = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mCameraView = findViewById(R.id.surfaceView);
        mTextView = findViewById(R.id.text_view);

        startCamera()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != requestPermissionID) {
            Log.d(TAG, "Got unexpected permission result: $requestCode")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                mCameraSource?.start(mCameraView!!.holder)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun startCamera() {

        val textRecognizer = TextRecognizer.Builder(applicationContext).build()

        if (!textRecognizer.isOperational) {
            Log.d("ERROR", "Error")
        } else {
            val cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setAutoFocusEnabled(true)
                .setRequestedFps(2.0f)
                .build()

            mCameraView?.holder?.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(p0: SurfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA), requestPermissionID)
                            return
                        }
                        cameraSource.start(mCameraView!!.holder)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}
                override fun surfaceDestroyed(p0: SurfaceHolder) {
                    cameraSource.stop()
                }

            })

            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {}

                override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                    val items = detections.detectedItems
                    if (items.size() != 0) {
                        mTextView?.post {
                            val stringBuilder = StringBuilder()
                            for (i in 0 until items.size()) {
                                val item = items.valueAt(i)
                                stringBuilder.append(item.value)
                                stringBuilder.append("\n")
                            }
                            mTextView!!.text = stringBuilder.toString()
                        }
                    }
                }

            })
        }
    }
}