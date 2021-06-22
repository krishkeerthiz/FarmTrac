package com.yourapp.farmtrac.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Customer(
    @PrimaryKey(autoGenerate = true) var cid : Int = 0,
    var name : String
)
