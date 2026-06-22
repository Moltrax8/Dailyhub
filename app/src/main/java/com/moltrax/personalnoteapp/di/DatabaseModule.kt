package com.moltrax.personalnoteapp.di

import android.content.Context
import androidx.room.Room
import com.moltrax.personalnoteapp.data.local.db.AppDatabase
import com.moltrax.personalnoteapp.data.local.db.dao.ExerciseDao
import com.moltrax.personalnoteapp.data.local.db.dao.TaskDao
import com.moltrax.personalnoteapp.data.local.db.dao.VaultDao
import com.moltrax.personalnoteapp.data.local.db.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "personal_note_app.db").build()

    @Provides fun provideTaskDao(db: AppDatabase): TaskDao         = db.taskDao()
    @Provides fun provideWorkoutDao(db: AppDatabase): WorkoutDao   = db.workoutDao()
    @Provides fun provideVaultDao(db: AppDatabase): VaultDao       = db.vaultDao()
    @Provides fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()
}
