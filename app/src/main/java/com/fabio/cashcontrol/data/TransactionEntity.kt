package com.fabio.cashcontrol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,

    val description: String,
    val value: Double,

    // ✅ gravado como String (ISO-8601) — convertido no MappingExtensions
    val date: String,

    // ✅ enums como String
    val category: String,
    val type: String
)
