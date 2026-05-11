package com.example.hastakalashop.data.repository

import com.example.hastakalashop.data.dao.BillDao
import com.example.hastakalashop.data.dao.ProductDao
import com.example.hastakalashop.data.model.BillRecord
import com.example.hastakalashop.data.model.Product
import kotlinx.coroutines.flow.Flow

class ShopRepository(private val productDao: ProductDao, private val billDao: BillDao) {

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val allBills: Flow<List<BillRecord>> = billDao.getAllBills()
    val totalRevenue: Flow<Double?> = billDao.getTotalRevenue()
    val totalStockCount: Flow<Int?> = productDao.getTotalStockCount()

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun getProductById(id: Int): Product? {
        return productDao.getProductById(id)
    }

    suspend fun insertBill(bill: BillRecord) {
        billDao.insertBill(bill)
    }

    suspend fun deleteBill(bill: BillRecord) {
        billDao.deleteBill(bill)
    }
}
