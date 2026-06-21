package com.example.mysdk

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mysdk.adapter.ProductAdapter
import com.example.mysdk.api.RetrofitClient
import com.example.mysdk.databinding.ActivityProductListBinding
import kotlinx.coroutines.launch

class ProductListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val category = intent.getStringExtra(Extras.CATEGORY) ?: "electronics"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.title = category.replaceFirstChar { it.uppercase() }
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.progressBar.visibility = View.VISIBLE
        binding.rvProducts.layoutManager = GridLayoutManager(this, 2)

        lifecycleScope.launch {
            val products = tracked("GET", "/products/category/$category") {
                RetrofitClient.api.getProductsByCategory(category)
            } ?: emptyList()

            binding.rvProducts.adapter = ProductAdapter(products) { product ->
                startActivity(
                    Intent(this@ProductListActivity, ProductDetailActivity::class.java)
                        .putExtra(Extras.PRODUCT_ID, product.id)
                )
            }
            binding.progressBar.visibility = View.GONE
        }
    }
}
