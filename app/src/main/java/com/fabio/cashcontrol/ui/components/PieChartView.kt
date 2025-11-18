package com.fabio.cashcontrol.ui.components

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.fabio.cashcontrol.model.Transaction
import com.fabio.cashcontrol.model.TransactionType
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

@Composable
fun PieChartView(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {

    AndroidView(
        modifier = modifier,
        factory = { context ->

            PieChart(context).apply {

                val expenseByCategory = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.category }
                    .mapValues { (_, list) -> list.sumOf { it.value } }

                if (expenseByCategory.isEmpty()) {
                    this.clear()
                    this.invalidate()
                    return@AndroidView this
                }

                val entries = expenseByCategory.map { (category, totalValue) ->
                    PieEntry(
                        totalValue.toFloat(),
                        category.label
                    )
                }

                val colors = expenseByCategory.keys.map {
                    val hash = it.name.hashCode()
                    Color.rgb(
                        100 + (hash shr 16 and 0x7F),
                        100 + (hash shr 8 and 0x7F),
                        100 + (hash and 0x7F)
                    )
                }

                val dataSet = PieDataSet(entries, "Gastos por Categoria")
                dataSet.colors = colors

                val total = expenseByCategory.values.sum()

                val pieData = PieData(dataSet).apply {
                    setValueTextSize(12f)
                    setValueFormatter(
                        object : com.github.mikephil.charting.formatter.ValueFormatter() {
                            override fun getFormattedValue(value: Float):
                                    String {
                                val percent = (value / total * 100)
                                return "R$ %.2f (%.1f%%)".format(value, percent)
                            }
                        }
                    )

                }

                this.data = pieData

                this.setUsePercentValues(true)
                this.description.isEnabled = false

                this.setDrawEntryLabels(true)
                this.setEntryLabelColor(Color.BLACK)
                this.setEntryLabelTextSize(12f)

                this.legend.isEnabled = true

                this.invalidate()
            }
        }
    )
}
