package com.moltrax.personalnoteapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.moltrax.personalnoteapp.data.local.db.dao.ExerciseDao
import com.moltrax.personalnoteapp.data.local.db.dao.TaskDao
import com.moltrax.personalnoteapp.data.local.db.dao.VaultDao
import com.moltrax.personalnoteapp.data.local.db.dao.WorkoutDao
import com.moltrax.personalnoteapp.data.local.db.entity.ExerciseEntity
import com.moltrax.personalnoteapp.data.local.db.entity.TaskEntity
import com.moltrax.personalnoteapp.data.local.db.entity.VaultEntryEntity
import com.moltrax.personalnoteapp.data.local.db.entity.WorkoutEntity
import com.moltrax.personalnoteapp.data.local.db.entity.WorkoutExerciseEntity
import com.moltrax.personalnoteapp.data.local.db.entity.WorkoutGroupEntity
import com.moltrax.personalnoteapp.data.local.db.entity.WorkoutSessionEntity

@Database(
    entities = [
        TaskEntity::class,
        WorkoutGroupEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        ExerciseEntity::class,
        VaultEntryEntity::class,
        WorkoutSessionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun vaultDao(): VaultDao
    abstract fun exerciseDao(): ExerciseDao
}
