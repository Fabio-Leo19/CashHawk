package com.fabio.cashcontrol.data

import com.fabio.cashcontrol.model.Category
import com.fabio.cashcontrol.model.Transaction
import com.fabio.cashcontrol.model.TransactionType
import com.fabio.cashcontrol.model.Totals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryRoom(
    private val dao: TransactionDao
) {

    // ✅ Lista tudo
    fun listAll(): Flow<List<Transaction>> =
        dao.listAll().map { list ->
            list.map { it.toModel() }
        }

    // ✅ Inserir
    suspend fun add(tx: Transaction) {
        dao.insert(tx.toEntity())
    }

    // ✅ Remover objeto
    suspend fun delete(tx: Transaction) {
        dao.delete(tx.toEntity())
    }

    // ✅ Remover por ID
    suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }

    // ✅ Lista por ano / mês
    fun listByMonth(year: Int, month: Int): Flow<List<Transaction>> =
        dao.listByMonth(
            year.toString(),
            month.toString().padStart(2, '0')
        ).map { list ->
            list.map { it.toModel() }
        }



    // ✅ Totais (mesma lógica do core antigo)
    fun totals(transactions: List<Transaction>): Totals {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.value }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.value }
        return Totals(income, expense)
    }

    // ✅ Totais por categoria
    fun totalsByCategory(transactions: List<Transaction>): Map<Category, Double> =
        transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.value } }

    // ✅ Inserts fake (opcional para testes)
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
