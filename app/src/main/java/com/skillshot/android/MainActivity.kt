/*
Portions of this file adapted from https://github.com/googlemaps/android-samples/
Those portions are covered under the following license:

   Copyright 2014 The Android Open Source Project

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.skillshot.android

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.skillshot.android.api.model.Location
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.skillshot.android.api.SkillShotApi
import okhttp3.Cache
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient



class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = MainActivity::class.java.simpleName

    private var locations: List<Location> = listOf()
    private val SKILL_SHOT_YELLOW = 42

    private var mMap: GoogleMap? = null
    private var mCameraPosition: CameraPosition? = null
    private val mDefaultLocation = LatLng(47.613834, -122.345043)
    private val DEFAULT_ZOOM = 15f
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mLocationPermissionGranted: Boolean = false

    // The entry point to the Fused Location Provider.
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null


    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var mLastKnownLocation: android.location.Location? = null

    // Keys for storing activity state.
    private val KEY_CAMERA_POSITION = "camera_position"
    private val KEY_LOCATION = "location"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }

        setContentView(R.layout.activity_main)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val cacheSize = 10L * 1024 * 1024 // 10 MB
        val cache = Cache(cacheDir, cacheSize)

        val okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .build()
        val retrofit =
            Retrofit.Builder()
                .baseUrl("http://list.skill-shot.com")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val client = retrofit.create(SkillShotApi::class.java)
        getLocations(client)


    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_CAMERA_POSITION, mMap?.cameraPosition)
        outState.putParcelable(KEY_LOCATION, mLastKnownLocation)
        super.onSaveInstanceState(outState)
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            mLocationPermissionGranted || return
            val locationResult = mFusedLocationProviderClient?.lastLocation
            locationResult?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val loc = task.result
                    mLastKnownLocation = loc
                } else {
                    Log.e(TAG, "Exception: %s", task.exception)
                    mMap?.uiSettings?.isMyLocationButtonEnabled = false
                }

                // Move the camera somewhere sane. Preferences are:
                // 1) last place the user had the camera
                // 2) user's location (possibly in a bound to include nearest venue
                // 3) default location
                if (mCameraPosition != null) {
                    mMap?.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition))
                } else {
                    val loc = mLastKnownLocation
                    if (loc != null) {
                        mMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    loc.latitude,
                                    loc.longitude
                                ), DEFAULT_ZOOM
                            )
                        )
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        mMap?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM)
                        )

                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
        getDeviceLocation()
        updateLocationUI()
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private fun updateLocationUI() {
        val map = mMap ?: return
        try {
            if (mLocationPermissionGranted) {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
                mLastKnownLocation = null
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }

    private fun addMarkersToMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { mMap ->
            Log.d(TAG, "Locations size is ${locations.size}")
            for (location in locations) {
                val latlng = LatLng(
                    location.latitude.toDouble(),
                    location.longitude.toDouble()
                )
                mMap.addMarker(
                    MarkerOptions()
                        .position(latlng)
                        .title(location.name)
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(
                                SKILL_SHOT_YELLOW.toFloat()
                            )
                        )
                )
            }
        }
    }

    private fun getLocations(client: SkillShotApi) {
        val call = client.getLocations()
        call.enqueue(object : Callback<List<Location>> {
            override fun onResponse(
                call: Call<List<Location>>,
                response: Response<List<Location>>
            ) {
                Log.d(TAG, "API request!")
                locations = response.body() ?: listOf()
                addMarkersToMap()
            }

            override fun onFailure(call: Call<List<Location>>, t: Throwable) {
                val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinatorLayout)
                Snackbar
                    .make(coordinatorLayout, t.localizedMessage, Snackbar.LENGTH_LONG)
                    .show()
            }
        })

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(mMap: GoogleMap) {
        this.mMap = mMap

        // Prompt the user for permission.
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
    }
}
