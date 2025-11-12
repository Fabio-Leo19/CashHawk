package com.fabio.cashcontrol.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fabio.cashcontrol.model.Transaction
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDelete: (String) -> Unit = {}
) {

    /* ----------------------------
        SWIPE STATE
    ---------------------------- */
    val dismissState = rememberDismissState()

    // Quando swipe completa → deletar
    if (dismissState.isDismissed(DismissDirection.EndToStart)) {
        onDelete(transaction.id)
    }
    if (dismissState.isDismissed(DismissDirection.StartToEnd)) {
        onEdit(transaction.id)
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(
            DismissDirection.StartToEnd,  // Edit
            DismissDirection.EndToStart   // Delete
        ),

        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss

            val bgColor: Color
            val icon: ImageVector
            val actionText: String

            when (direction) {
                DismissDirection.StartToEnd -> {
                    bgColor = MaterialTheme.colorScheme.primary
                    icon = Icons.Default.Edit
                    actionText = "Editar"
                }
                DismissDirection.EndToStart -> {
                    bgColor = MaterialTheme.colorScheme.error
                    icon = Icons.Default.Delete
                    actionText = "Excluir"
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (direction == DismissDirection.StartToEnd)
                    Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(icon, contentDescription = actionText, tint = Color.White)
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

/* ---------------------------
   CARD PRINCIPAL
---------------------------- */
@Composable
private fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val isIncome = transaction.type.name == "INCOME"
    val icon = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val color = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
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

/* ------------------------------------
      HELPERS
------------------------------------*/

private fun formatDate(tx: Transaction): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM")
    return tx.date.format(formatter)
}

private fun formatMoney(value: Double): String =
    "R$ %.2f".format(value)
