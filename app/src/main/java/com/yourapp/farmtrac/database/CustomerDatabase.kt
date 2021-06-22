package com.yourapp.farmtrac.database

import android.content.Context
import androidx.room.*
import com.yourapp.farmtrac.database.dao.CustomerBalanceDao
import com.yourapp.farmtrac.database.dao.CustomerDao
import com.yourapp.farmtrac.database.dao.CustomerHistoryDao
import com.yourapp.farmtrac.database.entity.Customer
import com.yourapp.farmtrac.database.entity.CustomerBalance
import com.yourapp.farmtrac.database.entity.CustomerHistory

import java.io.File
import java.sql.Date

@Database(entities = [Customer::class, CustomerHistory::class, CustomerBalance::class], version = 2)
@TypeConverters(Converters::class)
abstract class CustomerDatabase : RoomDatabase() {

    abstract fun getCustomerDao() : CustomerDao
    abstract fun getCustomerHistoryDao() : CustomerHistoryDao
    abstract fun getCustomerBalanceDao() : CustomerBalanceDao

    companion object {

        private const val TAG = "CustomerDatabase"

        const val VERSION = 1
        private const val DATABASE_NAME = "customer_database.db"

        @Volatile
        private var instance: CustomerDatabase? = null

        /**
         * Gets and returns the database instance if exists; otherwise, builds a new database.
         * @param context The context to access the application context.
         * @return The database instance.
         */
        fun getInstance(context: Context): CustomerDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }

        /**
         * Builds and returns the database.
         * @param appContext The application context to reference.
         * @return The built database.
         */
        private fun buildDatabase(appContext: Context): CustomerDatabase {
            val filesDir = appContext.getExternalFilesDir(null)
            val dataDir = File(filesDir, "data")
            if (!dataDir.exists())
                dataDir.mkdir()

            val builder =
                Room.databaseBuilder(
                    appContext,
                    CustomerDatabase::class.java,
                    File(dataDir, DATABASE_NAME).toString()
                ).fallbackToDestructiveMigration()

            return builder.build()
        }

    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value : Long?) : Date?{
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date : Date?) : Long?{
        return date?.time?.toLong()
    }
}