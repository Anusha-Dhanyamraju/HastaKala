package com.example.hastakalashop.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.hastakalashop.data.dao.BillDao
import com.example.hastakalashop.data.dao.ProductDao
import com.example.hastakalashop.data.model.BillRecord
import com.example.hastakalashop.data.model.Product
import com.example.hastakalashop.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Product::class, BillRecord::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun billDao(): BillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hastakala_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        prepopulate(context)
                    }

                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        prepopulate(context)
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private fun prepopulate(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).productDao()
                val products = listOf(
                    Product(name = "Kalamkari Saree", price = 2500.0, stockQuantity = 12, category = "Clothing", variant = "Red/Black", imageResId = R.drawable.img_bag),
                    Product(name = "Terracotta Pot", price = 450.0, stockQuantity = 3, category = "Home Decor", variant = "Large", imageResId = R.drawable.img_keychain),
                    Product(name = "Handloom Kurta", price = 1200.0, stockQuantity = 2, category = "Clothing", variant = "Indigo", imageResId = R.drawable.img_wall),
                    Product(name = "Brass Lamp", price = 3500.0, stockQuantity = 8, category = "Home Decor", variant = "Antique", imageResId = R.drawable.img_coasters),
                    Product(name = "Banana Fiber Bag", price = 600.0, stockQuantity = 1, category = "Accessories", variant = "Natural", imageResId = R.drawable.img_bag),
                    Product(name = "Madhubani Painting", price = 4200.0, stockQuantity = 4, category = "Art", variant = "Framed", imageResId = R.drawable.img_wall)
                )
                products.forEach { dao.insertProduct(it) }
            }
        }
    }
}
