package com.chandsharma.kajugoals.di

import android.content.Context
import androidx.room.Room
import com.chandsharma.kajugoals.data.local.GoalDao
import com.chandsharma.kajugoals.data.local.KajuGoalsDatabase
import com.chandsharma.kajugoals.data.repository.IdGenerator
import com.chandsharma.kajugoals.data.repository.RoomGoalRepository
import com.chandsharma.kajugoals.data.repository.TimeProvider
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
        ).build()

    @Provides
    fun provideGoalDao(database: KajuGoalsDatabase): GoalDao = database.goalDao()

    @Provides
    @Singleton
    fun provideGoalRepository(
        goalDao: GoalDao,
        timeProvider: TimeProvider,
        idGenerator: IdGenerator,
    ): GoalRepository = RoomGoalRepository(goalDao, timeProvider, idGenerator)

    @Provides
    fun provideTimeProvider(): TimeProvider = TimeProvider(System::currentTimeMillis)

    @Provides
    fun provideIdGenerator(): IdGenerator = IdGenerator { UUID.randomUUID().toString() }
}
