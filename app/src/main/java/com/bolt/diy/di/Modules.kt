package com.bolt.diy.di

import com.bolt.diy.data.local.BoltDatabase
import com.bolt.diy.data.repository.ChatRepository
import com.bolt.diy.domain.service.FileSystemService
import com.bolt.diy.domain.service.LlmService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

  @Provides
  @ViewModelScoped
  fun provideLlmService(): LlmService {
    return LlmService(
      HttpClient {
        install(ContentNegotiation) {
          json(Json {
            ignoreUnknownKeys = true
            isLenient = true
          })
        }
      }
    )
  }

  @Provides
  @ViewModelScoped
  fun provideFileSystemService(): FileSystemService {
    return FileSystemService()
  }
}

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

  @Provides
  @ViewModelScoped
  fun provideChatRepository(database: BoltDatabase): ChatRepository {
    return ChatRepository(database)
  }
}
