package com.ngt.pixplay.di

import android.content.Context
import androidx.room.Room
import com.ngt.pixplay.data.local.MusicDatabase
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
    fun provideMusicDatabase(
        @ApplicationContext context: Context
    ): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            MusicDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration(false)
        .build()
    }
    
    @Provides
    fun provideSongDao(database: MusicDatabase) = database.songDao()
    
    @Provides
    fun providePlaylistDao(database: MusicDatabase) = database.playlistDao()
    
    @Provides
    fun provideFavoriteDao(database: MusicDatabase) = database.favoriteDao()
}
