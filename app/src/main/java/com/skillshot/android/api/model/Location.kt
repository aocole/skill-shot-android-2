package com.skillshot.android.api.model

data class Location(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val postal_code: String,
    val latitude: Float,
    val longitude: Float,
    val phone: String,
    val url: String,
    val isAll_ages: Boolean,
    val num_games: Int = 0,
    var machines: Array<Machine>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Location

        if (id != other.id) return false
        if (name != other.name) return false
        if (address != other.address) return false
        if (city != other.city) return false
        if (postal_code != other.postal_code) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (phone != other.phone) return false
        if (url != other.url) return false
        if (isAll_ages != other.isAll_ages) return false
        if (num_games != other.num_games) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + city.hashCode()
        result = 31 * result + postal_code.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + isAll_ages.hashCode()
        result = 31 * result + num_games
        return result
    }
}