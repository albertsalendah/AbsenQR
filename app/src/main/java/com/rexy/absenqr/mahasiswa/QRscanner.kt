package com.rexy.absenqr.mahasiswa

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.vision.barcode.Barcode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Suppress("NAME_SHADOWING")
class QRscanner : AppCompatActivity() {
    private val requestcode = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this@QRscanner, ActivityScanner::class.java)
        startActivityForResult(intent, requestcode)
    }
    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val namakelas=intent.getStringExtra("Nama Kelas")
        val namadosen=intent.getStringExtra("Nama Dosen")

        if (requestCode == requestcode && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val barcode = data.getParcelableExtra<Barcode>("barcode")
                Log.d("QQ", barcode.displayValue)
                val scan = barcode.displayValue.toString().toUpperCase()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    val tanggal1: String =  current.format(formatter)
                    Log.d("QQ","$tanggal1+//+1")
                    if(scan == "$namakelas-$tanggal1"){
                        Log.d("QQ", "sama")
                        daftarkelas(namadosen,namakelas,scan,tanggal1)
                    }else{
                        val intent = Intent(this,MainActivityMahasiswa::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "Salah Kelas", Toast.LENGTH_LONG).show()
                        this.finish()
                    }
                } else {
                    val date = Date()
                    val formatter = SimpleDateFormat("dd.MM.yyyy")
                    val tanggal2: String = formatter.format(date)
                    Log.d("QQ","$tanggal2+//+2")
                    if(scan == "$namakelas-$tanggal2"){
                        Log.d("QQ", "sama")
                        daftarkelas(namadosen,namakelas,scan,tanggal2)
                    }else{
                        val intent = Intent(this,MainActivityMahasiswa::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "Salah Kelas", Toast.LENGTH_LONG).show()
                        this.finish()
                    }
                }
            }
        }
    }
    private fun daftarkelas(namadosen:String,namakelas:String,scan:String,tgl:String){
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = user.email
            val db = FirebaseFirestore.getInstance()
            val db2 = FirebaseFirestore.getInstance()
            val docRef = db.collection("User").document("$email")

            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val fakultas = document.getString("Fakultas").toString().toUpperCase()
                        val nim =  document.getString("NIM").toString().toUpperCase()
                        val nama =  document.getString("Nama").toString().toUpperCase()
                        val nn = "$nim $nama"
                        val docRef2 = db2.collection("DataAbsen/$fakultas/DOSEN/$namadosen/Kelas/")
                        docRef2.get().addOnSuccessListener { result ->
                            for (document in result) {
                                val n = document["Nama Kelas"]
                                val m = document["Kode Kelas"]
                                val o = "$m-$n"
                                Log.d("QQ",o)
                                if(namakelas == o){
                                    Log.d("QQ", "SAMA")
                                    Log.d("QQ", "$nama++$nim")
                                    val absen = HashMap<String, Any>()
                                    absen["Nama"]          = nama.toUpperCase()
                                    absen["NIM"]           = nim.toUpperCase()
                                    absen["Tanggal Absen"] = tgl.toUpperCase()
                                    db.document("DataAbsen/$fakultas/DOSEN/$namadosen/Kelas/"+document.id+"/$namakelas/$scan/DaftarMahasiswa/$nn")
                                        .set(absen)
                                        .addOnSuccessListener {
                                            val intent = Intent(this,MainActivityMahasiswa::class.java)
                                            startActivity(intent)
                                            Toast.makeText(this, "Input Absen Berhasil", Toast.LENGTH_LONG).show()
                                            Log.d("QQ","Input Absen Berhasil")
                                            this.finish()
                                        }
                                        .addOnFailureListener {
                                            val intent = Intent(this,MainActivityMahasiswa::class.java)
                                            startActivity(intent)
                                            Toast.makeText(this, "Input Absen Gagal", Toast.LENGTH_LONG).show()
                                            Log.d("QQ","Input Absen Gagal")
                                            this.finish()
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
}
