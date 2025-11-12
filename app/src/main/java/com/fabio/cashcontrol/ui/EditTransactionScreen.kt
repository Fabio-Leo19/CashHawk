package com.fabio.cashcontrol.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fabio.cashcontrol.data.TransactionRepositoryRoom
import com.fabio.cashcontrol.model.Category
import com.fabio.cashcontrol.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    id: String,
    repo: TransactionRepositoryRoom,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    /* ----------------------------------------
       ✅ Busca transação
    -----------------------------------------*/
    val txList by repo.listAll().collectAsState(initial = emptyList())
    val tx = remember(txList, id) { txList.find { it.id == id } }

    if (tx == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Carregando…")
        }
        return
    }

    /* ----------------------------------------
       ✅ Estados
    -----------------------------------------*/
    var description by remember { mutableStateOf(tx.description) }
    var valueText by remember { mutableStateOf(tx.value.toString()) }
    var type by remember { mutableStateOf(tx.type) }
    var selectedDate by remember { mutableStateOf(tx.date) }

    var selectedCategory by remember { mutableStateOf(tx.category) }
    var categoryExpanded by remember { mutableStateOf(false) }
    val categories = Category.values().toList()

    val valueDouble = valueText.replace(",", ".").toDoubleOrNull()
    val isValueError = valueText.isNotBlank() && valueDouble == null
    val canSave = description.isNotBlank() && valueDouble != null && valueDouble > 0

    /* ----------------------------------------
       ✅ DatePicker
    -----------------------------------------*/
    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = dateState.selectedDateMillis
                    if (millis != null) {
                        selectedDate = millis.toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    /* ----------------------------------------
       ✅ UI
    -----------------------------------------*/
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text("Editar Transação", style = MaterialTheme.typography.headlineSmall)

            /* DESCRIÇÃO */
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth()
            )

            /* VALOR */
            OutlinedTextField(
                value = valueText,
                onValueChange = { valueText = it },
                label = { Text("Valor") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = isValueError
            )

            if (isValueError) {
                Text(
                    "Valor inválido",
                    color = MaterialTheme.colorScheme.error
                )
            }

            /* TIPO */
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == TransactionType.INCOME,
                    onClick = { type = TransactionType.INCOME },
                    label = { Text("Receita") }
                )
                FilterChip(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { type = TransactionType.EXPENSE },
                    label = { Text("Despesa") }
                )
            }

            /* CATEGORIA */
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {

                OutlinedTextField(
                    value = selectedCategory.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.label) },
                            onClick = {
                                selectedCategory = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            /* DATA */
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Data: ${selectedDate.toFormatted()}")
            }

            Spacer(Modifier.height(16.dp))

            /* ✅ Salvar */
            Button(
                onClick = {
                    val updated = tx.copy(
                        description = description,
                        value = valueDouble ?: tx.value,
                        category = selectedCategory,
                        type = type,
                        date = selectedDate     // ✅ mantém a escolha do usuário
                    )
                    scope.launch(Dispatchers.IO) {
                        repo.add(updated)
                    }
                    onSave()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar alterações")
            }

            /* ✅ Excluir */
            var showDeleteDialog by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Excluir")
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Excluir transação") },
                    text = { Text("Tem certeza que deseja excluir esta transação?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    repo.deleteById(tx.id)
                                }
                                showDeleteDialog = false
                                onSave()
                                onDelete()
                            }
                        ) {
                            Text("Sim", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            /* ✅ Cancelar */
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}

/* -----------------------
   Helpers
------------------------*/

fun Long.toLocalDate(): LocalDate =
    java.time.Instant.ofEpochMilli(this)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()

fun LocalDate.toFormatted() = this.toString()
