package com.example.cfd_prototype

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BarcodeInfoDao {

    @Query("SELECT * FROM barcode_info")
    fun getAll(): List<BarcodeInfo>

    @Query("SELECT * FROM barcode_info WHERE item_barcode LIKE :code LIMIT 1")
    suspend fun findByBarcode(code: Long): BarcodeInfo?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(barcodeInfo: BarcodeInfo)

    @Query("SELECT EXISTS(SELECT * FROM barcode_info WHERE item_barcode LIKE :code)")
    suspend fun isRowExists(code: Long) : Boolean

    @Delete
    suspend fun delete(barcodeInfo: BarcodeInfo)

    @Query("DELETE FROM barcode_info")
    suspend fun deleteAll()

}