package com.fabio.cashcontrol.ui

import android.content.Intent
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.fabio.cashcontrol.data.SettingsStore
import com.fabio.cashcontrol.data.io.CSVUtil
import com.fabio.cashcontrol.model.Category
import com.fabio.cashcontrol.model.Transaction
import com.fabio.cashcontrol.model.TransactionType
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

enum class SortBy { DATE, VALUE }
enum class SortDir { DESC, ASC }

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (Transaction) -> Unit
) {

    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Limite mensal
    val savedLimit by SettingsStore.getMonthlyLimit(context)
        .collectAsState(initial = null)

    var monthlyLimit by remember { mutableStateOf(savedLimit) }

    // Filtros de data
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

    // Filtros de tipo / categoria
    var typeFilter: TransactionType? by remember { mutableStateOf(null) }
    val categories = Category.values().toList()
    var categoryFilter: Category? by remember { mutableStateOf(null) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var query by remember { mutableStateOf("") }

    // Ordenação
    var sortBy by remember { mutableStateOf(SortBy.DATE) }
    var sortDir by remember { mutableStateOf(SortDir.DESC) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Total de despesas do mês
    val monthlyExpense = transactions
        .filter { it.date.monthValue == selectedMonth && it.date.year == selectedYear && it.type == TransactionType.EXPENSE }
        .sumOf { it.value }

    val overLimit = monthlyLimit?.let { monthlyExpense > it } ?: false

    // Aplicar filtros na lista
    val filtered = transactions
        .filter { it.date.year == selectedYear && it.date.monthValue == selectedMonth }
        .filter { typeFilter?.let { t -> it.type == t } ?: true }
        .filter { categoryFilter?.let { c -> it.category == c } ?: true }
        .filter {
            if (query.isBlank()) true
            else {
                val q = query.trim().lowercase()
                it.description.lowercase().contains(q) ||
                        it.category.label.lowercase().contains(q)
            }
        }
        .let { list ->
            when (sortBy) {
                SortBy.DATE -> {
                    val sorted = list.sortedBy { it.date }
                    if (sortDir == SortDir.DESC) sorted.reversed() else sorted
                }
                SortBy.VALUE -> {
                    val sorted = list.sortedBy { it.value }
                    if (sortDir == SortDir.DESC) sorted.reversed() else sorted
                }
            }
        }

    val totalsIncome = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.value }
    val totalsExpense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.value }
    val totalsBalance = totalsIncome - totalsExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
            }

            Spacer(Modifier.width(8.dp))

            Text(
                "Histórico de Transações",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.weight(1f))

            IconButton(onClick = {

                val csv = CSVUtil.export(filtered)

                val downloads = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )

                val file = File(downloads, "transacoes.csv")
                file.writeText(csv)

                val uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    file
                )

                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(
                    Intent.createChooser(share, "Exportar CSV")
                )

            }) {
                Icon(Icons.Default.Share, contentDescription = "Exportar CSV")
            }
        }

        // Filtros de Mês e Ano
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

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
                    label = { Text("Mês") }
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

        // Filtro por tipo (Todos / Receitas / Despesas)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = typeFilter == null,
                onClick = { typeFilter = null },
                label = { Text("Todos") }
            )
            FilterChip(
                selected = typeFilter == TransactionType.INCOME,
                onClick = { typeFilter = TransactionType.INCOME },
                label = { Text("Receitas") }
            )
            FilterChip(
                selected = typeFilter == TransactionType.EXPENSE,
                onClick = { typeFilter = TransactionType.EXPENSE },
                label = { Text("Despesas") }
            )
        }

        // Filtro por categoria
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                value = categoryFilter?.label ?: "Todas as categorias",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                label = { Text("Categoria") }
            )

            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Todas") },
                    onClick = {
                        categoryFilter = null
                        categoryExpanded = false
                    }
                )

                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.label) },
                        onClick = {
                            categoryFilter = cat
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // Busca + Ordenação
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar…") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Search
                )
            )

            Box {

                OutlinedButton(onClick = { sortMenuExpanded = true }) {

                    val label = when (sortBy) {
                        SortBy.DATE -> "Data"
                        SortBy.VALUE -> "Valor"
                    } + when (sortDir) {
                        SortDir.DESC -> " ↓"
                        SortDir.ASC -> " ↑"
                    }

                    Text(label)
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {

                    DropdownMenuItem(
                        text = { Text("Data ↓") },
                        onClick = {
                            sortBy = SortBy.DATE
                            sortDir = SortDir.DESC
                            sortMenuExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Data ↑") },
                        onClick = {
                            sortBy = SortBy.DATE
                            sortDir = SortDir.ASC
                            sortMenuExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Valor ↓") },
                        onClick = {
                            sortBy = SortBy.VALUE
                            sortDir = SortDir.DESC
                            sortMenuExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Valor ↑") },
                        onClick = {
                            sortBy = SortBy.VALUE
                            sortDir = SortDir.ASC
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }

        // Limite mensal
        OutlinedTextField(
            value = monthlyLimit?.toString() ?: "",
            onValueChange = {
                monthlyLimit = it.toDoubleOrNull()
                scope.launch {
                    SettingsStore.setMonthlyLimit(context, monthlyLimit)
                }
            },
            label = { Text("Limite mensal (R$)") },
            modifier = Modifier.fillMaxWidth()
        )

        if (overLimit) {
            Text(
                "⚠ Gastos excederam o limite!",
                color = MaterialTheme.colorScheme.error
            )
        }

        Text("Receitas: R$ %.2f".format(totalsIncome))
        Text("Despesas: R$ %.2f".format(totalsExpense))
        Text("Saldo: R$ %.2f".format(totalsBalance))

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {

            items(filtered, key = { it.id }) { tx ->

                val dismissState =
                    rememberDismissState(
                        confirmStateChange = { newValue ->
                            when (newValue) {

                                DismissValue.DismissedToStart -> {
                                    onDelete(tx)
                                    true
                                }

                                DismissValue.DismissedToEnd -> {
                                    onEdit(tx.id)
                                    false
                                }

                                else -> false
                            }
                        }
                    )

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(
                        DismissDirection.StartToEnd,
                        DismissDirection.EndToStart
                    ),
                    background = {
                        val direction = dismissState.dismissDirection
                        val (bg, icon, text) = when (direction) {

                            DismissDirection.StartToEnd ->
                                Triple(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    Icons.Default.Edit,
                                    "Editar"
                                )

                            DismissDirection.EndToStart ->
                                Triple(
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                    Icons.Default.Delete,
                                    "Excluir"
                                )

                            else -> Triple(Color.Transparent, null, "")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .background(bg)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (direction == DismissDirection.StartToEnd && icon != null) {
                                    Icon(icon, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(text)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (direction == DismissDirection.EndToStart && icon != null) {
                                    Text(text)
                                    Spacer(Modifier.width(8.dp))
                                    Icon(icon, contentDescription = null)
                                }
                            }
                        }
                    },
                    dismissContent = {
                        TransactionItem(
                            transaction = tx,
                            onClick = { onEdit(tx.id) }
                        )
                    }
                )

                Divider()
            }
        }

        SnackbarHost(hostState = snackbarHost)
    }
}
