package com.rexy.absenqr.mahasiswa

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.rexy.absenqr.R
import java.io.IOException

class ActivityScanner : AppCompatActivity() {
    internal lateinit var cameraView: SurfaceView
    private lateinit var barcode: BarcodeDetector
    internal lateinit var cameraSource: CameraSource
    private lateinit var holder: SurfaceHolder
    lateinit var layout : LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        cameraView = findViewById(R.id.cameraView)
        cameraView.setZOrderMediaOverlay(true)
        holder = cameraView.holder
        barcode = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()
        if (!barcode.isOperational) {
            Toast.makeText(applicationContext, "Sorry, Couldn't setup the detector", Toast.LENGTH_LONG).show()
            this.finish()
        }
        cameraSource = CameraSource.Builder(this, barcode)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedFps(24f)
            .setAutoFocusEnabled(true)
            .setRequestedPreviewSize(1920, 1024)
            .build()
        cameraView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ContextCompat.checkSelfPermission(this@ActivityScanner, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    {
                        cameraSource.start(cameraView.holder)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        })

        barcode.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() > 0) {
                    val intent = Intent()
                    intent.putExtra("barcode", barcodes.valueAt(0))
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        })
    }
    override fun onBackPressed() {
        val intent = Intent(this,MainActivityMahasiswa::class.java)
        startActivity(intent)
        this.finish()
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (MotionEvent.ACTION_OUTSIDE == event.action) {
            Log.d("QQ","Touch")
            val intent = Intent(this,MainActivityMahasiswa::class.java)
            startActivity(intent)
            this.finish()
            return true
        }
        return super.onTouchEvent(event)
    }
}
