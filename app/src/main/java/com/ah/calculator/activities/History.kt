package com.ah.calculator.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.ah.calculator.CalItem
import com.ah.calculator.R
import com.ah.calculator.adapters.CalItemsAdapter
import com.ah.calculator.databinding.ActMainBinding
import com.ah.calculator.databinding.ActivityHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class History : AppCompatActivity() {
    private lateinit var transRef: DatabaseReference
    private lateinit var binding: ActivityHistoryBinding
    private var allCalculation: ArrayList<CalItem> = ArrayList()
    private val TAG: String = "TAG"
    private lateinit var adapter: CalItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(LayoutInflater.from(this))
        transRef = FirebaseDatabase.getInstance().getReference("users")
            .child("${FirebaseAuth.getInstance().currentUser?.uid}").child("all")
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //fetch data whenever availble
        transRef.addValueEventListener(postListener)

        //config RV
        adapter = CalItemsAdapter()
        binding.rvCalcs.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvCalcs.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    val postListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            Log.d(TAG, dataSnapshot.toString())
            allCalculation.clear()
            for (ds: DataSnapshot in dataSnapshot.children) {
                //val item: CalItem? = ds.getValue(CalItem::class.java)
                Log.d(TAG, "onmark " + ds.toString())
                val item: CalItem? = ds.getValue(CalItem::class.java)
                if (item != null) {
                    allCalculation.add(item)
                }
            }
            //udpate to to recycler view
            adapter.setDataSet(allCalculation)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
        }
    }
}