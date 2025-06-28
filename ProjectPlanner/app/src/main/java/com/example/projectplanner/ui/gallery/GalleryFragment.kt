package com.example.projectplanner.ui.gallery

import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectplanner.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import java.util.concurrent.TimeUnit

class GalleryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var fullscreenContainer: View
    private lateinit var fullscreenImage: ImageView
    private lateinit var closeFullscreen: ImageButton

    private val galleryItems = mutableListOf<GalleryItem>()
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvGallery)
        emptyState = view.findViewById(R.id.emptyState)
        fullscreenContainer = view.findViewById(R.id.fullscreenContainer)
        fullscreenImage = view.findViewById(R.id.ivFullscreen)
        closeFullscreen = view.findViewById(R.id.btnCloseFullscreen)

        setupRecyclerView()
        setupChipGroup(view.findViewById(R.id.chipGroupFilters))
        setupSearch(view.findViewById(R.id.etSearch))

        closeFullscreen.setOnClickListener { hideFullscreen() }

        // Check permissions and load images
        if (hasStoragePermission()) {
            loadGalleryImages()
        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadGalleryImages()
        } else {
            Toast.makeText(
                requireContext(),
                "Permission needed to access photos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun loadGalleryImages() {
        galleryItems.clear()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateTaken = cursor.getLong(dateColumn)
                val name = cursor.getString(nameColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                galleryItems.add(GalleryItem(id, contentUri, dateTaken, name))
            }
        }

        updateAdapter()
        updateEmptyState()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = ReceiptAdapter(emptyList()) { item ->
            showFullscreen(item.uri)
        }
    }

    private fun updateAdapter() {
        (recyclerView.adapter as ReceiptAdapter).updateItems(galleryItems)
    }

    private fun updateEmptyState() {
        emptyState.visibility = if (galleryItems.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupChipGroup(chipGroup: ChipGroup) {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedChips = group.checkedChipIds.map { id ->
                group.findViewById<Chip>(id).text.toString()
            }
            filterReceipts(selectedChips)
        }
    }

    private fun setupSearch(searchField: TextInputEditText) {
        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                filterReceiptsByText(s.toString())
            }
        })
    }

    private fun filterReceipts(selectedFilters: List<String>) {
        val filteredItems = if (selectedFilters.isEmpty() || selectedFilters.contains("All")) {
            galleryItems
        } else {
            val now = System.currentTimeMillis()
            galleryItems.filter { item ->
                selectedFilters.any { filter ->
                    when (filter) {
                        "This Month" -> now - item.dateTaken < TimeUnit.DAYS.toMillis(30)
                        "Last 3 Months" -> now - item.dateTaken < TimeUnit.DAYS.toMillis(90)
                        else -> true
                    }
                }
            }
        }
        (recyclerView.adapter as ReceiptAdapter).updateItems(filteredItems)
        updateEmptyState()
    }

    private fun filterReceiptsByText(query: String) {
        val filteredItems = if (query.isBlank()) {
            galleryItems
        } else {
            galleryItems.filter {
                it.name?.contains(query, ignoreCase = true) == true
            }
        }
        (recyclerView.adapter as ReceiptAdapter).updateItems(filteredItems)
        updateEmptyState()
    }

    private fun showFullscreen(uri: Uri) {
        fullscreenImage.setImageURI(uri)
        fullscreenContainer.visibility = View.VISIBLE
    }

    private fun hideFullscreen() {
        fullscreenContainer.visibility = View.GONE
    }

    data class GalleryItem(
        val id: Long,
        val uri: Uri,
        val dateTaken: Long,
        val name: String?
    )

    class ReceiptAdapter(
        initialItems: List<GalleryItem>,
        private val onClick: (GalleryItem) -> Unit
    ) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>() {

        private val items = mutableListOf<GalleryItem>()

        init {
            items.addAll(initialItems)
        }

        fun updateItems(newItems: List<GalleryItem>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.receipt_image)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_receipt, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.imageView.setImageURI(item.uri)
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}