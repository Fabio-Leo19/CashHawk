package com.fabio.cashcontrol.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fabio.cashcontrol.model.Category
import com.fabio.cashcontrol.model.Transaction
import com.fabio.cashcontrol.model.TransactionType
import java.time.LocalDate

// DatePicker (Material3)
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onSave: (Transaction) -> Unit,
    onCancel: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.INCOME) }

    // Categoria
    val categories = Category.values().toList()
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    // Data
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()

    // Validações
    val valueDouble = valueText.replace(",", ".").toDoubleOrNull()
    val isValueError = valueText.isNotBlank() && valueDouble == null
    val canSave = description.isNotBlank() && (valueDouble != null && valueDouble > 0)

    // DatePicker
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = dateState.selectedDateMillis
                        if (millis != null) {
                            selectedDate = millisToLocalDate(millis)
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Nova Transação", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = valueText,
            onValueChange = { valueText = it },
            label = { Text("Valor") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = isValueError
        )

        if (isValueError) {
            Text(
                "Valor inválido",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Tipo
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

        // Categoria
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

        // DATA
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Data: ${selectedDate.toString()}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val transaction = Transaction(
                    description = description,
                    value = valueDouble ?: 0.0,
                    category = selectedCategory,
                    type = type,
                    date = selectedDate
                )
                onSave(transaction)
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar")
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

/* ---------- Helper ---------- */
private fun millisToLocalDate(millis: Long): LocalDate =
    java.time.Instant.ofEpochMilli(millis)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
