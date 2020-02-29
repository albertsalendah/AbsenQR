package com.rexy.absenqr.mahasiswa

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rexy.absenqr.R
import java.util.ArrayList

@Suppress("NAME_SHADOWING")
class TambahKelasM : AppCompatActivity() {
    private var adapterd: ArrayAdapter<*>? = null
    private var adapterk: ArrayAdapter<*>? = null
    private val listdosen = ArrayList<String>()
    private val containerlistdosen = ArrayList<String>()
    val kelas = ArrayList<String>()
    private val containerkelas = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_kelas_m)
        daftarDosen()
    }

    override fun onBackPressed() {
       this.finish()
    }

    private fun daftarDosen(){
        val listDosen = findViewById<View>(R.id.listDosen) as ListView
        val listkelas = findViewById<ListView>(R.id.listkelas)
        val searchDosen = findViewById<EditText>(R.id.searchDosen)
        val namakelas = findViewById<EditText>(R.id.namakelas)
        val buttonkelas = findViewById<Button>(R.id.buttonkelas)
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = user.email
            val db = FirebaseFirestore.getInstance()
            val db2 = FirebaseFirestore.getInstance()
            val db3 = FirebaseFirestore.getInstance()
            val docRef = db.collection("User").document("$email")

            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val fakultas = document.getString("Fakultas")
                        val docRef2 = db2.collection("User")
                        docRef2.get().addOnSuccessListener { result ->
                            for (document in result) {
                                val u = document.id
                                if(document["Status"].toString() == "DOSEN"){
                                    Log.w("QQ", u)
                                    listdosen.add(document["Nama"].toString().toUpperCase())
                                    containerlistdosen.add(document["Nama"].toString().toUpperCase())
                                }
                            }

                            adapterd = ArrayAdapter(this, R.layout.list, listdosen)
                            listDosen.adapter = adapterd
                            searchDosen.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                                    listkelas.visibility = View.GONE
                                    listDosen.invalidateViews()
                                }

                                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                                    listDosen.visibility = View.VISIBLE
                                    namakelas.visibility = View.GONE
                                    listkelas.visibility = View.GONE
                                    buttonkelas.visibility = View.INVISIBLE
                                    namakelas.text.clear()
                                    listDosen.invalidateViews()
                                    //(adapter as ArrayAdapter<String>).filter.filter(charSequence)
                                    listdosen.clear()
                                    if(charSequence.isNotEmpty()){
                                        val search = charSequence.toString().toUpperCase()
                                        containerlistdosen.forEach {
                                            if(it.toUpperCase().contains(search)){
                                                Log.d("QQ",it)
                                                listdosen.add(it)
                                            }
                                        }
                                    }else{
                                        listdosen.addAll(containerlistdosen)
                                    }
                                    adapterd?.notifyDataSetChanged()
                                    return
                                }

                                override fun afterTextChanged(editable: Editable) {
                                    listkelas.visibility = View.GONE
                                    listDosen.invalidateViews()
                                }
                            })
                            listDosen.setOnItemClickListener { _, _, position, _ ->
                                searchDosen.setText(listDosen.getItemAtPosition(position).toString())
                                listDosen.visibility = View.GONE
                                namakelas.visibility = View.VISIBLE
                                containerkelas.clear()
                                 val namaD = searchDosen.text.toString().toUpperCase()
                                 Log.w("QQ", namaD)
                                 val docRef3 = db3.collection("DataAbsen/$fakultas/DOSEN/$namaD/Kelas")
                                docRef3.get().addOnSuccessListener {result ->
                                    val kk = ArrayList<String>()
                                    for (document in result) {
                                        val nk = document["Nama Kelas"]
                                        val kK = document["Kode Kelas"]
                                        val dat = "$kK-$nk"
                                        Log.w("QQ", dat)
                                        kelas.add(dat.toUpperCase())
                                        containerkelas.add(dat.toUpperCase())
                                        kk.add(document["Kode Kelas"].toString())
                                    }
                                    adapterk = ArrayAdapter(this, R.layout.list, kelas)
                                    listkelas.adapter = adapterk
                                    namakelas.addTextChangedListener(object : TextWatcher {
                                        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                                            listkelas.visibility = View.GONE
                                            buttonkelas.visibility = View.INVISIBLE
                                            listkelas.invalidateViews()
                                        }
                                        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                                            listkelas.visibility = View.VISIBLE
                                            buttonkelas.visibility = View.INVISIBLE
                                            listkelas.invalidateViews()
                                            //(this@TambahKelasM.adapter as ArrayAdapter<*>).filter.filter(charSequence)
                                            kelas.clear()
                                            if(charSequence.isNotEmpty()){
                                                val search = charSequence.toString().toUpperCase()
                                                containerkelas.forEach {
                                                    if(it.toUpperCase().contains(search)){
                                                        Log.d("QQ",it)
                                                        kelas.add(it)
                                                    }
                                                }
                                            }else{
                                                kelas.addAll(containerkelas)
                                            }
                                            adapterk?.notifyDataSetChanged()
                                            return
                                        }

                                        override fun afterTextChanged(editable: Editable) {
                                            buttonkelas.visibility = View.INVISIBLE
                                            listkelas.invalidateViews()
                                        }
                                    })
                                    listkelas.setOnItemClickListener { _, _, position, _ ->
                                        namakelas.setText(listkelas.getItemAtPosition(position).toString())
                                        listkelas.visibility = View.GONE
                                        buttonkelas.visibility = View.VISIBLE
                                        val check = listkelas.getItemAtPosition(position).toString()
                                    buttonkelas.setOnClickListener {
                                        val namaDosen = searchDosen.text
                                        val namakelas = namakelas.text
                                        if (namaDosen.isEmpty() || namakelas.isEmpty() || namakelas.toString() != check) {
                                            Log.w("QQ", "KOSONG")
                                        }else {
                                            tambahkelas(namakelas.toString(),namaDosen.toString())
                                            Log.w("QQ", "$namaDosen++$namakelas")
                                            }
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

    private fun tambahkelas(nk:String,nd:String){
        val kelas = HashMap<String, Any>()
        kelas["Nama Kelas"] = nk.toUpperCase()
        kelas["Nama Dosen"] = nd.toUpperCase()
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
                        db.collection("DataAbsen/$fakultas/$status/$nama/Kelas/").document(nk).set(kelas)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Kelas Berhasil Ditambahkan", Toast.LENGTH_LONG).show()
                                this.finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Kelas Gagal Ditambahkan", Toast.LENGTH_LONG).show()
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (MotionEvent.ACTION_OUTSIDE == event.action) {
            Log.d("QQ","Touch")
            finish()
            return true
        }
        return super.onTouchEvent(event)
    }
}
