package com.moltrax.personalnoteapp.domain.model

import java.util.UUID

data class VaultEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val encryptedContent: String,
    val iv: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
