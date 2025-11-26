package com.fabio.cashcontrol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fabio.cashcontrol.model.Transaction
import java.time.format.DateTimeFormatter

private val CardBackground = Color(0xFF292929)
private val Gold = Color(0xFFD4A048)

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDelete: (String) -> Unit = {}
) {

    val dismissState = rememberDismissState { value ->
        when (value) {
            DismissValue.DismissedToEnd -> {
                onEdit(transaction.id)
                false
            }

            DismissValue.DismissedToStart -> {
                onDelete(transaction.id)
                true
            }

            else -> false
        }
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(
            DismissDirection.StartToEnd,
            DismissDirection.EndToStart
        ),
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss

            val bgColor: Color
            val icon: ImageVector
            val actionText: String

            when (direction) {
                DismissDirection.StartToEnd -> {
                    bgColor = Color(0x3322C55E)
                    icon = Icons.Default.Edit
                    actionText = "Editar"
                }

                DismissDirection.EndToStart -> {
                    bgColor = Color(0x33FF5252)
                    icon = Icons.Default.Delete
                    actionText = "Excluir"
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (direction == DismissDirection.StartToEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, contentDescription = actionText, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(actionText, color = Color.White)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(actionText, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Icon(icon, contentDescription = actionText, tint = Color.White)
                    }
                }
            }
        },
        dismissContent = {
            TransactionCard(
                transaction = transaction,
                onClick = onClick
            )
        }
    )
}

@Composable
private fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val isIncome = transaction.type.name == "INCOME"
    val icon = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val color = if (isIncome) Gold else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )
                Text(
                    text = "${transaction.category.label} • ${formatDate(transaction)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatMoney(transaction.value),
                color = color,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

private fun formatDate(tx: Transaction): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM")
    return tx.date.format(formatter)
}

private fun formatMoney(value: Double): String =
    "R$ %.2f".format(value)
