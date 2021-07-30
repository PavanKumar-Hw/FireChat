package com.example.firechat.chat.data.entities.typeconverters

import androidx.room.TypeConverter
import com.example.firechat.chat.data.models.Location
import com.example.firechat.chat.data.models.RequestOptions
import com.example.firechat.common.fromObject
import com.example.firechat.common.fromString
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RequestOptionsConverter {
    /**
     * Convert  a RequestObject to a Json
     */
    @TypeConverter
    fun fromImagesJson(stat: RequestOptions): String {
        return Gson().fromObject(stat)
    }

    /**
     * Convert a json to a Obj
     */
    @TypeConverter
    fun toReqOptsObj(reqOptStr: String): RequestOptions {
        val reqOptType = object : TypeToken<RequestOptions>() {}.type
        return Gson().fromString(reqOptStr, reqOptType) as RequestOptions
    }
}


class LocationConverter {
    /**
     * Convert  a RequestObject to a Json
     */
    @TypeConverter
    fun fromImagesJson(stat: Location): String {
        return Gson().fromObject(stat)
    }

    /**
     * Convert a json to a Obj
     */
    @TypeConverter
    fun toReqOptsObj(reqOptStr: String): Location {
        val reqOptType = object : TypeToken<Location>() {}.type
        return Gson().fromString(reqOptStr, reqOptType) as Location
    }
}