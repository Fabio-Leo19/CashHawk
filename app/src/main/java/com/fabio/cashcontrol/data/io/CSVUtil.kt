package com.fabio.cashcontrol.data.io

import com.fabio.cashcontrol.model.Category
import com.fabio.cashcontrol.model.Transaction
import com.fabio.cashcontrol.model.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object CSVUtil {

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    /* ✅ EXPORT */
    fun export(transactions: List<Transaction>): String {

        val header = "id,description,value,category,type,date"

        val rows = transactions.joinToString("\n") { tx ->
            val desc = tx.description.replace(",", ";")
            val cat = tx.category.name.replace(",", ";")

            "${tx.id},${desc},${"%.2f".format(tx.value)},${cat},${tx.type.name},${tx.date.format(formatter)}"
        }

        return "$header\n$rows"
    }


    /* ✅ IMPORT */
    fun import(csv: String): List<Transaction> {
        val lines = csv.lines().drop(1)

        return lines.mapNotNull { line ->

            if (line.isBlank()) return@mapNotNull null

            val parts = line.split(",")

            if (parts.size < 6) return@mapNotNull null

            val id = parts[0]
            val description = parts[1].replace(";", ",")
            val value = parts[2].toDoubleOrNull() ?: return@mapNotNull null

            val category = try {
                Category.valueOf(parts[3])
            } catch (_: Exception) {
                return@mapNotNull null
            }

            val type = try {
                TransactionType.valueOf(parts[4])
            } catch (_: Exception) {
                return@mapNotNull null
            }

            val date = try {
                LocalDate.parse(parts[5], formatter)
            } catch (_: Exception) {
                return@mapNotNull null
            }

            Transaction(
                id = id,
                description = description,
                value = value,
                category = category,
                type = type,
                date = date
            )
        }
    }
}
