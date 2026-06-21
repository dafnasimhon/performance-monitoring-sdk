package com.example.mysdk

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysdk.adapter.CartAdapter
import com.example.mysdk.databinding.ActivityCartBinding
import com.example.perfsdk.PerfSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.title = "My Cart"
        binding.toolbar.setNavigationOnClickListener { finish() }

        val items = CartManager.items

        if (items.isEmpty()) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.rvCartItems.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            // Delay varies by product price so P95 differs visibly from average
            PerfSDK.startTrace("calculateTotal")
            val total = withContext(Dispatchers.Default) {
                var sum = 0.0
                for (product in items) {
                    sum += product.price
                    Thread.sleep((product.price % 40 + 8).toLong())
                }
                sum
            }
            PerfSDK.stopTrace("calculateTotal")

            PerfSDK.startTrace("renderCart")
            withContext(Dispatchers.Default) {
                items.forEach { Thread.sleep((it.price % 15 + 3).toLong()) }
            }
            binding.rvCartItems.adapter = CartAdapter(items)
            PerfSDK.stopTrace("renderCart")

            binding.progressBar.visibility = View.GONE
            binding.tvTotal.text = "$${String.format("%.2f", total)}"

            binding.rvCartItems.visibility = View.VISIBLE
            binding.divider.visibility     = View.VISIBLE
            binding.layoutTotal.visibility = View.VISIBLE

            binding.btnCheckout.setOnClickListener {
                CartManager.clear()
                Toast.makeText(this@CartActivity, "Order placed! Thank you.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
