package com.rexy.absenqr.mahasiswa

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.rexy.absenqr.Hapus
import com.rexy.absenqr.MainActivity
import com.rexy.absenqr.R
import kotlinx.android.synthetic.main.activity_main_mahasiswa.*
import kotlinx.android.synthetic.main.content_main_activity_mahasiswa.*

@Suppress("NAME_SHADOWING")
class MainActivityMahasiswa : AppCompatActivity() {
    private val kelas = ArrayList<Kelas>()
    private val containerkelas = ArrayList<Kelas>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_mahasiswa)
        setSupportActionBar(toolbar)
        daftarkelas()
        validasi()
        recyclerm.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        fab.setOnClickListener {
            val inten = Intent(this,TambahKelasM::class.java)
            startActivity(inten)
        }
    }

    override fun onBackPressed() {
        val inten = Intent(this,MainActivity::class.java)
        startActivity(inten)
        this.finish()
    }

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
                    kelas.clear()
                    if(newText!!.isNotEmpty()){
                        val search = newText.toUpperCase()
                        containerkelas.forEach {
                            if(it.toString().toUpperCase().contains(search)){
                                Log.d("QQ",it.toString())
                                kelas.add(it)
                            }
                        }
                    }else{
                        kelas.addAll(containerkelas)
                    }
                    recyclerm.adapter?.notifyDataSetChanged()
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun daftarkelas(){
        val user = FirebaseAuth.getInstance().currentUser
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
                        docRef2.addSnapshotListener{ documentSnapshots: QuerySnapshot?, _: FirebaseFirestoreException?->
                            for (doc in documentSnapshots!!.documentChanges) {
                                if(doc.type == DocumentChange.Type.ADDED){
                                    val namak = doc.document.getString("Nama Kelas")
                                    val namad = doc.document.getString("Nama Dosen")
                                    kelas.add(Kelas(namak.toString().toUpperCase(), namad.toString().toUpperCase()))
                                    containerkelas.add(Kelas(namak.toString().toUpperCase(), namad.toString().toUpperCase()))
                                }
                                if(doc.type == DocumentChange.Type.REMOVED){
                                    val namak = doc.document.getString("Nama Kelas")
                                    val namad = doc.document.getString("Nama Dosen")
                                    kelas.remove(Kelas(namak.toString().toUpperCase(), namad.toString().toUpperCase()))
                                    containerkelas.remove(Kelas(namak.toString().toUpperCase(), namad.toString().toUpperCase()))
                                }
                            }
                            val c= documentSnapshots.size()
                            val textK = findViewById<TextView>(R.id.textView3)
                            if(c > 0) {
                                recyclerm.visibility = View.VISIBLE
                                textK.visibility = View.INVISIBLE
                            }else{
                                recyclerm.visibility = View.INVISIBLE
                                textK.visibility = View.VISIBLE
                            }
                            recyclerm.adapter = CustomAdapterKelas(kelas)
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
    private fun validasi(){
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = user.email
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("User").document("$email")

            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val status = document.getString("Status")
                        val fakultas = document.getString("Fakultas")
                        val nama = document.getString("Nama")
                        val db = FirebaseFirestore.getInstance()
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

data class Kelas(val namaKLS: String, val namaDosen: String)

class ViewHolderKelas(itemView: View) : RecyclerView.ViewHolder(itemView){
    val textViewNamaKLS = itemView.findViewById(R.id.textViewNamaKLS) as TextView
    val textViewDosen = itemView.findViewById(R.id.textViewDosen) as TextView
}

class CustomAdapterKelas(private val userList: ArrayList<Kelas>) : RecyclerView.Adapter<ViewHolderKelas>() {
    override fun onBindViewHolder(p0: ViewHolderKelas, p1: Int) {
        val user: Kelas = userList[p1]
        p0.textViewNamaKLS.text = user.namaKLS
        p0.textViewDosen.text = user.namaDosen

        val namakelas = p0.textViewNamaKLS.text.toString()
        val namaDosen = p0.textViewDosen.text.toString()

        p0.textViewNamaKLS.setOnClickListener { view ->
            val intent = Intent(view.context, QRscanner::class.java)
            intent.putExtra("Nama Kelas",namakelas)
            intent.putExtra("Nama Dosen",namaDosen)
           view.context.startActivity(intent)
        }
        p0.textViewNamaKLS.setOnLongClickListener{view ->
            val intent = Intent(view.context, Hapus::class.java)
            intent.putExtra("Nama Kelas",namakelas)
            intent.putExtra("Nama Dosen",namaDosen)
            view.context.startActivity(intent)
            return@setOnLongClickListener true
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolderKelas {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.conteiner_m, p0, false)
        return ViewHolderKelas(v)
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}