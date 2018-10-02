package com.skillshot.android.model

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Reader

data class Location(val id: String) {
    var name: String? = null
    var address: String? = null
    var city: String? = null
    var postal_code: String? = null
    var latitude: Float = 0.toFloat()
    var longitude: Float = 0.toFloat()
    var phone: String? = null
    var url: String? = null
    var isAll_ages: Boolean = false
    var num_games: Int = 0
//    var machines: Array<Machine>? = null

    class Deserializer : ResponseDeserializable<Location> {
        override fun deserialize(reader: Reader) = Gson().fromJson(reader, Location::class.java)
    }

    class ListDeserializer : ResponseDeserializable<List<Location>> {
        override fun deserialize(reader: Reader): List<Location> {
            val type = object : TypeToken<List<Location>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }
}
