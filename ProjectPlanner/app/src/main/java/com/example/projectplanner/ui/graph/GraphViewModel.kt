package com.example.projectplanner.ui.graph

import android.icu.text.SimpleDateFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projectplanner.R
import java.util.*

class GraphViewModel : ViewModel() {

    // Define data classes
    data class MonthlyExpense(val month: String, val amount: Float)
    data class CategoryTotal(val name: String, val amount: Float, val color: Int)
    data class Badge(val name: String, val iconRes: Int, val description: String)

    // LiveData properties
    private val _monthlyExpenses = MutableLiveData<List<MonthlyExpense>>()
    val monthlyExpenses: LiveData<List<MonthlyExpense>> = _monthlyExpenses

    private val _categoryTotals = MutableLiveData<List<CategoryTotal>>()
    val categoryTotals: LiveData<List<CategoryTotal>> = _categoryTotals

    private val _badges = MutableLiveData<List<Badge>>()
    val badges: LiveData<List<Badge>> = _badges

    private val _dateRange = MutableLiveData<Pair<Date, Date>>()
    val dateRange: LiveData<Pair<Date, Date>> = _dateRange

    private val _budgetGoals = MutableLiveData<Pair<Int, Int>>()
    val budgetGoals: LiveData<Pair<Int, Int>> = _budgetGoals

    // Initialize with default values
    init {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.MONTH, -5) // Last 6 months
        val startDate = calendar.time

        _dateRange.value = Pair(startDate, endDate)
        loadData(startDate, endDate)
    }

    fun updateDateRange(startDate: Date, endDate: Date) {
        _dateRange.value = Pair(startDate, endDate)
        loadData(startDate, endDate)
    }

    private fun loadData(startDate: Date, endDate: Date) {
        // Generate mock data - replace with your actual data source
        _monthlyExpenses.value = generateMonthlyExpenses(startDate, endDate)
        _categoryTotals.value = generateCategoryTotals()
        _badges.value = generateBadges()
        _budgetGoals.value = Pair(500, 1000) // Min and max goals
    }

    private fun generateMonthlyExpenses(startDate: Date, endDate: Date): List<MonthlyExpense> {
        val months = getMonthsBetweenDates(startDate, endDate)
        return months.mapIndexed { index, month ->
            MonthlyExpense(month, (300 + index * 100).toFloat())
        }
    }

    private fun getMonthsBetweenDates(startDate: Date, endDate: Date): List<String> {
        val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val months = mutableListOf<String>()

        val startCal = Calendar.getInstance().apply { time = startDate }
        val endCal = Calendar.getInstance().apply { time = endDate }

        while (startCal.before(endCal) || startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)) {
            months.add(dateFormat.format(startCal.time))
            startCal.add(Calendar.MONTH, 1)
        }

        return months
    }

    private fun generateCategoryTotals(): List<CategoryTotal> {
        return listOf(
            CategoryTotal("Food", 400f, R.color.food_color),
            CategoryTotal("Transport", 200f, R.color.transport_color),
            CategoryTotal("Bills", 300f, R.color.bills_color),
            CategoryTotal("Entertainment", 150f, R.color.entertainment_color),
            CategoryTotal("Shopping", 350f, R.color.shopping_color)
        )
    }

    private fun generateBadges(): List<Badge> {
        return listOf(
            Badge("Goal Master", R.drawable.ic_trophy, "Achieved savings goal 3 months in a row"),
            Badge("Consistent Logger", R.drawable.ic_badge, "Logged expenses for 30 consecutive days"),
            Badge("Budget Pro", R.drawable.ic_star, "Stayed under budget for 2 months"),
            Badge("Savings Champion", R.drawable.ic_medal, "Saved 20% of income for 3 months")
        )
    }
}