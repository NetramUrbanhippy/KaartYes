package nl.kaartyes.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nl.kaartyes.app.data.db.AppDatabase
import nl.kaartyes.app.data.db.CardDao
import nl.kaartyes.app.data.security.DatabaseKeyManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyManager: DatabaseKeyManager
    ): AppDatabase {
        val passphrase = keyManager.getOrCreateDatabasePassphrase()
        return AppDatabase.create(context, passphrase)
    }

    @Provides
    @Singleton
    fun provideCardDao(database: AppDatabase): CardDao = database.cardDao()
}
