package com.example.mysdk

import com.example.mysdk.api.models.Product

object CartManager {
    private val _items = mutableListOf<Product>()

    val items: List<Product> get() = _items.toList()

    fun add(product: Product) {
        _items.add(product)
    }

    fun clear() {
        _items.clear()
    }
}
