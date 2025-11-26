package com.fabio.cashcontrol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fabio.cashcontrol.model.Category
import com.fabio.cashcontrol.model.Transaction
import com.fabio.cashcontrol.model.TransactionType
import java.time.LocalDate

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState

private val ScreenBg = Color(0xFF1E1F26)
private val CardBg = Color(0xFF292929)
private val GoldAccent = Color(0xFFD4A048)

private val CashHawkGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFFCBA135),
        Color(0xFFFFFFFF),
        Color(0xFFD5AB44),
        Color(0xFFFFFFFF),
        Color(0xFFD5AB44),
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    id: String,
    transaction: Transaction?,
    onSave: (Transaction) -> Unit,
    onCancel: () -> Unit,
    onDelete: (Transaction) -> Unit
) {
    if (transaction == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(ScreenBg),
            contentAlignment = Alignment.Center
        ) {
            Text("Carregando…", color = Color.White)
        }
        return
    }

    var description by remember { mutableStateOf(transaction.description) }
    var valueText by remember { mutableStateOf(transaction.value.toString()) }
    var type by remember { mutableStateOf(transaction.type) }
    var selectedDate by remember { mutableStateOf(transaction.date) }

    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var categoryExpanded by remember { mutableStateOf(false) }
    val categories = Category.values().toList()

    val valueDouble = valueText.replace(",", ".").toDoubleOrNull()
    val isValueError = valueText.isNotBlank() && valueDouble == null
    val canSave = description.isNotBlank() && valueDouble != null && valueDouble > 0

    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = dateState.selectedDateMillis
                    if (millis != null) selectedDate = millis.toLocalDate()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                "Editar Transação",
                style = MaterialTheme.typography.titleLarge,
                color = GoldAccent
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedColorsEdit()
                    )

                    OutlinedTextField(
                        value = valueText,
                        onValueChange = { valueText = it },
                        label = { Text("Valor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = isValueError,
                        colors = outlinedColorsEdit()
                    )

                    if (isValueError) {
                        Text("Valor inválido", color = MaterialTheme.colorScheme.error)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = type == TransactionType.INCOME,
                            onClick = { type = TransactionType.INCOME },
                            label = { Text("Receita") }
                        )
                        FilterChip(
                            selected = type == TransactionType.EXPENSE,
                            onClick = { TransactionType.EXPENSE.also { type = it } },
                            label = { Text("Despesa") }
                        )
                    }

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {

                        OutlinedTextField(
                            value = selectedCategory.label,
                            readOnly = true,
                            onValueChange = {},
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            label = { Text("Categoria") },
                            colors = outlinedColorsEdit()
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

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GoldAccent
                        )
                    ) {
                        Text("Data: ${selectedDate}")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Salvar (gradiente)
            Button(
                onClick = {
                    val updated = transaction.copy(
                        description = description,
                        value = valueDouble ?: transaction.value,
                        category = selectedCategory,
                        type = type,
                        date = selectedDate
                    )
                    onSave(updated)
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color(0xFF3A3A3A)
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (canSave) CashHawkGradient else Brush.verticalGradient(
                                listOf(Color(0xFF3A3A3A), Color(0xFF3A3A3A))
                            ),
                            shape = MaterialTheme.shapes.large
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Salvar alterações",
                        color = if (canSave) Color.Black else Color(0xFF777777),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Excluir
            var showDeleteDialog by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
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
                                showDeleteDialog = false
                                onDelete(transaction)
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

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Cancelar")
            }
        }
    }
}

private fun Long.toLocalDate(): LocalDate =
    java.time.Instant.ofEpochMilli(this)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()

@Composable
private fun outlinedColorsEdit() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GoldAccent,
    unfocusedBorderColor = Color(0x55FFFFFF),
    focusedLabelColor = GoldAccent,
    unfocusedLabelColor = Color(0x88FFFFFF),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
