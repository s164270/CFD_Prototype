package com.example.cfd_prototype

sealed class Destination(val route: String) {
    object Home: Destination("home")
    object Inventory: Destination("inventory")
    object AddItem: Destination("add")
    object TakePicture: Destination("takepicture")
    object Item: Destination("item/{id}") {
        fun routeToItem(id: Int) = route.replace("{id}", id.toString())
    }

}
