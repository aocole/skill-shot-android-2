package com.skillshot.android.api

import com.skillshot.android.api.model.Location
import retrofit2.Call
import retrofit2.http.GET

interface SkillShotApi {

    @GET("/locations/for_wordpress.json")
    fun getLocations(): Call<List<Location>>

}