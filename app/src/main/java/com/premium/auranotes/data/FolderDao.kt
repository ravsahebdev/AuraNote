package com.premium.auranotes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<Folder>>

    @Query("""
        SELECT 
            f.id, 
            f.name, 
            f.iconEmoji, 
            f.createdAt, 
            (SELECT COUNT(*) FROM notes n WHERE n.folderId = f.id AND n.isTrashed = 0) AS noteCount 
        FROM folders f 
        ORDER BY f.name ASC
    """)
    fun getFoldersWithCount(): Flow<List<FolderWithCount>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: Long): Folder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: Folder): Long

    @Update
    suspend fun update(folder: Folder)

    @Delete
    suspend fun delete(folder: Folder)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM folders")
    suspend fun getAllFoldersSync(): List<Folder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(folders: List<Folder>)
}
