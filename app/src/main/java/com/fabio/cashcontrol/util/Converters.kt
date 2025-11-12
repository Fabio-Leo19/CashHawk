package com.fabio.cashcontrol.util

import androidx.room.TypeConverter
import com.fabio.cashcontrol.model.Category
import com.fabio.cashcontrol.model.TransactionType
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromStringDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun dateToString(date: LocalDate?): String? =
        date?.toString()

    @TypeConverter
    fun categoryToString(category: Category?): String? =
        category?.name

    @TypeConverter
    fun stringToCategory(value: String?): Category? =
        value?.let { Category.valueOf(it) }

    @TypeConverter
    fun typeToString(type: TransactionType?): String? =
        type?.name

    @TypeConverter
    fun stringToType(value: String?): TransactionType? =
        value?.let { TransactionType.valueOf(it) }
}
