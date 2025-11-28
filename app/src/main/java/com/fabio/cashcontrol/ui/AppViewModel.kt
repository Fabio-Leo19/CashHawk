package com.fabio.cashcontrol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabio.cashcontrol.data.TransactionRepositoryRoom
import com.fabio.cashcontrol.model.Category
import com.fabio.cashcontrol.model.Totals
import com.fabio.cashcontrol.model.Transaction
import com.fabio.cashcontrol.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class AppViewModel(
    private val repo: TransactionRepositoryRoom
) : ViewModel() {

    // Flow bruto vindo do Room
    private val allTransactionsFlow = repo.listAll()

    // Estado principal
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState

    init {
        viewModelScope.launch {
            allTransactionsFlow.collect { list ->
                val income = list.filter { it.type.isIncome }.sumOf { it.value }
                val expense = list.filter { it.type.isExpense }.sumOf { it.value }

                _uiState.update {
                    it.copy(
                        transactions = list,
                        totalIncome = income,
                        totalExpense = expense
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------

    fun addTransaction(tx: Transaction) = viewModelScope.launch {
        repo.add(tx)
    }

    fun updateTransaction(tx: Transaction) = viewModelScope.launch {
        repo.add(tx)
    }

    fun deleteTransaction(tx: Transaction) = viewModelScope.launch {
        repo.deleteById(tx.id)
    }

    fun getTransaction(id: String): Transaction? =
        _uiState.value.transactions.find { it.id == id }

    fun getTransactionFlow(id: String): Flow<Transaction?> =
        allTransactionsFlow.map { list -> list.find { it.id == id } }

    // -------------------------------------------------------------------
    // Funções avançadas
    // -------------------------------------------------------------------

    /** Totais completos para qualquer lista */
    fun calculateTotals(list: List<Transaction>): Totals {
        val inc = list.filter { it.type == TransactionType.INCOME }.sumOf { it.value }
        val exp = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.value }
        return Totals(inc, exp)
    }

    /** Filtra por mês/ano */
    fun filterByMonth(list: List<Transaction>, year: Int, month: Int): List<Transaction> =
        list.filter { it.date.year == year && it.date.monthValue == month }

    /** Filtra por tipo */
    fun filterByType(list: List<Transaction>, type: TransactionType?): List<Transaction> =
        type?.let { t -> list.filter { it.type == t } } ?: list

    /** Filtra por categoria */
    fun filterByCategory(list: List<Transaction>, category: Category?): List<Transaction> =
        category?.let { c -> list.filter { it.category == c } } ?: list

    /** Busca por texto */
    fun search(list: List<Transaction>, query: String): List<Transaction> {
        if (query.isBlank()) return list
        val q = query.trim().lowercase()
        return list.filter {
            it.description.lowercase().contains(q) ||
                    it.category.label.lowercase().contains(q)
        }
    }

    /** Ordenação */
    fun sort(
        list: List<Transaction>,
        by: SortBy,
        dir: SortDir
    ): List<Transaction> {
        val sorted = when (by) {
            SortBy.DATE -> list.sortedBy { it.date }
            SortBy.VALUE -> list.sortedBy { it.value }
        }
        return if (dir == SortDir.DESC) sorted.reversed() else sorted
    }
}

// -------------------------------------------------------------------
// Estado da UI
// -------------------------------------------------------------------
data class AppUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
) {
    val balance: Double get() = totalIncome - totalExpense
}

// MELHORES HELPERS (sem comparar string)
val TransactionType.isIncome: Boolean get() = this == TransactionType.INCOME
val TransactionType.isExpense: Boolean get() = this == TransactionType.EXPENSE
