package com.moltrax.personalnoteapp.data.repository

import com.moltrax.personalnoteapp.data.local.db.dao.ExerciseDao
import com.moltrax.personalnoteapp.data.local.db.dao.WorkoutDao
import com.moltrax.personalnoteapp.data.local.db.entity.WorkoutGroupEntity
import com.moltrax.personalnoteapp.data.local.db.entity.toDomain
import com.moltrax.personalnoteapp.data.local.db.entity.toEntity
import com.moltrax.personalnoteapp.data.local.storage.ExerciseVideoStorage
import com.moltrax.personalnoteapp.domain.model.Exercise
import com.moltrax.personalnoteapp.domain.model.WorkoutGroup
import com.moltrax.personalnoteapp.domain.model.WorkoutSession
import com.moltrax.personalnoteapp.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val videoStorage: ExerciseVideoStorage,
) : WorkoutRepository {

    // Üç tabloyu da (groups, workouts, workout_exercises) reaktif olarak dinler; herhangi birine
    // ekleme/silme yapıldığında Flow yeniden tetiklenir ve UI anında güncellenir.
    override fun observeGroups(): Flow<List<WorkoutGroup>> =
        combine(
            workoutDao.observeGroups(),
            workoutDao.observeAllWorkouts(),
            workoutDao.observeAllWorkoutExercises(),
        ) { groups, allWorkouts, allExercises ->
            val workoutsByGroup = allWorkouts.groupBy { it.groupId }
            val exercisesByWorkout = allExercises.groupBy { it.workoutId }
            groups.map { groupEntity ->
                val workouts = workoutsByGroup[groupEntity.id].orEmpty()
                    .sortedBy { it.orderIndex }
                    .map { workoutEntity ->
                        val exercises = exercisesByWorkout[workoutEntity.id].orEmpty()
                            .sortedBy { it.orderIndex }
                            .map { it.toDomain() }
                        workoutEntity.toDomain(exercises)
                    }
                groupEntity.toDomain(workouts)
            }
        }

    // Kullanıcı düzenlemesi: updatedAt'i şimdiye çek (LWW). Antrenman ekleme/silme dahil her
    // değişiklik bu yolu kullandığından, silinen bir antrenman senkronizasyonda geri gelmez.
    override suspend fun upsertGroup(group: WorkoutGroup) =
        persistGroup(group.copy(updatedAt = System.currentTimeMillis()))

    /** Grubu ve çocuklarını verilen zaman damgalarını KORUYARAK yazar (senkronizasyon birleştirmesi). */
    private suspend fun persistGroup(group: WorkoutGroup) {
        val workoutEntities = group.workouts.mapIndexed { idx, workout ->
            workout.toEntity(group.id, idx)
        }
        val exerciseEntities = group.workouts.flatMap { workout ->
            workout.exercises.map { it.toEntity(workout.id) }
        }
        workoutDao.upsertGroupWithChildren(group.toEntity(), workoutEntities, exerciseEntities)
    }

    // Yumuşak silme (mezar taşı) — sync silinen grubu geri diriltmesin. Grup silindiğinde alt
    // hareketleri de gittiğinden, artık kullanılmayan demo medya dosyalarını eş zamanlı temizle.
    override suspend fun deleteGroup(id: String) {
        workoutDao.softDeleteGroup(id, System.currentTimeMillis())
        cleanupOrphanedExerciseMedia()
    }

    override suspend fun saveSession(session: WorkoutSession) = workoutDao.upsertSession(session.toEntity())

    override suspend fun deleteSession(id: String) = workoutDao.deleteSession(id)

    override suspend fun getSessions(): List<WorkoutSession> =
        workoutDao.getSessions().map { it.toDomain() }

    override suspend fun getSessionById(id: String): WorkoutSession? =
        workoutDao.getSessionById(id)?.toDomain()

    override suspend fun getLatestSessionForTask(taskId: String): WorkoutSession? =
        workoutDao.getLatestSessionForTask(taskId)?.toDomain()

    override suspend fun getGroups(): List<WorkoutGroup> =
        workoutDao.getAllGroups().map { it.withChildren() }

    // Mezar taşları dahil tüm gruplar. Silinmiş grupların çocuğu olmadığından boş listeyle gelir.
    override suspend fun getGroupsForSync(): List<WorkoutGroup> =
        workoutDao.getAllGroupsRaw().map { groupEntity ->
            if (groupEntity.isDeleted) groupEntity.toDomain(emptyList())
            else groupEntity.withChildren()
        }

    private suspend fun WorkoutGroupEntity.withChildren(): WorkoutGroup {
        val workouts = workoutDao.getWorkoutsForGroup(id).map { workoutEntity ->
            val exercises = workoutDao.getExercisesForWorkout(workoutEntity.id).map { it.toDomain() }
            workoutEntity.toDomain(exercises)
        }
        return toDomain(workouts)
    }

    override suspend fun replaceGroups(groups: List<WorkoutGroup>) {
        // CASCADE foreign key'ler sayesinde grup silindiğinde alt workout/exercise kayıtları da silinir.
        // Birleştirilmiş grupları zaman damgalarını KORUYARAK yaz (bump etme) — aksi halde her pull
        // sonrası tüm updatedAt'ler şimdiye çekilip LWW ve mezar taşları bozulurdu.
        workoutDao.deleteAllGroups()
        groups.forEach { persistGroup(it) }
    }

    override suspend fun replaceSessions(sessions: List<WorkoutSession>) {
        workoutDao.deleteAllSessions()
        workoutDao.upsertSessions(sessions.map { it.toEntity() })
    }

    override fun observeExercises(): Flow<List<Exercise>> =
        exerciseDao.observeAll().map { it.map { e -> e.toDomain() } }

    override suspend fun upsertExercise(exercise: Exercise) = exerciseDao.upsert(exercise.toEntity())

    override suspend fun searchExercises(query: String): List<Exercise> =
        exerciseDao.search(query).map { it.toDomain() }

    override suspend fun getExercisesByBodyPart(bodyPart: String): List<Exercise> =
        exerciseDao.getByBodyPart(bodyPart).map { it.toDomain() }

    // Referans sayımı: workout_exercises'te hâlâ geçen exerciseId'ler "canlı"dır. Geri kalan
    // önbellek hareketleri yetimdir — önce fiziksel medya dosyası (File.delete) sonra DB kaydı silinir.
    override suspend fun cleanupOrphanedExerciseMedia() {
        val referenced = workoutDao.getReferencedExerciseIds().toSet()
        exerciseDao.getAll().forEach { ex ->
            if (ex.id !in referenced) {
                videoStorage.delete(ex.localMediaPath)
                exerciseDao.deleteById(ex.id)
            }
        }
    }
}
