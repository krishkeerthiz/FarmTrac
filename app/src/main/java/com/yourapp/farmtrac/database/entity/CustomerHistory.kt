package com.yourapp.farmtrac.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity
data class CustomerHistory(
    @PrimaryKey(autoGenerate = true) var cid : Int = 0,
    var name : String,
    var work : String,
    var points : Int,
    var date : Date?
)