package com.moltrax.personalnoteapp.domain.repository

import com.moltrax.personalnoteapp.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    val syncStatus: Flow<SyncStatus>

    /** Arka plan (otomatik) gönderim — veri değiştikçe çağrılır, başarısı sessiz geçer. */
    suspend fun pushToDrive()

    suspend fun pullFromDrive(manual: Boolean = false)

    /**
     * Güvenli tam senkronizasyon: önce uzaktaki veriyi çekip yerelle birleştirir,
     * sonra birleşmiş sonucu geri gönderir. Yeni/boş bir cihazda uzaktaki yedeğin
     * boş veriyle ezilmesini önler. Açılışta ve manuel "Senkronize et" için kullanılır.
     *
     * [manual] true ise (kullanıcı tetiklediyse) başarı durumu arayüzde gösterilir.
     * Otomatik (arka plan) senkronizasyonda başarı yalnızca önceki durum HATA ise gösterilir;
     * aksi halde sessizce tamamlanır.
     */
    suspend fun sync(manual: Boolean = false)

    /** Gösterilen "Senkronize edildi" başarı durumunu temizler (Idle'a çeker). Hatayı temizlemez. */
    fun acknowledgeStatus()
}
