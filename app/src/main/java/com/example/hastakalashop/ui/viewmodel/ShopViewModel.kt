package com.example.hastakalashop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hastakalashop.data.db.AppDatabase
import com.example.hastakalashop.data.model.BillRecord
import com.example.hastakalashop.data.model.Product
import com.example.hastakalashop.data.repository.ShopRepository
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShopRepository
    val allProducts: LiveData<List<Product>>
    val lowStockProducts: LiveData<List<Product>>
    val allBills: LiveData<List<BillRecord>>
    val totalRevenue: LiveData<Double?>
    val totalStockCount: LiveData<Int?>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ShopRepository(database.productDao(), database.billDao())
        allProducts = repository.allProducts.asLiveData()
        lowStockProducts = repository.lowStockProducts.asLiveData()
        allBills = repository.allBills.asLiveData()
        totalRevenue = repository.totalRevenue.asLiveData()
        totalStockCount = repository.totalStockCount.asLiveData()
    }

    fun insertProduct(product: Product) = viewModelScope.launch {
        repository.insertProduct(product)
    }

    fun updateProduct(product: Product) = viewModelScope.launch {
        repository.updateProduct(product)
    }

    fun recordSale(productId: Int, quantity: Int, totalAmount: Double, itemsSold: String) = viewModelScope.launch {
        val product = repository.getProductById(productId)
        if (product != null) {
            val updatedProduct = product.copy(stockQuantity = product.stockQuantity - quantity)
            repository.updateProduct(updatedProduct)
            
            val bill = BillRecord(
                date = System.currentTimeMillis(),
                totalAmount = totalAmount,
                itemsSold = itemsSold
            )
            repository.insertBill(bill)
        }
    }
}
