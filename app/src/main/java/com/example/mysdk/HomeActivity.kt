package com.example.mysdk

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mysdk.adapter.ProductAdapter
import com.example.mysdk.api.RetrofitClient
import com.example.mysdk.databinding.ActivityHomeBinding
import com.example.perfsdk.PerfSDK
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "QuickShop"

        binding.progressBar.visibility = View.VISIBLE
        binding.rvProducts.layoutManager = GridLayoutManager(this, 2)

        lifecycleScope.launch {
            PerfSDK.startTrace("loadHomePage")

            val catsDeferred = async {
                tracked("GET", "/products/categories") {
                    RetrofitClient.api.getCategories()
                }
            }
            val prodsDeferred = async {
                tracked("GET", "/products") {
                    RetrofitClient.api.getProducts(limit = 6)
                }
            }

            val categories = catsDeferred.await() ?: emptyList()
            val products   = prodsDeferred.await() ?: emptyList()

            PerfSDK.stopTrace("loadHomePage")

            categories.forEach { cat ->
                val chip = Chip(this@HomeActivity).apply {
                    text = cat.replaceFirstChar { it.uppercase() }
                    isCheckable = false
                    setOnClickListener {
                        startActivity(
                            Intent(this@HomeActivity, ProductListActivity::class.java)
                                .putExtra(Extras.CATEGORY, cat)
                        )
                    }
                }
                binding.chipContainer.addView(chip)
            }

            PerfSDK.startTrace("renderProductGrid")
            withContext(Dispatchers.Default) {
                // Simulate layout calculation work per product
                products.forEach { Thread.sleep((it.price % 20 + 5).toLong()) }
            }
            binding.rvProducts.adapter = ProductAdapter(products) { product ->
                startActivity(
                    Intent(this@HomeActivity, ProductDetailActivity::class.java)
                        .putExtra(Extras.PRODUCT_ID, product.id)
                )
            }
            PerfSDK.stopTrace("renderProductGrid")

            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_login -> {
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
