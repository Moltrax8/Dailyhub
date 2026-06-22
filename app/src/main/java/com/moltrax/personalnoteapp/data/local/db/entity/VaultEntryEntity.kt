package com.moltrax.personalnoteapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moltrax.personalnoteapp.domain.model.VaultEntry

@Entity(tableName = "vault_entries")
data class VaultEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val encryptedContent: String,
    val iv: String,
    val createdAt: Long,
    val updatedAt: Long,
)

fun VaultEntryEntity.toDomain() = VaultEntry(id, title, encryptedContent, iv, createdAt, updatedAt)
fun VaultEntry.toEntity()       = VaultEntryEntity(id, title, encryptedContent, iv, createdAt, updatedAt)
