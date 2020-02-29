package com.rexy.absenqr

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_hapus.*

class Hapus : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hapus)

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = user.email
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("User").document("$email")

            docRef.get()
                .addOnSuccessListener { document ->
                    if(document != null) {
                        val status =  document.getString("Status")
                        if(status == "DOSEN"){
                            val namaK = intent.getStringExtra("Nama K")
                            hapus.text = "Hapus Kelas?"
                            yes.setOnClickListener {
                                hapuskelas(namaK)
                                this.finish()
                            }
                            no.setOnClickListener {
                                this.finish()
                            }
                            Log.d("QQ",status)
                        }else if(status == "MAHASISWA"){
                            Log.d("QQ",status)
                            val namakelas = intent.getStringExtra("Nama Kelas")
                            hapus.text = "Hapus Kelas?"
                            Log.d("QQ",namakelas)
                            yes.setOnClickListener {
                                hapuskelas(namakelas)
                                this.finish()
                            }
                            no.setOnClickListener {
                                this.finish()
                            }
                        }
                    }else{
                        Toast.makeText(this, "No Data", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun hapuskelas(namakelas:String){
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = user.email
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("User").document("$email")

            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val fakultas = document.getString("Fakultas")
                        val status =  document.getString("Status")
                        val nama =  document.getString("Nama")
                        db.collection("DataAbsen/$fakultas/$status/$nama/Kelas/").document(namakelas)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("QQ", "DocumentSnapshot successfully deleted!")
                                Toast.makeText(this, "Kelas Berhasil Dihapus", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                                    e -> Log.w("QQ", "Error deleting document", e)
                                Toast.makeText(this, "Kelas Gagal Dihapus", Toast.LENGTH_LONG).show()
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
