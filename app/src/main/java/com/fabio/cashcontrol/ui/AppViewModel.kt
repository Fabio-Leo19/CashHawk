package com.fabio.cashcontrol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabio.cashcontrol.data.TransactionRepositoryRoom
import com.fabio.cashcontrol.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 *  ----------------------------------------------------------------------
 *   ✅ AppViewModel
 *   Centraliza toda a lógica do app (UI + Dados).
 *   - Lista as transações
 *   - Calcula totais
 *   - Adiciona, edita e exclui
 *   - Obtém transação por ID
 *   - Prepara o app para backend futuramente
 *  ----------------------------------------------------------------------
 */

class AppViewModel(
    private val repo: TransactionRepositoryRoom
) : ViewModel() {

    /* ------------------------------------------------------------------
       🔹 LISTA COMPLETA DE TRANSACOES
       Fluxo vindo do banco Room
    ------------------------------------------------------------------ */
    private val allTransactionsFlow = repo.listAll()

    /* ------------------------------------------------------------------
       🔹 Estado completo exposto à UI
    ------------------------------------------------------------------ */
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState

    init {
        /**
         * Sempre que o banco de dados mudar, atualizamos o estado
         */
        viewModelScope.launch {
            allTransactionsFlow.collect { list ->
                _uiState.update { state ->
                    state.copy(
                        transactions = list,
                        totalIncome = list.filter { it.type.isIncome }.sumOf { it.value },
                        totalExpense = list.filter { it.type.isExpense }.sumOf { it.value },
                    )
                }
            }
        }
    }

    /* ------------------------------------------------------------------
       🔹 ADICIONAR TRANSAÇÃO
    ------------------------------------------------------------------ */
    fun addTransaction(tx: Transaction) {
        viewModelScope.launch {
            repo.add(tx)
        }
    }

    /* ------------------------------------------------------------------
       🔹 EDITAR TRANSAÇÃO
    ------------------------------------------------------------------ */
    fun updateTransaction(tx: Transaction) {
        viewModelScope.launch {
            repo.add(tx) // Room faz upsert = atualiza ou cria
        }
    }

    /* ------------------------------------------------------------------
       🔹 DELETAR TRANSAÇÃO
    ------------------------------------------------------------------ */
    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            repo.deleteById(tx.id)
        }
    }

    /* ------------------------------------------------------------------
       🔹 BUSCAR TRANSAÇÃO POR ID
    ------------------------------------------------------------------ */
    fun getTransaction(id: String): Transaction? {
        return _uiState.value.transactions.find { it.id == id }
    }
}

/**
 *  ----------------------------------------------------------------------
 *  📦 Estado completo do App
 *  Tudo que a UI precisa observar e atualizar automaticamente
 *  ----------------------------------------------------------------------
 */
data class AppUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
) {
    val balance: Double get() = totalIncome - totalExpense
}

/**
 *  ----------------------------------------------------------------------
 *  🏷 Helpers de tipo
 *  ----------------------------------------------------------------------
 */
val com.fabio.cashcontrol.model.TransactionType.isIncome: Boolean
    get() = this.name == "INCOME"

val com.fabio.cashcontrol.model.TransactionType.isExpense: Boolean
    get() = this.name == "EXPENSE"
