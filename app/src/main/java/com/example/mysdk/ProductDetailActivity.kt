package com.example.mysdk

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mysdk.api.RetrofitClient
import com.example.mysdk.api.models.CartProduct
import com.example.mysdk.api.models.CartRequest
import com.example.mysdk.databinding.ActivityProductDetailBinding
import kotlinx.coroutines.launch

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getIntExtra(Extras.PRODUCT_ID, 1)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val product = tracked("GET", "/products/$productId") {
                RetrofitClient.api.getProduct(productId)
            }

            binding.progressBar.visibility = View.GONE

            if (product != null) {
                binding.toolbar.title = product.title.take(24)
                binding.scrollView.visibility = View.VISIBLE

                Glide.with(this@ProductDetailActivity)
                    .load(product.image)
                    .into(binding.ivProduct)

                binding.chipCategory.text = product.category.replaceFirstChar { it.uppercase() }
                binding.tvTitle.text       = product.title
                binding.tvPrice.text       = "$${String.format("%.2f", product.price)}"
                binding.tvDescription.text = product.description

                binding.btnAddToCart.setOnClickListener {
                    binding.btnAddToCart.isEnabled = false
                    binding.btnAddToCart.text = "Adding…"

                    lifecycleScope.launch {
                        // POST to fakestoreapi for SDK network demo; result not used for cart display
                        tracked("POST", "/carts") {
                            RetrofitClient.api.addToCart(
                                CartRequest(products = listOf(CartProduct(product.id, 1)))
                            )
                        }
                        CartManager.add(product)
                        startActivity(Intent(this@ProductDetailActivity, CartActivity::class.java))
                        binding.btnAddToCart.isEnabled = true
                        binding.btnAddToCart.text = "Add to Cart 🛒"
                    }
                }
            }
        }
    }
}
