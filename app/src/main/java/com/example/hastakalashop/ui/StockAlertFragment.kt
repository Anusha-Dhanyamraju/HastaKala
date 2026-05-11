package com.example.hastakalashop.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.hastakalashop.R
import com.example.hastakalashop.data.model.Product
import com.example.hastakalashop.databinding.FragmentStockAlertBinding
import com.example.hastakalashop.databinding.ItemStockAlertBinding
import com.example.hastakalashop.ui.viewmodel.ShopViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class StockAlertFragment : Fragment() {

    private var _binding: FragmentStockAlertBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ShopViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val adapter = StockAdapter { product ->
            showAddStockDialog(product)
        }
        binding.recyclerStockAlerts.adapter = adapter
        
        viewModel.lowStockProducts.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }
    }

    private fun showAddStockDialog(product: Product) {
        val input = EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Enter quantity"
        }
        val container = FrameLayout(requireContext()).apply {
            setPadding(48, 16, 48, 16)
            addView(input)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Stock: ${product.name}")
            .setMessage("How many units would you like to add?")
            .setView(container)
            .setPositiveButton("Add") { _, _ ->
                val qtyStr = input.text.toString()
                if (qtyStr.isNotEmpty()) {
                    val qty = qtyStr.toIntOrNull() ?: 0
                    if (qty > 0) {
                        val updatedProduct = product.copy(stockQuantity = product.stockQuantity + qty)
                        viewModel.updateProduct(updatedProduct)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class StockAdapter(private val onAddStockClick: (Product) -> Unit) : RecyclerView.Adapter<StockAdapter.ViewHolder>() {
    
    private var items: List<Product> = emptyList()
    
    fun submitList(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    class ViewHolder(val binding: ItemStockAlertBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemStockAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvItemName.text = item.name
        holder.binding.tvVariant.text = item.variant
        holder.binding.tvStockCount.text = "${item.stockQuantity} Left"
        
        // Products in this list are already low stock (<= 5)
        // If <= 2, we show critical color
        if (item.stockQuantity <= 2) {
            holder.binding.ivWarning.visibility = View.VISIBLE
            holder.binding.badgeCard.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.stock_critical)
            )
        } else {
            holder.binding.ivWarning.visibility = View.VISIBLE
            holder.binding.badgeCard.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.stock_low)
            )
        }
        
        holder.binding.btnAddStock.setOnClickListener {
            onAddStockClick(item)
        }
    }

    override fun getItemCount() = items.size
}
