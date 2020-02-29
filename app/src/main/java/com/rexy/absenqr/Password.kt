package com.rexy.absenqr

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_password.*

class Password : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)
        submit.setOnClickListener{
            val email = email.text.toString()
            val auth = FirebaseAuth.getInstance()
            if(email.isEmpty()){
                Toast.makeText(this@Password, "Masukan Email Anda", Toast.LENGTH_SHORT).show()
            }else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@Password, "Permintaan Reset Password Terkirim", Toast.LENGTH_LONG).show()
                            Toast.makeText(this@Password, "Cek Email Anda", Toast.LENGTH_SHORT).show()
                            this.finish()
                        } else {
                            Toast.makeText(this@Password, "Email Tidak Terdaftar", Toast.LENGTH_SHORT).show()
                            this.finish()
                        }
                    }
            }
        }
    }
    override fun onBackPressed() {
        this.finish()
    }
}
