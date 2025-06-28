package com.example.projectplanner.ui.envelop

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectplanner.R
import com.example.projectplanner.databinding.FragmentEnvelopBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EnvelopFragment : Fragment() {

    private var _binding: FragmentEnvelopBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EnvelopViewModel by viewModels()

    private val calendar = Calendar.getInstance()
    private var startDate = calendar.time
    private var endDate = calendar.time
    private var expenseDate = calendar.time
    private var receiptUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEnvelopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupDateButtons()
        setupExpenseList()

        viewModel.loadExpenses(startDate, endDate)
        binding.btnAddExpense.setOnClickListener { showAddExpenseDialog() }
    }

    private fun setupObservers() {
        viewModel.budgetData.observe(viewLifecycleOwner) { budget ->
            updateBudgetUI(budget)
        }

        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            (binding.rvExpenses.adapter as ExpenseAdapter).updateExpenses(expenses)
        }
    }

    private fun setupDateButtons() {
        updateDateButtonText()
        binding.btnStartDate.setOnClickListener { showDatePicker(true) }
        binding.btnEndDate.setOnClickListener { showDatePicker(false) }
    }

    private fun updateDateButtonText() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.btnStartDate.text = dateFormat.format(startDate)
        binding.btnEndDate.text = dateFormat.format(endDate)
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
            val selectedDate = calendar.time

            if (isStartDate) {
                startDate = selectedDate
                if (startDate.after(endDate)) endDate = startDate
            } else {
                endDate = selectedDate
                if (endDate.before(startDate)) startDate = endDate
            }

            updateDateButtonText()
            viewModel.loadExpenses(startDate, endDate)
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupExpenseList() {
        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ExpenseAdapter(emptyList())
        }
    }

    private fun updateBudgetUI(budget: EnvelopViewModel.BudgetData) {
        binding.tvMonthlyIncome.text = formatCurrency(budget.monthlyIncome)
        binding.tvSavingsGoal.text = formatCurrency(budget.savingsGoal)
        binding.tvAmountSpent.text = formatCurrency(budget.amountSpent)
        binding.tvAmountLeft.text = formatCurrency(budget.amountLeft)

        val progress = (budget.amountSpent / budget.monthlyIncome).coerceIn(0f, 1f)
        binding.progressBar.progress = (progress * 100).toInt()

        val progressColor = when {
            progress > 0.9f -> R.color.expense_red
            progress > 0.7f -> R.color.orange
            else -> R.color.progress_indicator
        }
        binding.progressBar.setIndicatorColor(
            ContextCompat.getColor(requireContext(), progressColor)
        )
    }

    private fun formatCurrency(amount: Float) = "R${"%.2f".format(amount)}"

    @SuppressLint("MissingInflatedId")
    private fun showAddExpenseDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_expense, null)
        dialog.setContentView(view)
        dialog.show()

        val etExpenseName = view.findViewById<TextInputEditText>(R.id.etExpenseName)
        val etAmount = view.findViewById<TextInputEditText>(R.id.etAmount)
        val etCategory = view.findViewById<TextInputEditText>(R.id.etCategory)
        val tilCategory = view.findViewById<TextInputLayout>(R.id.tilCategory)
        val etDate = view.findViewById<TextInputEditText>(R.id.etDate)
        val tilDate = view.findViewById<TextInputLayout>(R.id.tilDate)
        val ivReceiptPreview = view.findViewById<ImageView>(R.id.ivReceiptPreview)
        val btnCapture = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCaptureImage)
        val btnSelect = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSelectImage)

        // Initialize with current date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        etDate.setText(dateFormat.format(expenseDate))

        // Category selection
        tilCategory.setOnClickListener {
            viewModel.categories.value?.let { categories ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Select Category")
                    .setItems(categories.toTypedArray()) { _, which ->
                        etCategory.setText(categories[which])
                    }
                    .show()
            }
        }

        // Date selection
        tilDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Expense Date")
                .setSelection(expenseDate.time)
                .build()

            datePicker.addOnPositiveButtonClickListener { selectedDate ->
                expenseDate = Date(selectedDate)
                etDate.setText(dateFormat.format(expenseDate))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        // Create launchers here to access the local ivReceiptPreview
        val takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && receiptUri != null) {
                ivReceiptPreview.setImageURI(receiptUri)
            }
        }

        val pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                receiptUri = it
                ivReceiptPreview.setImageURI(it)
            }
        }

        // Create permission launcher inside the dialog
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                captureImage(takePictureLauncher)
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        // Image capture
        btnCapture.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                captureImage(takePictureLauncher)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        // Image selection
        btnSelect.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Action buttons
        view.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<View>(R.id.btnAdd).setOnClickListener {
            val name = etExpenseName.text.toString().trim()
            val amountStr = etAmount.text.toString().trim()
            val category = etCategory.text.toString().trim()

            if (name.isEmpty() || amountStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = try {
                amountStr.toFloat()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = EnvelopViewModel.Expense(
                id = UUID.randomUUID().toString(),
                name = name,
                category = category,
                amount = amount,
                date = expenseDate,
                imageUri = receiptUri?.toString()
            )

            viewModel.addExpense(expense)
            Toast.makeText(requireContext(), "Expense added", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    private fun captureImage(takePictureLauncher: ActivityResultLauncher<Uri>) {
        val photoFile = createImageFile()
        receiptUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )
        takePictureLauncher.launch(receiptUri!!)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class ExpenseAdapter(private var expenses: List<EnvelopViewModel.Expense>) :
        RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvCategory)
            val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
            val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        }

        fun updateExpenses(newExpenses: List<EnvelopViewModel.Expense>) {
            expenses = newExpenses
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            expenses[position].run {
                holder.tvName.text = "$category: $name"
                holder.tvAmount.text = formatCurrency(amount)
                holder.tvDate.text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
            }
        }

        override fun getItemCount() = expenses.size
    }
}