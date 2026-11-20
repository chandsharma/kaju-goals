package com.chandsharma.kajugoals.di

import android.content.Context
import androidx.room.Room
import com.chandsharma.kajugoals.data.local.GoalDao
import com.chandsharma.kajugoals.data.local.KajuGoalsDatabase
import com.chandsharma.kajugoals.data.repository.IdGenerator
import com.chandsharma.kajugoals.data.repository.RoomGoalRepository
import com.chandsharma.kajugoals.data.repository.SyncScheduler
import com.chandsharma.kajugoals.data.repository.TimeProvider
import com.chandsharma.kajugoals.data.sync.WorkManagerSyncScheduler
import com.chandsharma.kajugoals.data.sync.DefaultDriveSyncCoordinator
import com.chandsharma.kajugoals.data.sync.DriveSyncCoordinator
import com.chandsharma.kajugoals.domain.repository.GoalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KajuGoalsDatabase =
        Room.databaseBuilder(
            context,
            KajuGoalsDatabase::class.java,
            "kaju-goals.db",
        ).addMigrations(KajuGoalsDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideGoalDao(database: KajuGoalsDatabase): GoalDao = database.goalDao()

    @Provides
    @Singleton
    fun provideGoalRepository(
        goalDao: GoalDao,
        timeProvider: TimeProvider,
        idGenerator: IdGenerator,
        syncScheduler: SyncScheduler,
    ): GoalRepository = RoomGoalRepository(goalDao, timeProvider, idGenerator, syncScheduler)

    @Provides
    @Singleton
    fun provideSyncScheduler(scheduler: WorkManagerSyncScheduler): SyncScheduler = scheduler

    @Provides
    @Singleton
    fun provideDriveSyncCoordinator(
        coordinator: DefaultDriveSyncCoordinator,
    ): DriveSyncCoordinator = coordinator

    @Provides
    fun provideTimeProvider(): TimeProvider = TimeProvider(System::currentTimeMillis)

    @Provides
    fun provideIdGenerator(): IdGenerator = IdGenerator { UUID.randomUUID().toString() }
}
