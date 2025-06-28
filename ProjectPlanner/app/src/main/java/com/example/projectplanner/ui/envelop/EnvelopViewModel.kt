package com.example.projectplanner.ui.envelop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class EnvelopViewModel : ViewModel() {
    data class BudgetData(
        val monthlyIncome: Float,
        val savingsGoal: Float,
        val amountSpent: Float,
        val amountLeft: Float
    )

    data class Expense(
        val id: String,
        val name: String,
        val category: String,
        val amount: Float,
        val date: Date,
        val imageUri: String? = null
    )

    private val _budgetData = MutableLiveData<BudgetData>()
    val budgetData: LiveData<BudgetData> = _budgetData

    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    // Master list of expenses
    private val allExpenses = mutableListOf<Expense>()
    private var currentStartDate = Date()
    private var currentEndDate = Date()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Mock data
        _categories.value = listOf(
            "Groceries", "Transport", "Entertainment",
            "Utilities", "Shopping", "Dining", "Healthcare", "Other"
        )

        val mockData = BudgetData(
            monthlyIncome = 5000f,
            savingsGoal = 1000f,
            amountSpent = 2500f,
            amountLeft = 2500f
        )
        _budgetData.value = mockData

        // Generate mock expenses
        val calendar = Calendar.getInstance()
        val mockExpenses = mutableListOf<Expense>()
        for (i in 1..15) {
            calendar.add(Calendar.DAY_OF_MONTH, -i)
            mockExpenses.add(
                Expense(
                    id = UUID.randomUUID().toString(),
                    name = "Expense $i",
                    category = _categories.value!!.random(),
                    amount = (100..500).random().toFloat(),
                    date = calendar.time,
                    imageUri = if (i % 3 == 0) "content://mock/image$i" else null
                )
            )
        }
        allExpenses.addAll(mockExpenses)
        _expenses.value = allExpenses
    }

    fun loadBudgetData() {
        // Calculate based on current expenses
        val amountSpent = _expenses.value?.sumOf { it.amount.toDouble() }?.toFloat() ?: 0f
        val currentBudget = _budgetData.value ?: BudgetData(0f, 0f, 0f, 0f)

        _budgetData.value = currentBudget.copy(
            amountSpent = amountSpent,
            amountLeft = currentBudget.monthlyIncome - amountSpent
        )
    }

    fun loadExpenses(startDate: Date, endDate: Date) {
        currentStartDate = startDate
        currentEndDate = endDate

        val filtered = allExpenses.filter { expense ->
            expense.date.time in startDate.time..endDate.time
        }
        _expenses.value = filtered
        loadBudgetData()
    }

    fun addExpense(expense: Expense) {
        allExpenses.add(expense)
        loadExpenses(currentStartDate, currentEndDate)
    }
}