package com.moltrax.personalnoteapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.moltrax.personalnoteapp.domain.repository.TaskRepository
import com.moltrax.personalnoteapp.widget.TaskWidget
import com.moltrax.personalnoteapp.worker.RescheduleNotificationsWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PersonalNoteApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var taskRepository: TaskRepository

    // Süreç ömrü boyunca yaşayan hafif kapsam: görev akışını dinleyip widget'ı tazeler.
    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        val wm = WorkManager.getInstance(this)

        // Oyunlaştırma kaldırıldı: eski sürümlerde kurulmuş "Ceza Bölgesi" periyodik işini iptal et
        // (worker sınıfı artık yok; aksi halde WorkManager onu başlatmaya çalışıp hata üretir).
        wm.cancelUniqueWork("penalty_check_periodic")

        // Uygulama her açıldığında bekleyen görevlerin hatırlatma alarmlarını yeniden kur
        // (cihaz yeniden başlatıldıysa veya alarmlar düştüyse güvenlik ağı).
        wm.enqueue(OneTimeWorkRequestBuilder<RescheduleNotificationsWorker>().build())

        // Görev verisi (alt görevler dahil) her değiştiğinde widget'ı otomatik tazele. Glance
        // widget'ları akışı kendiliğinden dinleyemez; yalnızca updateAll çağrılınca yeniden çizilir.
        // Bu yüzden tek merkezden dinleyip her değişimde günceller — böylece uygulama içindeki alt
        // görev tamamlamaları da widget'a anında yansır. Hafif imzayla yalnızca anlamlı (başlık,
        // tamamlanma, alt görev durumu) değişimlerde tetiklenir.
        widgetScope.launch {
            taskRepository.observeAll()
                .map { tasks ->
                    tasks.joinToString("|") { t ->
                        "${t.id}:${t.isDone}:${t.title}:" +
                            t.subtasks.joinToString(",") { "${it.id}=${it.isDone}=${it.title}" }
                    }
                }
                .distinctUntilChanged()
                .drop(1) // ilk emisyon mevcut durumdur; açılışta gereksiz güncelleme yapma
                .collect { TaskWidget.requestUpdate(this@PersonalNoteApp) }
        }
    }
}
