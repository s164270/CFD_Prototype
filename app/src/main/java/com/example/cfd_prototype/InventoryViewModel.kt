package com.example.cfd_prototype

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings.Global
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InventoryViewModel : ViewModel() {
    val groceries = mutableListOf<Grocery>()


    @Volatile var ready: Boolean = true
    lateinit var appDb: AppDatabase

    fun addWithBarcode(code: Long) {
        /*            GlobalScope.launch(Dispatchers.IO) {
                codeInfo = appDatabase.barcodeInfoDao().findByBarcode(testcode)
                Log.d("BARCODETEST1", "Name: " + codeInfo.name)
            }*/

        if(ready) {
            ready = false
            GlobalScope.launch(Dispatchers.IO) {
                val codeInfo = appDb.barcodeInfoDao().findByBarcode(code)
                if(codeInfo != null) {
                    groceries.add(
                        Grocery(id = groceries.size + 1,
                            barcode = codeInfo.barcode!!,
                            name = codeInfo.name!!,
                            category = categoryFromString(codeInfo.category!!)))
                } else {
                    groceries.add(
                        Grocery(id = groceries.size + 1,
                            barcode = code,
                            name = "Unknown item",
                            category = GroceryCategory.NoCategory))
                }
                // delay here?

                delay(1500)

                ready = true
            }
        }

    }

    fun addItem(item: Grocery = Grocery(0)) {
        groceries.add(item)
    }

    fun getItemWithId(id: Int) = groceries.find { it.id == id }

    init {
        //groceries.clear()
        groceries.addAll(
            listOf(
                Grocery(id = 0, barcode = 11, name = "Toastbrød", category = GroceryCategory.Dairy)
                )
        )
    }
}
/*,
                Grocery(id = 2, barcode = 12, name = "Sødmælk", category = GroceryCategory.Dairy),
                Grocery(id = 3, barcode = 13, name = "Agurk", category = GroceryCategory.Vegetable),
                Grocery(id = 4, barcode = 14, name = "Mørbrad", category = GroceryCategory.Meat),
                Grocery(id = 5, barcode = 15, name = "Toastbrød", category = GroceryCategory.Bread)*/