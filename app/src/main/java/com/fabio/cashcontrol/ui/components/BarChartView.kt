package com.fabio.cashcontrol.ui.components

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * ✅ Exibe um gráfico de barras simples (Receitas x Despesas)
 *
 * @param income Valor total de receitas
 * @param expense Valor total de despesas
 */
@Composable
fun BarChartView(
    modifier: Modifier = Modifier,
    income: Double,
    expense: Double
) {

    AndroidView(
        modifier = modifier,
        factory = { context ->

            BarChart(context).apply {

                /* ✅ ENTRADAS DO GRÁFICO */
                val entries = listOf(
                    BarEntry(0f, income.toFloat()),
                    BarEntry(1f, expense.toFloat())
                )

                /* ✅ CONJUNTO DE DADOS */
                val dataSet = BarDataSet(entries, "Resumo").apply {
                    colors = listOf(
                        Color.rgb(76, 175, 80),   // Verde – Receitas
                        Color.rgb(244, 67, 54),   // Vermelho – Despesas
                    )
                    valueTextSize = 12f
                }

                data = BarData(dataSet)

                /* ✅ EIXO X */
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    valueFormatter = IndexAxisValueFormatter(
                        listOf("Receitas", "Despesas")
                    )
                }

                /* ✅ APARÊNCIA */
                axisRight.isEnabled = false
                description.isEnabled = false
                legend.isEnabled = true

                setFitBars(true)
                invalidate()
            }
        }
    )
}
