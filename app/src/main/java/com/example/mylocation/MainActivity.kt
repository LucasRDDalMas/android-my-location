package com.example.mylocation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val btn = findViewById<Button>(R.id.btnGetLocation)
        btn.setOnClickListener {
            Log.d("Debug:", checkPermission().toString())
            Log.d("Debug:", isLocationEnabled().toString())

            requestPermission()
            getCurrentLocation(false)
        }

        val btnLastLocation = findViewById<Button>(R.id.btnGetLastLocation)
        btnLastLocation.setOnClickListener {
            Log.d("Debug:", checkPermission().toString())
            Log.d("Debug:", isLocationEnabled().toString())

            requestPermission()
            getCurrentLocation(true)
        }
    }

    private fun getCurrentLocation(lastLocation: Boolean) {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                if (lastLocation) {
                    fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                        val location: Location? = task.result

                        if (location == null) {
                            getNewLocation()
                        } else {
                            setLocation(location)
                        }
                    }
                } else {
                    getNewLocation()
                }
            } else {
                Toast.makeText(this, "Please Enable your Location service", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            requestPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQ_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("Debug: ", "You have the Permission")
            } else {
                Log.d("Debug: ", "You do not have the Permission")
                Toast.makeText(
                    this, "You need to grant permission to access location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getNewLocation() {
        locationRequest = LocationRequest.create().apply {
            interval = 50
            fastestInterval = 50
            numUpdates = 2
            maxWaitTime = 100
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkPermission()) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        }
    }

    private fun setLocation(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        Log.d("Debug", "Lat: ${location.latitude} - Long: ${location.longitude}")
        val txtLocation = findViewById<TextView>(R.id.txtLocation)
        txtLocation.text = "Lat: ${location.latitude} - Long: ${location.longitude}"

        val txtLocationPlace = findViewById<TextView>(R.id.txtLocationPlace)
        txtLocationPlace.text = "City: ${getCityName()} - Country: ${getCountryName()}"
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            setLocation(locationResult.lastLocation)
        }
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQ_CODE
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun getCityName(): String {
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address: MutableList<Address> = geoCoder.getFromLocation(latitude, longitude, 1)

        return address[0].locality ?: address[0].subAdminArea
    }

    private fun getCountryName(): String {
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address: MutableList<Address> = geoCoder.getFromLocation(latitude, longitude, 1)

        return address[0].countryName
    }

    companion object {
        private const val LOCATION_PERMISSION_REQ_CODE = 1000
    }
}