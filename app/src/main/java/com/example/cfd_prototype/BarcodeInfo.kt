package com.example.cfd_prototype

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barcode_info")
data class BarcodeInfo(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "item_barcode") val barcode: Long?,
    @ColumnInfo(name = "item_name") val name: String?,
    @ColumnInfo(name = "item_category") val category: String?
)