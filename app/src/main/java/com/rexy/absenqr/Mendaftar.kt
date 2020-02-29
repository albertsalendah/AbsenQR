package com.rexy.absenqr

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_mendaftar.*

class Mendaftar : AppCompatActivity() {
    private lateinit var radioGroup: RadioGroup
    lateinit var radioButtonT: RadioButton
    private lateinit var kodedosen: EditText
    private lateinit var nim: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mendaftar)
        val dosen = "DOSEN"
        val mahasiswa = "MAHASISWA"

        kodedosen = this.findViewById(R.id.text_kode_dosen)
        nim = findViewById(R.id.text_nim)
        radioGroup = findViewById(R.id.radioGroup_status)
          radioGroup.setOnCheckedChangeListener { _, _ ->
            val id = radioGroup.checkedRadioButtonId
            radioButtonT = findViewById<View>(id) as RadioButton
            val status = radioButtonT.text.toString()
            Log.d("QQ", "Click-$status")
               when {
                   radioButtonT.text.toString() == dosen -> {
                     Log.d("QQ", "$radioButtonT")
                     kodedosen.visibility = View.VISIBLE
                     nim.visibility = View.GONE
                      }
                    radioButtonT.text.toString() == mahasiswa -> {
                     nim.visibility = View.VISIBLE
                     kodedosen.visibility = View.GONE
                      }
                }
           }

        val spinner: Spinner = this.findViewById(R.id.spinner)
        val daftarfakultas = arrayOf("Fakultas","FTI", "FEB", "FKIP")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, daftarfakultas)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                daftar.setOnClickListener {
                    if (radioGroup.checkedRadioButtonId == -1)
                    {
                        Toast.makeText(this@Mendaftar, "Isi Semua Kolom", Toast.LENGTH_LONG).show()
                    }
                    else
                    {   val status = radioButtonT.text.toString()
                        val fakultas = daftarfakultas[position]

                        if(status == "DOSEN" && kodedosen.text.isEmpty()){
                            Toast.makeText(this@Mendaftar, "Isi Semua Kolom", Toast.LENGTH_LONG).show()
                        }else if(status == "MAHASISWA" && nim.text.isEmpty()){
                            Toast.makeText(this@Mendaftar, "Isi Semua Kolom", Toast.LENGTH_LONG).show()
                        }else{
                            if(fakultas == "Fakultas"){
                                Toast.makeText(this@Mendaftar, "Pilih Fakultas Anda", Toast.LENGTH_LONG).show()
                            }else{
                                mendaftar(status,fakultas)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun mendaftar(Stats: String, Fakul: String){
        val nama  = text_Nama_Lengkap.text.toString().toUpperCase()
        val email = text_Email.text.toString()
        val pass  = text_Pass.text.toString()
        val status = Stats.toUpperCase()
        val fakultas = Fakul.toUpperCase()

        if(email.isEmpty() || pass.isEmpty() || nama.isEmpty() || status.isEmpty()){
            Toast.makeText(this, "Isi Semua Kolom", Toast.LENGTH_SHORT).show()
            return
        }else {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    Toast.makeText(this, "Berhasil Mendaftar", Toast.LENGTH_LONG).show()
                    validasi(email,pass,status,fakultas)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Email Sudah Terdaftar", Toast.LENGTH_LONG).show()
                }
             }
        }

    private fun validasi(Em:String,Pa:String, Stats: String, Fakul: String){
        val status = Stats.toUpperCase()
        val fakultas = Fakul.toUpperCase()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(Em, Pa).addOnSuccessListener {
            val user = FirebaseAuth.getInstance().currentUser
            if ( user != null) {
                user.let {
                    add(status, fakultas)
                    Log.d("QQ", "Berhasil LogIn")
                    user.sendEmailVerification()
                }
            }else{
                Log.d("QQ", "Gagal LogIn")
            }
        }
    }

    private fun add(Stats:String, Fakul:String){
            val nama  = text_Nama_Lengkap.text.toString().toUpperCase()
            val kodedosen = text_kode_dosen.text.toString().toUpperCase()
            val nim = text_nim.text.toString().toUpperCase()
            val fakultas = Fakul.toUpperCase()
            val email = text_Email.text.toString()
            val status = Stats.toUpperCase()
             val db = FirebaseFirestore.getInstance()
            val profile = HashMap<String, Any>()
            profile["Nama"] = nama
            profile["Fakultas"]  = fakultas
            profile["Status"] = status
            val daftarFakultas = HashMap<String, Any>()
            daftarFakultas["Fakultas"]  = fakultas
            val daftarDosen  = HashMap<String, Any>()
            if (status == "DOSEN") {
                daftarDosen["Nama"] = nama
                daftarDosen["KodeDosen"] = kodedosen
                 profile["Kode_Dosen"] = kodedosen
                 Log.d("QQ", "Nama : $nama")
                 Log.d("QQ", "Status : $status")
                 Log.d("QQ", "Fakultas : $kodedosen")
                 Log.d("QQ", "Fakultas : $fakultas")
                 Log.d("QQ", "Fakultas : $email")
            } else if (status == "MAHASISWA") {
                 profile["NIM"] = nim
                 Log.d("QQ", "Nama : $nama")
                 Log.d("QQ", "Status : $status")
                 Log.d("QQ", "NIM : $nim")
                 Log.d("QQ", "Fakultas : $fakultas")
                 Log.d("QQ", "email : $email")
            }
        db.collection("DataAbsen/$fakultas/$status/$nama/Profile").add(profile)
                .addOnSuccessListener {
                Toast.makeText(this, "Cek Email Untuk Melakukan Verifikasi Akun Anda", Toast.LENGTH_LONG).show()
                 db.collection("User").document(email).set(profile)
                    this.finish()
                }
                .addOnFailureListener {
                 Toast.makeText(this, "Input Failed", Toast.LENGTH_LONG).show()
                    this.finish()
                }
        db.collection("DataAbsen/$fakultas/$status/$nama/Profile")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                     Log.d("QQ", document.id + " => " + document.data)
                }
             }
            .addOnFailureListener { exception ->
                Log.w("QQ", "Error getting documents.", exception)
        }
    }
}
