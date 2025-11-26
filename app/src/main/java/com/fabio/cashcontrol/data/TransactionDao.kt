package com.fabio.cashcontrol.data

import androidx.room.*
import com.fabio.cashcontrol.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun listAll(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: Transaction)

    @Delete
    suspend fun delete(tx: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        """
        SELECT * FROM transactions
        WHERE strftime('%Y', date) = :year
        AND   strftime('%m', date) = :month
        ORDER BY date DESC
        """
    )
    fun listByMonth(
        year: String,
        month: String
    ): Flow<List<Transaction>>
}
