package com.fabio.cashcontrol.data

import com.fabio.cashcontrol.model.Category
import com.fabio.cashcontrol.model.Transaction
import com.fabio.cashcontrol.model.TransactionType
import com.fabio.cashcontrol.model.Totals
import kotlinx.coroutines.flow.Flow

class TransactionRepositoryRoom(
    private val dao: TransactionDao
) {

    // Lista tudo diretamente do Room
    fun listAll(): Flow<List<Transaction>> =
        dao.listAll()

    // Inserir / atualizar (upsert)
    suspend fun add(tx: Transaction) {
        dao.insert(tx)
    }

    // Remover objeto
    suspend fun delete(tx: Transaction) {
        dao.delete(tx)
    }

    // Remover por ID
    suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }

    // Lista por ano / mês
    fun listByMonth(year: Int, month: Int): Flow<List<Transaction>> =
        dao.listByMonth(
            year.toString(),
            month.toString().padStart(2, '0')
        )

    // Totais
    fun totals(transactions: List<Transaction>): Totals {
        val income = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.value }
        val expense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.value }
        return Totals(income, expense)
    }

    // Totais por categoria (para despesas)
    fun totalsByCategory(transactions: List<Transaction>): Map<Category, Double> =
        transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.value } }

    // Dados fake (opcional para testes)
    suspend fun seedSample() {
        add(
            Transaction(
                description = "Salário",
                value = 3000.0,
                type = TransactionType.INCOME
            )
        )
        add(
            Transaction(
                description = "Pizza",
                value = 30.0,
                type = TransactionType.EXPENSE,
                category = Category.ALIMENTACAO
            )
        )
        add(
            Transaction(
                description = "Aluguel",
                value = 1200.0,
                type = TransactionType.EXPENSE,
                category = Category.CASA
            )
        )
    }
}
