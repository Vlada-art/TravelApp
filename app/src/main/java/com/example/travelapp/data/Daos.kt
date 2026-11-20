package com.example.travelapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :hash LIMIT 1")
    suspend fun login(email: String, hash: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): User?

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}

@Dao
interface FavoriteDao {
    @Insert
    suspend fun insert(fav: Favorite): Long

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY addedAt DESC")
    suspend fun getAll(userId: Long): List<Favorite>

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId AND placeName = :name")
    suspend fun isFavorite(userId: Long, name: String): Int

    @Query("DELETE FROM favorites WHERE userId = :userId AND placeName = :name")
    suspend fun deleteByName(userId: Long, name: String)

    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}