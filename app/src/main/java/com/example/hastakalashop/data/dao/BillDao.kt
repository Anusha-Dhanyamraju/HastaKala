package com.example.hastakalashop.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.hastakalashop.data.model.BillRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert
    suspend fun insertBill(bill: BillRecord)

    @Query("SELECT * FROM bills ORDER BY date DESC")
    fun getAllBills(): Flow<List<BillRecord>>

    @Query("SELECT SUM(totalAmount) FROM bills")
    fun getTotalRevenue(): Flow<Double?>

    @androidx.room.Delete
    suspend fun deleteBill(bill: BillRecord)
}
