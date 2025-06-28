package com.example.projectplanner.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.PieData
import androidx.core.graphics.toColorInt

class GalleryViewModel : ViewModel() {

    private val _barChartData = MutableLiveData<BarData>()
    val barChartData: LiveData<BarData> = _barChartData

    private val _pieChartData = MutableLiveData<PieData>()
    val pieChartData: LiveData<PieData> = _pieChartData

    private val _minGoal = MutableLiveData<String>()
    val minGoal: LiveData<String> = _minGoal

    private val _maxGoal = MutableLiveData<String>()
    val maxGoal: LiveData<String> = _maxGoal

    init {
        // Fake/mock data for testing
        loadChartData()
    }

    private fun loadChartData() {
        val barEntries = listOf(
            com.github.mikephil.charting.data.BarEntry(0f, 1200f),
            com.github.mikephil.charting.data.BarEntry(1f, 900f),
            com.github.mikephil.charting.data.BarEntry(2f, 1500f),
        )
        val barDataSet = com.github.mikephil.charting.data.BarDataSet(barEntries, "Monthly Spending")
        barDataSet.color = "#3F51B5".toColorInt()
        _barChartData.value = com.github.mikephil.charting.data.BarData(barDataSet)

        val pieEntries = listOf(
            com.github.mikephil.charting.data.PieEntry(500f, "Food"),
            com.github.mikephil.charting.data.PieEntry(300f, "Transport"),
            com.github.mikephil.charting.data.PieEntry(700f, "Housing"),
        )
        val pieDataSet = com.github.mikephil.charting.data.PieDataSet(pieEntries, "")
        pieDataSet.setColors(
            android.graphics.Color.GREEN,
            android.graphics.Color.BLUE,
            android.graphics.Color.MAGENTA
        )
        _pieChartData.value = com.github.mikephil.charting.data.PieData(pieDataSet)

        _minGoal.value = "R500"
        _maxGoal.value = "R1500"
    }
}
