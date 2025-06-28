package com.example.projectplanner.ui.graph

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectplanner.R
import com.example.projectplanner.databinding.FragmentGraphBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart

    // Date handling
    private val calendar = Calendar.getInstance()
    private var startDate = Date()
    private var endDate = Date()

    // Data classes
    data class CategoryTotal(val category: String, val total: Int)
    data class Badge(val name: String, val iconRes: Int, val description: String)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize charts
        barChart = binding.barChart
        pieChart = binding.pieChart

        // Setup date buttons with current dates
        setupDateButtons()

        // Setup quick select buttons
        setupQuickSelectButtons()

        // Setup recycler views
        setupRecyclerViews()

        // Load and display chart data
        loadData()
    }

    private fun setupDateButtons() {
        // Format dates for display
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.btnStartDate.text = dateFormat.format(startDate)
        binding.btnEndDate.text = dateFormat.format(endDate)

        // Set click listeners for date pickers
        binding.btnStartDate.setOnClickListener { showDatePicker(true) }
        binding.btnEndDate.setOnClickListener { showDatePicker(false) }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
            val selectedDate = calendar.time

            if (isStartDate) {
                startDate = selectedDate
                // Ensure start date is before end date
                if (startDate.after(endDate)) {
                    endDate = startDate
                    binding.btnEndDate.text = binding.btnStartDate.text
                }
            } else {
                endDate = selectedDate
                // Ensure end date is after start date
                if (endDate.before(startDate)) {
                    startDate = endDate
                    binding.btnStartDate.text = binding.btnEndDate.text
                }
            }

            // Update buttons and reload data
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            if (isStartDate) binding.btnStartDate.text = dateFormat.format(startDate)
            else binding.btnEndDate.text = dateFormat.format(endDate)

            loadData()
        }

        // Show date picker dialog
        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupQuickSelectButtons() {
        binding.btnLast3Months.setOnClickListener { setDateRange(3) }
        binding.btnLast6Months.setOnClickListener { setDateRange(6) }
        binding.btnThisYear.setOnClickListener { setDateRange(12) }
    }

    private fun setDateRange(monthsBack: Int) {
        val calendar = Calendar.getInstance()
        endDate = calendar.time

        calendar.add(Calendar.MONTH, -monthsBack)
        startDate = calendar.time

        // Update buttons
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.btnStartDate.text = dateFormat.format(startDate)
        binding.btnEndDate.text = dateFormat.format(endDate)

        // Reload data
        loadData()
    }

    private fun setupRecyclerViews() {
        // Badges RecyclerView
        binding.rvBadges.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvBadges.adapter = BadgeAdapter(getBadges())
    }

    private fun loadData() {
        setupBarChart()
        setupPieChart()
        updateGoals()
    }

    private fun setupBarChart() {
        // Generate mock data based on date range
        val months = getMonthsBetweenDates()
        val entries = months.mapIndexed { index, _ ->
            BarEntry(index.toFloat(), (300 + index * 100).toFloat())
        }

        val barDataSet = BarDataSet(entries, "Monthly Expenses").apply {
            color = ContextCompat.getColor(requireContext(), R.color.progress_indicator)
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            setDrawValues(true)
        }

        val barData = BarData(barDataSet).apply {
            barWidth = 0.6f
            setValueFormatter(CurrencyValueFormatter())
        }

        barChart.apply {
            data = barData
            setFitBars(true)

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(months)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                labelCount = months.size
                axisMinimum = -0.5f
                axisMaximum = months.size - 0.5f
            }

            axisLeft.apply {
                axisMinimum = 0f
                addLimitLine(createLimitLine(getMinGoal().toFloat(), "Min Goal", Color.GREEN))
                addLimitLine(createLimitLine(getMaxGoal().toFloat(), "Max Goal", Color.RED))
                setDrawLimitLinesBehindData(true)
                gridColor = Color.LTGRAY
                axisLineColor = Color.DKGRAY
            }

            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            animateY(1000)
            extraBottomOffset = 10f
            invalidate()
        }
    }

    private fun getMonthsBetweenDates(): List<String> {
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

    private fun createLimitLine(limit: Float, label: String, color: Int): LimitLine {
        return LimitLine(limit, label).apply {
            lineColor = color
            lineWidth = 2f
            textColor = color
            textSize = 12f
            enableDashedLine(10f, 10f, 0f)
        }
    }

    private fun setupPieChart() {
        val entries = listOf(
            PieEntry(400f, "Food"),
            PieEntry(200f, "Transport"),
            PieEntry(300f, "Bills"),
            PieEntry(150f, "Entertainment"),
            PieEntry(350f, "Shopping")
        )

        val pieDataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            sliceSpace = 3f
            selectionShift = 5f
        }

        pieChart.apply {
            data = PieData(pieDataSet).apply {
                setValueFormatter(PercentFormatter(pieChart))
                setValueTextSize(12f)
            }

            setUsePercentValues(true)
            setDrawEntryLabels(false)
            setDrawHoleEnabled(true)
            holeRadius = 50f
            transparentCircleRadius = 55f
            centerText = "Spending"
            setCenterTextSize(14f)
            setCenterTextColor(Color.DKGRAY)
            description.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 12f
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            animateY(1000)
            invalidate()
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun updateGoals() {
        binding.tvMinGoal.text = getString(R.string.min_goal_format, getMinGoal())
        binding.tvMaxGoal.text = getString(R.string.max_goal_format, getMaxGoal())
    }

    private fun getBadges(): List<Badge> {
        return listOf(
            Badge("Goal Master", R.drawable.ic_trophy, "Achieved savings goal 3 months in a row"),
            Badge("Consistent Logger", R.drawable.ic_badge, "Logged expenses for 30 consecutive days"),
            Badge("Budget Pro", R.drawable.ic_star, "Stayed under budget for 2 months"),
            Badge("Savings Champion", R.drawable.ic_medal, "Saved 20% of income for 3 months")
        )
    }

    private fun getMinGoal(): Int {
        return 500
    }

    private fun getMaxGoal(): Int {
        return 1000
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapters
    inner class BadgeAdapter(private val badges: List<Badge>) :
        RecyclerView.Adapter<BadgeAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivBadge: ImageView = itemView.findViewById(R.id.ivBadge)
            val tvBadgeName: TextView = itemView.findViewById(R.id.tvBadgeName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_badge, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val badge = badges[position]
            holder.tvBadgeName.text = badge.name
            holder.ivBadge.setImageResource(badge.iconRes)

            // Add badge description as content description
            holder.itemView.contentDescription = "${badge.name}: ${badge.description}"
        }

        override fun getItemCount() = badges.size
    }

    private inner class CurrencyValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return "R${value.toInt()}"
        }
    }
}