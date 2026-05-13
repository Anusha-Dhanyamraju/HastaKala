package com.example.hastakalashop.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hastakalashop.R
import com.example.hastakalashop.data.model.Product
import android.net.Uri
import com.bumptech.glide.Glide
import com.example.hastakalashop.databinding.DialogAddProductBinding
import com.example.hastakalashop.databinding.FragmentQuickBillBinding
import com.example.hastakalashop.databinding.ItemProductCardBinding
import com.example.hastakalashop.ui.viewmodel.ShopViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest

class QuickBillFragment : Fragment() {

    private var _binding: FragmentQuickBillBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ShopViewModel by viewModels()
    
    private var quantity = 1
    private var selectedProduct: Product? = null
    private var tempImageUri: Uri? = null
    
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            tempImageUri = uri
            currentDialogBinding?.let { binding ->
                Glide.with(this).load(uri).into(binding.ivProductPreview)
            }
        }
    }

    private var currentDialogBinding: DialogAddProductBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuickBillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupQuantityPicker()
        
        binding.btnCloseSelection.setOnClickListener {
            resetSelection()
        }
        
        binding.btnSaveSale.setOnClickListener {
            val product = selectedProduct
            val priceStr = binding.etPrice.text.toString()
            
            if (product != null && priceStr.isNotEmpty()) {
                val totalAmount = priceStr.toDouble() * quantity
                val itemsSold = "${product.name} (x$quantity)"
                
                viewModel.recordSale(product.id, quantity, totalAmount, itemsSold)
                
                Snackbar.make(view, "Sale saved successfully!", Snackbar.LENGTH_SHORT).show()
                resetSelection()
            } else {
                Snackbar.make(view, "Please select a product and enter price", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.fabAddProduct.setOnClickListener {
            showAddProductDialog()
        }
    }

    private fun showAddProductDialog() {
        tempImageUri = null
        val dialogBinding = DialogAddProductBinding.inflate(layoutInflater)
        currentDialogBinding = dialogBinding
        
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.Theme_HastaKalaShop)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnPickImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        dialogBinding.btnSaveProduct.setOnClickListener {
            val name = dialogBinding.etProductName.text.toString()
            val priceStr = dialogBinding.etProductPrice.text.toString()
            val stockStr = dialogBinding.etProductStock.text.toString()
            val category = dialogBinding.etProductCategory.text.toString()
            val variant = dialogBinding.etProductVariant.text.toString()

            if (name.isNotEmpty() && priceStr.isNotEmpty() && stockStr.isNotEmpty() && category.isNotEmpty()) {
                val product = Product(
                    name = name,
                    price = priceStr.toDouble(),
                    stockQuantity = stockStr.toInt(),
                    category = category,
                    variant = variant,
                    imageResId = R.drawable.img_bag,
                    imageUri = tempImageUri?.toString()
                )
                viewModel.insertProduct(product)
                Snackbar.make(binding.root, R.string.product_added_success, Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Snackbar.make(dialogBinding.root, R.string.error_empty_fields, Snackbar.LENGTH_SHORT).show()
            }
        }

        dialog.setOnDismissListener {
            currentDialogBinding = null
        }
        dialog.show()
    }
    
    private fun resetSelection() {
        binding.layoutSelection.visibility = View.GONE
        selectedProduct = null
        quantity = 1
        binding.tvQuantity.text = quantity.toString()
        binding.etPrice.text?.clear()
        binding.chipGroupVariants.clearCheck()
    }

    private fun setupQuantityPicker() {
        binding.btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
            }
        }
        binding.btnPlus.setOnClickListener {
            quantity++
            binding.tvQuantity.text = quantity.toString()
        }
    }

    private fun setupRecyclerView() {
        val adapter = ProductAdapter { product ->
            selectedProduct = product
            binding.layoutSelection.visibility = View.VISIBLE
            binding.tvSelectedProduct.text = product.name
            binding.etPrice.setText(product.price.toString())
            
            // Populate variants if any
            binding.chipGroupVariants.removeAllViews()
            if (product.variant.isNotEmpty()) {
                val variants = product.variant.split("/")
                variants.forEach { variant ->
                    val chip = Chip(requireContext()).apply {
                        text = variant.trim()
                        isCheckable = true
                    }
                    binding.chipGroupVariants.addView(chip)
                }
            }
        }
        
        binding.recyclerProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerProducts.adapter = adapter
        
        viewModel.allProducts.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ProductAdapter(private val onClick: (Product) -> Unit) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    
    private var items: List<Product> = emptyList()
    
    fun submitList(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    class ViewHolder(val binding: ItemProductCardBinding) : RecyclerView.ViewHolder(binding.root)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = items[position]
        holder.binding.productName.text = product.name
        holder.binding.productPrice.text = "₹ ${product.price}"
        
        if (product.imageUri != null) {
            Glide.with(holder.itemView.context)
                .load(Uri.parse(product.imageUri))
                .placeholder(product.imageResId)
                .error(product.imageResId)
                .into(holder.binding.productIcon)
        } else {
            holder.binding.productIcon.setImageResource(product.imageResId)
        }
        
        holder.binding.root.setOnClickListener {
            onClick(product)
        }
    }
    
    override fun getItemCount() = items.size
}
