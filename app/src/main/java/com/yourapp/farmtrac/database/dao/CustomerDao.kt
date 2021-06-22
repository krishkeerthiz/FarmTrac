package com.yourapp.farmtrac.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.yourapp.farmtrac.database.entity.Customer

@Dao
interface CustomerDao {
    @Insert
    suspend fun insert(customer : Customer)

    @Query("select * from Customer")
    suspend fun getAll() : List<Customer>

    @Delete
    suspend fun delete(customer: Customer)

    @Query("select name from customer")
    suspend fun getNames() : List<String>

    @Query("select * from customer where name like :name limit 1")
    suspend fun getByName(name : String) : Customer

    @Insert
    suspend fun insertAll(vararg customer: Customer)
}