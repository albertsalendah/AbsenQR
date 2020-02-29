package com.rexy.absenqr.dosen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.rexy.absenqr.R
import kotlinx.android.synthetic.main.activity_daftar_hadir_d.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.ArrayList

@Suppress("NAME_SHADOWING")
class DaftarHadirD : AppCompatActivity() {
    private var adapter: ArrayAdapter<*>? = null
    val mahasiswa  = ArrayList<String>()
    private val containermahasiswa  = ArrayList<String>()
    private val permissionRequest = 200
    private val arrayNIM = ArrayList<String>()
    private val arrayNama = ArrayList<String>()
    private var tanggal:String = ""
    private lateinit var cell : PdfPCell
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_hadir_d)
        val daftarhadir = findViewById<View>(R.id.daftarhadir) as ListView
        val user = FirebaseAuth.getInstance().currentUser
        val nk1 = intent.getStringExtra("Nama Kelas")
        val tglAbsen = intent.getStringExtra("Tanggal Absen")
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
                        val kodedosen = document.getString("Kode_Dosen")
                        val docRef2 = db2.collection("DataAbsen/$fakultas/$status/$nama/Kelas/")
                        docRef2.get().addOnSuccessListener { result ->
                            for (document in result) {
                                val nk = document["Nama Kelas"].toString()
                                val kK = document["Kode Kelas"].toString()
                                val dat = "$kK-$nk"
                                if(nk1.toUpperCase() == dat.toUpperCase()){
                                    val docRe = db2.collection("DataAbsen/$fakultas/$status/$nama/Kelas/"+document.id+"/$nk1/$tglAbsen/DaftarMahasiswa/")
                                    docRe.addSnapshotListener{ documentSnapshots: QuerySnapshot?, _: FirebaseFirestoreException?->

                                        for (doc in documentSnapshots!!.documentChanges) {
                                            if(doc.type == DocumentChange.Type.ADDED){
                                                val namaM= doc.document.getString("Nama")
                                                val nim = doc.document.getString("NIM")
                                                val tgl = doc.document.getString("Tanggal Absen")
                                                val id = "$nim $namaM"
                                                mahasiswa.add(id)
                                                containermahasiswa.add(id)
                                                arrayNIM.add(nim.toString())
                                                arrayNama.add(namaM.toString())
                                                tanggal = tgl.toString()
                                            }
                                            if(doc.type == DocumentChange.Type.REMOVED){
                                                val namaM = doc.document.getString("Nama")
                                                val nim = doc.document.getString("NIM")
                                                val id = "$nim $namaM"
                                                mahasiswa.remove(id)
                                                containermahasiswa.remove(id)
                                            }
                                        }
                                        val jumlahdata= documentSnapshots.size()
                                        val jumlah = mahasiswa.size
                                        buttonPDF.setOnClickListener {
                                            if(jumlah > 0) {
                                                if (ContextCompat.checkSelfPermission(
                                                        this,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                    ) != PackageManager.PERMISSION_GRANTED
                                                ) {
                                                    ActivityCompat.requestPermissions(
                                                        this,
                                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                                        permissionRequest
                                                    )
                                                } else {
                                                    createPDF(jumlah, arrayNIM, arrayNama, tanggal, nk1!!,kodedosen!!,nama!!)
                                                }
                                            }else{
                                                Toast.makeText(this@DaftarHadirD,"Daftar Hadir Kosong",Toast.LENGTH_LONG).show()
                                            }
                                        }
                                        val textK = findViewById<TextView>(R.id.textView6)
                                        if(jumlahdata > 0) {
                                            daftarhadir.visibility = View.VISIBLE
                                            textK.visibility = View.INVISIBLE
                                        }else{
                                            daftarhadir.visibility = View.INVISIBLE
                                            textK.visibility = View.VISIBLE
                                        }
                                        adapter = ArrayAdapter(this, R.layout.list, mahasiswa)
                                        daftarhadir.adapter = adapter as ArrayAdapter<*>
                                        //-----------------------SEARCH-----------------------\\
                                        searchM.addTextChangedListener(object : TextWatcher{
                                            override fun afterTextChanged(s: Editable?) {
                                            }
                                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                                            }
                                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                                mahasiswa.clear()
                                                if(s!!.isNotEmpty()){
                                                    val search = s.toString().toUpperCase()
                                                    containermahasiswa.forEach {
                                                        if(it.toUpperCase().contains(search)){
                                                            Log.d("QQ",it)
                                                            mahasiswa.add(it)
                                                        }
                                                    }
                                                }else{
                                                    mahasiswa.addAll(containermahasiswa)
                                                }
                                                adapter?.notifyDataSetChanged()
                                                return
                                            }
                                        })
                                    }
                                        //---------------------------------------------------------\\
                                    daftarhadir.setOnItemClickListener { _, _, position, _ ->
                                        val op = daftarhadir.getItemAtPosition(position).toString()
                                        Log.d("QQ", op)
                                    }
                                    daftarhadir.setOnItemLongClickListener { _, view, position, _ ->
                                        val op = daftarhadir.getItemAtPosition(position).toString()
                                        val intent = Intent(view.context, HapusDaftarHadir::class.java)
                                        intent.putExtra("Nama Mahasiswa",op)
                                        intent.putExtra("nk",nk1)
                                        intent.putExtra("tgl",tglAbsen)
                                        view.context.startActivity(intent)
                                        return@setOnItemLongClickListener true
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

    private fun createPDF(jumlah:Int,nim:ArrayList<String>,nama:ArrayList<String>,tgl:String,kelas:String,kodedosen:String,namadosen:String) {
        Log.d("QQ", "$jumlah-$nim-$nama-$tgl-$kelas")

      val path = Environment.getExternalStorageDirectory().absolutePath + "/AbsenQR/$kelas"
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val doc = Document()
        val file = File(dir, "$kelas-$tgl.pdf")
        try {
            PdfWriter.getInstance(doc, FileOutputStream(file))
            doc.open()
            val pt = PdfPTable(1)
            pt.widthPercentage = 100f
            val fl = floatArrayOf(10f)
            pt.setWidths(fl)
            cell = PdfPCell()
            cell.border = Rectangle.NO_BORDER
            cell.addElement(Paragraph("Nama Dosen    : $namadosen"))
            cell.addElement(Paragraph("Kode Dosen    : $kodedosen"))
            cell.addElement(Paragraph("Nama Kelas    : $kelas"))
            cell.addElement(Paragraph("Tanggal Absen : $tgl"))
            pt.addCell(cell)

            val pTable = PdfPTable(1)
            pTable.widthPercentage = 100f
            cell = PdfPCell()
            cell.colspan = 1
            cell.addElement(pt)
            pTable.addCell(cell)
            val table = PdfPTable(3)

            val columnWidth = floatArrayOf(5f, 10f, 35f)
            table.setWidths(columnWidth)
            cell = PdfPCell()
            cell.colspan = 6
            cell.addElement(pTable)
            table.addCell(cell)
            cell = PdfPCell(Phrase(" "))
            cell.colspan = 6
            table.addCell(cell)
            cell = PdfPCell()
            cell.colspan = 6
            cell = PdfPCell(Phrase("#"))
            table.addCell(cell)
            cell = PdfPCell(Phrase("NIM"))
            table.addCell(cell)
            cell = PdfPCell(Phrase("Nama"))
            table.addCell(cell)
            table.headerRows = 3
            cell = PdfPCell()
            cell.colspan = 6
            table.addCell(cell)

            for (i in 1..jumlah) {
                table.addCell(i.toString())
                table.addCell(nim[i-1])
                table.addCell(nama[i-1])
            }

            doc.add(table)
            Toast.makeText(applicationContext, "created PDF", Toast.LENGTH_LONG).show()
            doc.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        this.finish()
    }
}
