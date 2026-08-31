package com.bolt.diy.di

import android.content.Context
import androidx.room.Room
import com.bolt.diy.data.local.BoltDatabase
import com.bolt.diy.data.local.SettingsRepository
import com.bolt.diy.data.repository.ChatRepository
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
  fun provideBoltDatabase(@ApplicationContext context: Context): BoltDatabase {
    return Room.databaseBuilder(
      context,
      BoltDatabase::class.java,
      "bolt_database"
    ).build()
  }

  @Provides
  @Singleton
  fun provideChatRepository(database: BoltDatabase): ChatRepository {
    return ChatRepository(database)
  }

  @Provides
  @Singleton
  fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
    return SettingsRepository(context)
  }
}
