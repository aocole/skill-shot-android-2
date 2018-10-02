package com.skillshot.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.skillshot.android.model.Location

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var mMap: GoogleMap
    private var locations: List<Location> = listOf()
    private val SKILL_SHOT_YELLOW = 42


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        FuelManager.instance.apply {
            basePath = "http://list.skill-shot.com"
        }
        getLocations()


    }

    private fun getLocations() {
        Fuel.get("/locations.json")
            .responseObject(Location.ListDeserializer()) { request, _, result ->
                //            .responseString { request, _, result ->
                Log.d(TAG, request.toString())
                Log.d(TAG, result.toString())
                result.fold({ value ->
                    locations = value
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
                            Log.d(TAG, "Added marker for ${location.name}")
                        }
                    }
                }, { error ->
                    val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinatorLayout)
                    val snackbar = Snackbar
                        .make(coordinatorLayout, error.toString(), Snackbar.LENGTH_LONG)
                    snackbar.show()
                })
            }

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.setMyLocationEnabled(true)
        } else {
            // TODO: Show rationale and request permission.
        }

        // Move the camera to Shorty's
        val shortys = LatLng(47.613834, -122.345043)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(shortys))

        // Show a pointless snackbar
        val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinatorLayout)
        val snackbar = Snackbar
            .make(coordinatorLayout, "Nailed it!", Snackbar.LENGTH_LONG)
        snackbar.show()
    }
}
