package com.example.cfd_prototype

import java.time.LocalDate

enum class GroceryCategory {
    NoCategory,
    Meat,
    Dairy,
    Fruit,
    Vegetable,
    Bread
}

fun categoryFromString(cat: String): GroceryCategory {
    return when(cat) {
        "NoCategory" -> GroceryCategory.NoCategory
        "Meat" -> GroceryCategory.Meat
        "Dairy" -> GroceryCategory.Dairy
        "Fruit" -> GroceryCategory.Fruit
        "Vegetable" -> GroceryCategory.Vegetable
        "Bread" -> GroceryCategory.Bread
        else -> GroceryCategory.NoCategory
    }
}

class Grocery(
    val id: Int,
    val barcode: Long = 0,
    val name: String = "",
    val category: GroceryCategory = GroceryCategory.NoCategory,
    val boughtDate: LocalDate = LocalDate.now(),
) {
    var expirationDate: LocalDate

    init {
        val expiresInDays = when(category) {
            GroceryCategory.Vegetable -> 7
            GroceryCategory.NoCategory -> 30
            GroceryCategory.Meat -> 7
            GroceryCategory.Dairy -> 7
            GroceryCategory.Fruit -> 7
            GroceryCategory.Bread -> 7
        }
        expirationDate = boughtDate.plusDays(expiresInDays.toLong())
    }

    fun openItem() {
        expirationDate = LocalDate.now().plusDays(1)
    }
}