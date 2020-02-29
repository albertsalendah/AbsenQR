package com.rexy.absenqr.dosen

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.rexy.absenqr.R
import java.util.HashMap

@Suppress("NAME_SHADOWING")
class QRGenerator : AppCompatActivity() {
    private lateinit var imageQR: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrgenerator)

        imageQR = findViewById(R.id.imageQR)
        val namakelas = intent.getStringExtra("NamaKelas")
        val tanggal = intent.getStringExtra("Tanggal")
        val string = "$namakelas-$tanggal"
        tambahAbsen(tanggal,namakelas)
        qrgenerator(string)
    }

private fun qrgenerator(text:String){
    if (text.isNotEmpty()) {
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 400, 400)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            imageQR.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}
    private fun tambahAbsen(tanggal:String ,nk1:String){
        val user = FirebaseAuth.getInstance().currentUser
        val kelas = HashMap<String, Any>()
        kelas["Nama Kelas"] = nk1.toUpperCase()
        kelas["Tanggal"] = tanggal
        user?.let {
            val email = user.email
            val db = FirebaseFirestore.getInstance()
            val db2 = FirebaseFirestore.getInstance()
            val docRef = db.collection("User").document("$email")

            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val fakultas = document.getString("Fakultas")
                        val status =  document.getString("Status")
                        val nama =  document.getString("Nama")
                        val docRef2 = db2.collection("DataAbsen/$fakultas/$status/$nama/Kelas/")
                        docRef2.get().addOnSuccessListener { result ->
                            for (document in result) {
                                val nk = document["Nama Kelas"]
                                val kK = document["Kode Kelas"]
                                val dat = "$kK-$nk"
                                if(nk1.toUpperCase() == dat.toUpperCase()){
                                    val docRef2 = db2.collection("DataAbsen/$fakultas/$status/$nama/Kelas/"+document.id+"/$nk1")
                                    docRef2.get().addOnSuccessListener {
                                        db.collection("DataAbsen/$fakultas/$status/$nama/Kelas/"+document.id+"/$nk1")
                                            .document("$nk1-$tanggal").set(kelas)
                                            .addOnSuccessListener {
                                                Log.d("QQ", "Input Berhasil")
                                            }
                                            .addOnFailureListener {
                                                Log.d("QQ", "Input Gagal")
                                            }
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "No Data", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onBackPressed() {
        this.finish()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (MotionEvent.ACTION_OUTSIDE == event.action) {
            Log.d("QQ","Touch")
            finish()
            return true
        }
        return super.onTouchEvent(event)
    }
}
