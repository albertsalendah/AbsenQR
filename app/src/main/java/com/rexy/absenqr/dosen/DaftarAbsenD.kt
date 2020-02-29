package com.rexy.absenqr.dosen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.rexy.absenqr.R
import kotlinx.android.synthetic.main.activity_daftar_absen_d.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

@Suppress("NAME_SHADOWING")
class DaftarAbsenD : AppCompatActivity() {
    private var adapter: ArrayAdapter<*>? = null
    private val absen = ArrayList<String>()
    private val containerabsen = ArrayList<String>()
    private val listtanggal = ArrayList<String>()
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_absen_d)

        daftarkelas()

        fab4.setOnClickListener { view ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                val tanggal1: String =  current.format(formatter)
                val clickCheck = "click"
                Log.d("QQ", clickCheck)
                val nk1 = intent.getStringExtra("Nama Kelas")
                val intent = Intent(view.context, QRGenerator::class.java)
                intent.putExtra("Tanggal",tanggal1)
                intent.putExtra("clickCheck",clickCheck)
                intent.putExtra("NamaKelas",nk1)
                view.context.startActivity(intent)
            } else {
                val date = Date()
                val formatter = SimpleDateFormat("dd.MM.yyyy")
                val tanggal2: String = formatter.format(date)
                val nk1 = intent.getStringExtra("Nama Kelas")
                val intent = Intent(view.context, QRGenerator::class.java)
                intent.putExtra("Tanggal",tanggal2)
                intent.putExtra("NamaKelas",nk1)
                view.context.startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        val intent= Intent(this,MainActivityDosen::class.java)
        startActivity(intent)
        this.finish()
    }
//------------------------SEARCH-------------------------------\\
  override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search,menu)
        val searchItem = menu.findItem(R.id.menu_search)
        if(searchItem != null){
            val searchView = searchItem.actionView as SearchView
            val editext = searchView.findViewById<EditText>(android.support.v7.appcompat.R.id.search_src_text)
            editext.hint = "Search here..."
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    absen.clear()
                    if(newText!!.isNotEmpty()){
                        val search = newText.toUpperCase()
                        containerabsen.forEach {
                            if(it.toUpperCase().contains(search)){
                                Log.d("QQ",it)
                                absen.add(it)
                            }
                        }
                    }else{
                        absen.addAll(containerabsen)
                    }
                    adapter?.notifyDataSetChanged()
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }
//-------------------------------------------------------------------\\
//-------------------------------BUAT-DAFTAR-ABSEN------------------------\\
  private fun daftarkelas(){
        val listAbsenDosen = findViewById<View>(R.id.listAbsenDosen) as ListView
        val user = FirebaseAuth.getInstance().currentUser
        val nk1 = intent.getStringExtra("Nama Kelas")
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
                                val nk = document["Nama Kelas"].toString()
                                val kK = document["Kode Kelas"].toString()
                                val dat = "$kK-$nk"
                                if(nk1.toUpperCase() == dat.toUpperCase()){
                                    val docRef2 = db2.collection("DataAbsen/$fakultas/$status/$nama/Kelas/"+document.id+"/$nk1")
                                    docRef2.addSnapshotListener{ documentSnapshots: QuerySnapshot?, _: FirebaseFirestoreException?->
                                        for (doc in documentSnapshots!!.documentChanges) {
                                            if(doc.type == DocumentChange.Type.ADDED){
                                                val tgl = doc.document.getString("Tanggal")
                                                absen.add("$dat-$tgl")
                                                containerabsen.add("$dat-$tgl")
                                                listtanggal.add(tgl!!)

                                            }
                                            if(doc.type == DocumentChange.Type.REMOVED){
                                                val tgl = doc.document.getString("Tanggal")
                                                absen.remove("$dat-$tgl")
                                                containerabsen.remove("$dat-$tgl")
                                            }
                                        }

                                        val c= documentSnapshots.size()
                                        val textK = findViewById<TextView>(R.id.textView5)
                                        if(c > 0) {
                                            listAbsenDosen.visibility = View.VISIBLE
                                            textK.visibility = View.INVISIBLE
                                        }else{
                                            listAbsenDosen.visibility = View.INVISIBLE
                                            textK.visibility = View.VISIBLE
                                        }
                                        adapter = ArrayAdapter(this, R.layout.list, absen)
                                        listAbsenDosen.adapter = adapter
                                        val t = listAbsenDosen.count
                                        Log.d("QQ","Jumlah $t")
                                        listAbsenDosen.setOnItemClickListener { _, view, position, _ ->
                                            val op = listAbsenDosen.getItemAtPosition(position).toString()
                                            Log.d("QQ", op)
                                            val intent = Intent(view.context, DaftarHadirD::class.java)
                                            intent.putExtra("Nama Kelas",nk1)
                                            intent.putExtra("Tanggal Absen",op)
                                            view.context.startActivity(intent)
                                        }
                                        listAbsenDosen.setOnItemLongClickListener { _, view, position, _ ->
                                            val op = listAbsenDosen.getItemAtPosition(position).toString()
                                            val intent = Intent(view.context, HapusAbsen::class.java)
                                            intent.putExtra("nk",dat)
                                            intent.putExtra("Nama Kelas Dosen",op)
                                            view.context.startActivity(intent)
                                            return@setOnItemLongClickListener true
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
}
