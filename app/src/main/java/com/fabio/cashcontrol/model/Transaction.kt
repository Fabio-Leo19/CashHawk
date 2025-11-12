package com.fabio.cashcontrol.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val description: String,
    val value: Double,

    // ✅ LocalDate → convertido via TypeConverter
    val date: LocalDate = LocalDate.now(),

    val category: Category = Category.OUTROS,
    val type: TransactionType
)
