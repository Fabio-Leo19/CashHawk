package com.fabio.cashcontrol.model

data class Totals(
    val income: Double,
    val expense: Double
) {
    val balance: Double
        get() = income - expense

    val hasIncome: Boolean
        get() = income > 0

    val hasExpense: Boolean
        get() = expense > 0
}
