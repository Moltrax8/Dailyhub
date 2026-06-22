package com.moltrax.personalnoteapp.service

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.moltrax.personalnoteapp.domain.model.WorkoutSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor() {

    private fun model(apiKey: String, modelName: String = "gemini-1.5-flash") =
        GenerativeModel(modelName = modelName, apiKey = apiKey)

    suspend fun coachWorkout(session: WorkoutSession, apiKey: String): String {
        val prompt = buildString {
            appendLine("Aşağıdaki antrenman seansını analiz et ve Türkçe olarak koçluk geri bildirimi ver:")
            appendLine("Antrenman: ${session.workoutName}")
            appendLine("Başlangıç: ${java.util.Date(session.startedAt)}")
            session.completedAt?.let { appendLine("Bitiş: ${java.util.Date(it)}") }
            appendLine()
            session.loggedExercises.forEach { ex ->
                appendLine("Egzersiz: ${ex.exerciseName}")
                ex.sets.forEachIndexed { i, set ->
                    val weight = set.weightKg?.let { "${it}kg" } ?: ""
                    val dur    = set.durationSeconds?.let { "${it}sn" } ?: ""
                    appendLine("  Set ${i+1}: ${set.reps} tekrar $weight $dur")
                }
            }
            appendLine()
            appendLine("Lütfen performans değerlendirmesi, iyileştirme önerileri ve sonraki antrenman için tavsiyeler ver.")
        }
        return model(apiKey).generateContent(prompt).text ?: "Analiz yapılamadı."
    }

    suspend fun analyseFood(bitmap: Bitmap, apiKey: String): String {
        val prompt = "Bu yiyeceğin Türkçe olarak yaklaşık besin değerlerini (kalori, protein, karbonhidrat, yağ) ver. " +
            "Yiyeceği tanımla ve kısa bir değerlendirme yap."
        val visionModel = model(apiKey, "gemini-1.5-flash")
        val response = visionModel.generateContent(
            content {
                image(bitmap)
                text(prompt)
            }
        )
        return response.text ?: "Analiz yapılamadı."
    }
}
