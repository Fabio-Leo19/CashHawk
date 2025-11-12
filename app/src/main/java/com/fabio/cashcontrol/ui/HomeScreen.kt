package com.fabio.cashcontrol.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fabio.cashcontrol.model.Transaction
import java.time.LocalDate
import com.fabio.cashcontrol.ui.components.PieChartView
import com.fabio.cashcontrol.ui.components.BarChartView

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    totalIncome: Double,
    totalExpense: Double,
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {

    /* ---------------------------
        ✅ FILTRO POR MÊS / ANO
    ----------------------------*/
    val now = LocalDate.now()

    var selectedMonth by remember { mutableStateOf(now.monthValue) }
    var selectedYear by remember { mutableStateOf(now.year) }

    var expandedMonth by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }

    val months = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )

    val years = (now.year downTo now.year - 10).toList()

    // ✅ Filtra result
    val filtered = transactions.filter {
        it.date.year == selectedYear && it.date.monthValue == selectedMonth
    }

    val totals = filtered.sumOf {
        if (it.type.name == "INCOME") it.value else 0.0
    } to filtered.sumOf {
        if (it.type.name == "EXPENSE") it.value else 0.0
    }

    val income = totals.first
    val expense = totals.second
    val balance = income - expense


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Resumo", style = MaterialTheme.typography.titleLarge)

        /* ---------------------------
            ✅ SELECTOR MÊS / ANO
        ----------------------------*/
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            // ✅ MÊS
            ExposedDropdownMenuBox(
                expanded = expandedMonth,
                onExpandedChange = { expandedMonth = !expandedMonth }
            ) {
                OutlinedTextField(
                    value = months[selectedMonth - 1],
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .weight(1f),
                    label = { Text("Mês") },
                )

                ExposedDropdownMenu(
                    expanded = expandedMonth,
                    onDismissRequest = { expandedMonth = false }
                ) {
                    months.forEachIndexed { index, name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                selectedMonth = index + 1
                                expandedMonth = false
                            }
                        )
                    }
                }
            }

            // ✅ ANO
            ExposedDropdownMenuBox(
                expanded = expandedYear,
                onExpandedChange = { expandedYear = !expandedYear }
            ) {
                OutlinedTextField(
                    value = selectedYear.toString(),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .weight(1f),
                    label = { Text("Ano") }
                )

                ExposedDropdownMenu(
                    expanded = expandedYear,
                    onDismissRequest = { expandedYear = false }
                ) {
                    years.forEach { y ->
                        DropdownMenuItem(
                            text = { Text(y.toString()) },
                            onClick = {
                                selectedYear = y
                                expandedYear = false
                            }
                        )
                    }
                }
            }
        }

        /* ---------------------------
            ✅ RESUMO
        ----------------------------*/
        Text("Receitas: R$ %.2f".format(income), style = MaterialTheme.typography.bodyLarge)
        Text("Despesas: R$ %.2f".format(expense), style = MaterialTheme.typography.bodyLarge)
        Text("Saldo: R$ %.2f".format(balance), style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(12.dp))

        /* ---------------------------
            ✅ GRÁFICOS
        ----------------------------*/
        Text("Distribuição de Despesas", style = MaterialTheme.typography.titleMedium)
        PieChartView(
            transactions = filtered,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Receitas vs Despesas", style = MaterialTheme.typography.titleMedium)
        BarChartView(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            income = income,
            expense = expense
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Transações do período", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(filtered) { transaction ->
                TransactionItem(transaction)
                Divider()
            }
        }
    }
}
