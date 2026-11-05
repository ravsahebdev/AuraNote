package com.premium.auranotes.di
import android.app.Application
import androidx.room.Room
import com.premium.auranotes.data.AppDatabase
import com.premium.auranotes.data.NoteDao
import com.premium.auranotes.repository.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "notes_db").fallbackToDestructiveMigration().build()
    }
    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
    @Provides
    @Singleton
    fun provideNoteRepository(dao: NoteDao): NoteRepository = NoteRepository(dao)
}
