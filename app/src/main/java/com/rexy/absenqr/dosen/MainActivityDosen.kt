package com.rexy.absenqr.dosen

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

import kotlinx.android.synthetic.main.activity_main_dosen.*
import kotlinx.android.synthetic.main.content_main_activity_dosen.*
import java.util.ArrayList

@Suppress("NAME_SHADOWING")
class MainActivityDosen : AppCompatActivity() {
    val kelas = ArrayList<String>()
    private val containerkelas  = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_dosen)
        setSupportActionBar(toolbar)
        daftarkelas()
        validasi()
        listKelasdosen.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        fab.setOnClickListener {
            val inten = Intent(this, TambahKelasD::class.java)
            startActivity(inten)
        }
    }

    override fun onBackPressed() {
        val inten = Intent(this, MainActivity::class.java)
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
                            if(it.toUpperCase().contains(search)){
                                Log.d("QQ",it)
                                kelas.add(it)
                            }
                        }
                    }else{
                        kelas.addAll(containerkelas)
                    }
                    listKelasdosen.adapter?.notifyDataSetChanged()
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

 private fun daftarkelas() {
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
                        val status = document.getString("Status")
                        val nama = document.getString("Nama")

                        val docs = db2.collection("DataAbsen/$fakultas/$status/$nama/Kelas/")
                        docs.addSnapshotListener{ documentSnapshots: QuerySnapshot?, _: FirebaseFirestoreException?->
                            for (doc in documentSnapshots!!.documentChanges) {
                                if(doc.type == DocumentChange.Type.ADDED){
                                    val nk = doc.document.getString("Nama Kelas")
                                    val kK = doc.document.getString("Kode Kelas")
                                    val dat = "$kK-$nk"
                                    kelas.add(dat.toUpperCase())
                                    containerkelas.add(dat.toUpperCase())
                                }
                                if(doc.type == DocumentChange.Type.REMOVED){
                                    val nk = doc.document.getString("Nama Kelas")
                                    val kK = doc.document.getString("Kode Kelas")
                                    val dat = "$kK-$nk"
                                    kelas.remove(dat.toUpperCase())
                                    containerkelas.remove(dat.toUpperCase())
                                }
                            }
                            val c= documentSnapshots.size()
                            val textK = findViewById<TextView>(R.id.textView4)
                            if(c > 0) {
                                listKelasdosen.visibility = View.VISIBLE
                                textK.visibility = View.INVISIBLE
                            }else{
                                listKelasdosen.visibility = View.INVISIBLE
                                textK.visibility = View.VISIBLE
                            }
                            listKelasdosen.adapter = CustomAdapterKelas(kelas)
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

class ViewHolderKelas(itemView: View) : RecyclerView.ViewHolder(itemView){
    val namaKLSD = itemView.findViewById(R.id.textViewNamaKLSD) as TextView
}

class CustomAdapterKelas(private val userList: ArrayList<String>) : RecyclerView.Adapter<ViewHolderKelas>() {

    override fun onBindViewHolder(p0: ViewHolderKelas, p1: Int) {
        val kelas: String = userList[p1]
        p0.namaKLSD.text = kelas

        val namakelas = p0.namaKLSD.text.toString()

        p0.namaKLSD.setOnClickListener { view ->
            val inten = Intent(view.context, DaftarAbsenD::class.java)
            inten.putExtra("Nama Kelas",namakelas)
            view.context.startActivity(inten)
        }
        p0.namaKLSD.setOnLongClickListener{ view ->
            val intent = Intent(view.context, Hapus::class.java)
            intent.putExtra("Nama K",namakelas)
            view.context.startActivity(intent)
            return@setOnLongClickListener true
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolderKelas {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.container_d, p0, false)
        return ViewHolderKelas(v)
    }


    override fun getItemCount(): Int {
        return userList.size
    }
}