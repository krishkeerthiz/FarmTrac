package com.yourapp.farmtrac.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.yourapp.farmtrac.database.entity.Customer
import com.yourapp.farmtrac.database.entity.CustomerBalance

@Dao
interface CustomerBalanceDao {
    @Insert
    suspend fun insert(customerBalance: CustomerBalance)

    @Query("select * from CustomerBalance")
    suspend fun getAll() : List<CustomerBalance>

    @Delete
    suspend fun delete(customerBalance: CustomerBalance)

    @Query("select name from CustomerBalance")
    suspend fun getNames() : List<String>

    @Query("select amount from CustomerBalance")
    suspend fun getAmounts() : List<Int>

    @Query("select * from CustomerBalance where name like :name limit 1")
    suspend fun getByName(name : String) : CustomerBalance

    @Query("update CustomerBalance set amount = :amount where name = :name")
    suspend fun updateAmount( name : String, amount : Int)

    @Insert
    suspend fun insertAll(vararg customerBalance: CustomerBalance)
}