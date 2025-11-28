package com.fabio.cashcontrol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fabio.cashcontrol.model.Transaction
import com.fabio.cashcontrol.ui.components.BarChartView
import com.fabio.cashcontrol.ui.components.PieChartView
import java.time.LocalDate

// Cores base do tema
private val DarkBackground = Color(0xFF1E1F26)
private val CardBackground = Color(0xFF292929)
private val Gold = Color(0xFFD4A048)

private val CashHawkGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFFCBA135),
        Color(0xFFFFFFFF),
        Color(0xFFD5AB44),
        Color(0xFFFFFFFF),
        Color(0xFFD5AB44),
    )
)

/* -----------------------------------------------------------
   VERSÃO QUE USA O VIEWMODEL
------------------------------------------------------------ */
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onAddClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    HomeScreen(
        totalIncome = state.totalIncome,
        totalExpense = state.totalExpense,
        transactions = state.transactions,
        onAddClick = onAddClick,
        onHistoryClick = onHistoryClick,
        modifier = modifier
    )
}

/* -----------------------------------------------------------
   UI PRINCIPAL
------------------------------------------------------------ */
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

    val filtered = transactions.filter {
        it.date.year == selectedYear && it.date.monthValue == selectedMonth
    }

    val income = filtered.filter { it.type.isIncome }.sumOf { it.value }
    val expense = filtered.filter { it.type.isExpense }.sumOf { it.value }
    val balance = income - expense

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Cabeçalho “CashHawk style”
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = CardBackground,
                        shape = MaterialTheme.shapes.large
                    )
                    .padding(16.dp)
            ) {
                Text(
                    "Resumo",
                    style = MaterialTheme.typography.titleLarge,
                    color = Gold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Dê asas às suas finanças",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Mês
                    ExposedDropdownMenuBox(
                        expanded = expandedMonth,
                        onExpandedChange = { expandedMonth = !expandedMonth },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = months[selectedMonth - 1],
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            label = { Text("Mês") },
                            colors = textFieldColors()
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

                    // Ano
                    ExposedDropdownMenuBox(
                        expanded = expandedYear,
                        onExpandedChange = { expandedYear = !expandedYear },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedYear.toString(),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            label = { Text("Ano") },
                            colors = textFieldColors()
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
            }

            // Card de totais
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Receitas: R$ %.2f".format(income),
                        color = Color(0xFF9BE7FF),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Despesas: R$ %.2f".format(expense),
                        color = Color(0xFFFF9B9B),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Divider(
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                        color = Color(0x33FFFFFF)
                    )
                    Text(
                        "Saldo: R$ %.2f".format(balance),
                        color = if (balance >= 0) Gold else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Gráficos
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Distribuição de Despesas",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gold
                    )

                    PieChartView(
                        transactions = filtered,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )

                    Text(
                        "Receitas vs Despesas",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gold
                    )

                    BarChartView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        income = income,
                        expense = expense
                    )
                }
            }

            Text(
                "Transações do período",
                style = MaterialTheme.typography.titleMedium,
                color = Gold
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered, key = { it.id }) { tx ->
                    TransactionItem(
                        transaction = tx,
                        onClick = onHistoryClick // leva pra tela de histórico/edição
                    )
                }
            }

            // Botão de "ver histórico" com gradiente
            Button(
                onClick = onHistoryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CashHawkGradient, shape = MaterialTheme.shapes.large),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Ver histórico completo",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // FAB de adicionar transação
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Gold,
            contentColor = Color.Black
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Gold,
    unfocusedBorderColor = Color(0x55FFFFFF),
    focusedLabelColor = Gold,
    unfocusedLabelColor = Color(0x88FFFFFF),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
