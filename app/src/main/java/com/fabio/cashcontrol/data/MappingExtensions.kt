package com.fabio.cashcontrol.data

import com.fabio.cashcontrol.model.Category
import com.fabio.cashcontrol.model.Transaction
import com.fabio.cashcontrol.model.TransactionType
import java.time.LocalDate

// ✅ Entity -> Model
fun TransactionEntity.toModel(): Transaction {
    return Transaction(
        id = id,
        description = description,
        value = value,
        date = LocalDate.parse(date),          // String → LocalDate
        category = Category.valueOf(category), // String → Enum
        type = TransactionType.valueOf(type)   // String → Enum
    )
}

// ✅ Model -> Entity
fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        description = description,
        value = value,
        date = date.toString(),      // LocalDate → String
        category = category.name,    // Enum → String
        type = type.name             // Enum → String
    )
}
