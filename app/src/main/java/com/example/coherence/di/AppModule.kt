package com.example.coherence.di

import android.content.Context
import com.example.coherence.data.ble.BiofeedbackBleManager
import com.example.coherence.data.ble.BiofeedbackBleManagerImpl
import com.example.coherence.data.database.AppDatabase
import com.example.coherence.data.repository.BiofeedbackRepositoryImpl
import com.example.coherence.domain.repository.BiofeedbackRepository
import com.example.coherence.domain.usecase.CalculateCoherenceUseCase
import com.example.coherence.ui.permission.PermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideBiofeedbackBleManager(@ApplicationContext context: Context): BiofeedbackBleManager {
        return BiofeedbackBleManagerImpl(context)
    }

    @Provides
    @Singleton
    fun provideBiofeedbackRepository(
        bleManager: BiofeedbackBleManager,
        db: AppDatabase,
        @ApplicationContext context: Context // <--- AÑADIDO: Necesario para DataStore
    ): BiofeedbackRepository {

        return BiofeedbackRepositoryImpl(bleManager, db, context)
    }

    @Provides
    @Singleton
    fun provideCalculateCoherenceUseCase(): CalculateCoherenceUseCase {
        return CalculateCoherenceUseCase()
    }

    @Provides
    @Singleton
    fun providePermissionManager(@ApplicationContext context: Context): PermissionManager {
        return PermissionManager(context)
    }
}