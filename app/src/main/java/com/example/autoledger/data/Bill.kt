package com.example.autoledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 一条账单。amountCents 恒为正，direction 区分收支。 */
@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val direction: Int,          // 0=支出 1=收入
    val merchant: String,
    val category: String,
    val source: String,          // notification / accessibility / manual / ai
    val sourceKey: String,       // 去重键
    val rawText: String,         // 原始文本（排查用）
    val happenedAt: Long,        // epoch millis
    val createdAt: Long = System.currentTimeMillis()
)
