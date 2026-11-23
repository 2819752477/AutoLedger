package com.example.autoledger.data

import androidx.room.*

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY happenedAt DESC")
    suspend fun all(): List<Bill>

    @Query("SELECT * FROM bills WHERE sourceKey = :key LIMIT 1")
    suspend fun bySourceKey(key: String): Bill?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bill: Bill): Long

    @Query("DELETE FROM bills")
    suspend fun clear()
}
