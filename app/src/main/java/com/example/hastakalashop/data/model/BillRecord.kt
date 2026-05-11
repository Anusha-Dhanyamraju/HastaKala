package com.example.hastakalashop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,
    val totalAmount: Double,
    val itemsSold: String // We will store items as a comma separated string for simplicity, or JSON.
)
