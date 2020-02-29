package com.rexy.absenqr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rexy.absenqr.dosen.MainActivityDosen
import com.rexy.absenqr.mahasiswa.MainActivityMahasiswa
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val permissionRequest = 200
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), permissionRequest)
        }
        buttonLogin.setOnClickListener{
            login()
            Log.d("QQ","Click")
        }
        textView.setOnClickListener {
            val inten = Intent(this,Mendaftar::class.java)
            startActivity(inten)
        }
        lupapassword.setOnClickListener {
            val intent = Intent(this,Password::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun login(){
        val email = loginEmail.text.toString()
        val pass  = loginPass.text.toString()
        if(email.isEmpty() || pass.isEmpty()){
            Toast.makeText(this, "Isi Semua Kolom", Toast.LENGTH_SHORT).show()
            return
        }else{
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,pass)
                .addOnSuccessListener {
                    val user = FirebaseAuth.getInstance().currentUser
                    if(user!!.isEmailVerified){
                        validasi()
                    }else{
                        Toast.makeText(this, "Harap Melakukan Verifikasi Email", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Email/Password Salah", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validasi(){
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = user.email
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("User").document("$email")

            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val job = document.getString("Status")
                        if( job == "DOSEN"){
                            val inten = Intent(this, MainActivityDosen::class.java)
                            startActivity(inten)
                            Log.d("QQ","$job")
                            this.finish()
                        }else if( job == "MAHASISWA"){
                            val inten2 = Intent(this, MainActivityMahasiswa::class.java)
                            startActivity(inten2)
                            Log.d("QQ","$job")
                            this.finish()
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
}
