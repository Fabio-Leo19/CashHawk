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
import java.util.UUID

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState

private val ScreenBackground = Color(0xFF1E1F26)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onSave: (Transaction) -> Unit,
    onCancel: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.INCOME) }

    val categories = Category.values().toList()
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()

    val valueDouble = valueText.replace(",", ".").toDoubleOrNull()
    val isValueError = valueText.isNotBlank() && valueDouble == null
    val canSave = description.isNotBlank() && (valueDouble != null && valueDouble > 0)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                "Nova Transação",
                style = MaterialTheme.typography.titleLarge,
                color = Gold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
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
                        colors = outlinedColors()
                    )

                    OutlinedTextField(
                        value = valueText,
                        onValueChange = { valueText = it },
                        label = { Text("Valor") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = isValueError,
                        colors = outlinedColors()
                    )

                    if (isValueError) {
                        Text(
                            "Valor inválido",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

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

                    if (type == TransactionType.EXPENSE) {
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
                                    .fillMaxWidth(),
                                colors = outlinedColors()
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
                    }

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Gold
                        )
                    ) {
                        Text("Data: ${selectedDate}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botão gradiente Salvar
            Button(
                onClick = {
                    val transaction = Transaction(
                        id = UUID.randomUUID().toString(),
                        description = description,
                        value = valueDouble ?: 0.0,
                        category = if (type == TransactionType.EXPENSE) selectedCategory else Category.OUTROS,
                        type = type,
                        date = selectedDate
                    )
                    onSave(transaction)
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
                        "Salvar",
                        color = if (canSave) Color.Black else Color(0xFF777777),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
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

private fun millisToLocalDate(millis: Long) =
    java.time.Instant.ofEpochMilli(millis)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Gold,
    unfocusedBorderColor = Color(0x55FFFFFF),
    focusedLabelColor = Gold,
    unfocusedLabelColor = Color(0x88FFFFFF),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
