package com.fabio.cashcontrol.model

data class Totals(
    val income: Double,
    val expense: Double
) {
    val balance: Double
        get() = income - expense
}
