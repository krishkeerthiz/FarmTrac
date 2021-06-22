package com.yourapp.farmtrac.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.yourapp.farmtrac.database.entity.CustomerHistory
import java.sql.Date

@Dao
interface CustomerHistoryDao {
    @Insert
    suspend fun insert(customerHistory: CustomerHistory)

    @Query("select * from CustomerHistory")
    suspend fun getAll() : List<CustomerHistory>

    @Delete
    suspend fun delete(customerHistory: CustomerHistory)

    @Query("select name from CustomerHistory")
    suspend fun getNames() : List<String>

    @Query("select date from CustomerHistory")
    suspend fun getDates() : List<Date>

    @Query("select points from CustomerHistory")
    suspend fun getPoints() : List<Int>

    @Query("select * from CustomerHistory where name like :name ")
    suspend fun getByName(name : String) : List<CustomerHistory>

    @Query("select * from CustomerHistory where date = :date")
    suspend fun getByDate(date : Date) : List<CustomerHistory>

    @Query("select * from CustomerHistory where name = :name and points = :point and date = :date")
    suspend fun getRecord(name : String, point : Int, date : Date) : CustomerHistory

    @Insert
    suspend fun insertAll(vararg customerHistory: CustomerHistory)
}