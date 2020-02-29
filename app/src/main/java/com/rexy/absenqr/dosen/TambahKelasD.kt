package com.rexy.absenqr.dosen

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rexy.absenqr.R


class TambahKelasD : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_kelas_d)

        val namakelasdosen = findViewById<EditText>(R.id.namakelasdosen)
        val kodekelasdosen = findViewById<EditText>(R.id.kodekelasdosen)
        val add = findViewById<Button>(R.id.tambahkelasdosen)

        add.setOnClickListener {
            val nk = namakelasdosen.text.toString().toUpperCase()
            val kk = kodekelasdosen.text.toString().toUpperCase()
            if(nk.isEmpty() || kk.isEmpty()){
                Toast.makeText(this,"Isi Semua",Toast.LENGTH_SHORT).show()
            }else{
                tambahkelas(nk,kk)
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

    private fun tambahkelas(nk:String,kk:String){
        val kelas = HashMap<String, Any>()
        kelas["Nama Kelas"] = nk.toUpperCase()
        kelas["Kode Kelas"] = kk.toUpperCase()
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = user.email
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("User").document("$email")

            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val fakultas = document.getString("Fakultas")
                        val status = document.getString("Status")
                        val nama = document.getString("Nama")
                        db.document("DataAbsen/$fakultas/$status/$nama/Kelas/$kk-$nk").set(kelas)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Input Success", Toast.LENGTH_LONG).show()
                                this.finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Input Failed", Toast.LENGTH_LONG).show()
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
