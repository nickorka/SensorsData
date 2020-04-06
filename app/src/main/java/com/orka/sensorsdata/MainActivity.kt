package com.orka.sensorsdata

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File

import com.google.android.gms.location.*


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAcc: Sensor
    private lateinit var sensorGravity: Sensor
    private lateinit var sensorGyro: Sensor
    private lateinit var dirPath: String
    private lateinit var accFilePath: String
    private lateinit var gravityFilePath: String
    private lateinit var gyroFilePath: String
    private lateinit var gmsFilePath: String

    val PERMISSION_ID = 42
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest

    private fun createSensorFile(suffix: String): Boolean {

        val filePath = when (suffix) {
            "acc" -> {
                accFilePath = dirPath + filePrefix.text.toString() + "_acc.csv"
                accFilePath
            }
            "gravity" -> {
                gravityFilePath = dirPath + filePrefix.text.toString() + "_gravity.csv"
                gravityFilePath
            }
            "gyro" -> {
                gyroFilePath = dirPath + filePrefix.text.toString() + "_gyro.csv"
                gyroFilePath
            }
            "gms" -> {
                gmsFilePath = dirPath + filePrefix.text.toString() + "_gms.csv"
                gmsFilePath
            }
            else -> "NULL"
        }

        if (filePath != "NULL") {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            return (true)
        } else
            return (false)


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED)
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationCallback()

        fabStart.setOnClickListener { view ->
            Snackbar.make(view, "Register Sensors", Snackbar.LENGTH_LONG).show()

            dirPath = getExternalFilesDir("MyFileStorage").absolutePath + "/SensorsData/"
            val dir = File(dirPath)
            dir.mkdirs()
            createSensorFile("acc")
            createSensorFile("gravity")
            createSensorFile("gyro")
            createSensorFile("gms")

            sensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager.registerListener(this, sensorGyro, SensorManager.SENSOR_DELAY_NORMAL)

            requestNewLocationData()
        }

        fabStop.setOnClickListener { view ->
            Snackbar.make(view, "Unregister listeners", Snackbar.LENGTH_LONG).show()
            sensorManager.unregisterListener(this)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAccuracyChanged(p0: Sensor, p1: Int) {
        when (p0.type) {

        }
    }

    private fun getAccuracyString(accuracy: Int): String =
        when(accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "HIGH"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "MEDIUM"
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "LOW"
            else -> "UNKNOWN"
        }
    
    override fun onSensorChanged(p0: SensorEvent) {
        val accuracy = getAccuracyString(p0.accuracy)
        when (p0.sensor.type) {
            Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> {
                accAccuracy.setText(accuracy)
                accX.setText(p0.values[0].toString())
                accY.setText(p0.values[1].toString())
                accZ.setText(p0.values[2].toString())

                val accFile = File(accFilePath)
                accFile.appendText(
                    listOf(
                        p0.timestamp.toString(),
                        accuracy,
                        p0.values[0].toString(),
                        p0.values[1].toString(),
                        p0.values[2].toString()
                    ).joinToString(separator = ",") + "\n"
                )
            }
            Sensor.TYPE_GRAVITY -> {
                gravityAccuracy.setText(accuracy)
                gravityX.setText(p0.values[0].toString())
                gravityY.setText(p0.values[1].toString())
                gravityZ.setText(p0.values[2].toString())

                val accFile = File(gravityFilePath)
                accFile.appendText(
                    listOf(
                        p0.timestamp.toString(),
                        accuracy,
                        p0.values[0].toString(),
                        p0.values[1].toString(),
                        p0.values[2].toString()
                    ).joinToString(separator = ",") + "\n"
                )
            }
            Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> {
                gyroAccuracy.setText(accuracy)
                gyroX.setText(p0.values[0].toString())
                gyroY.setText(p0.values[1].toString())
                gyroZ.setText(p0.values[2].toString())

                val accFile = File(gyroFilePath)
                accFile.appendText(
                    listOf(
                        p0.timestamp.toString(),
                        accuracy,
                        p0.values[0].toString(),
                        p0.values[1].toString(),
                        p0.values[2].toString()
                    ).joinToString(separator = ",") + "\n"
                )
            }
        }
    }

    /*
    Fused location
     */

    private fun checkPermissions(): Boolean {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
//            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
//            LocationManager.NETWORK_PROVIDER
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    }
                    else {
                        gmsAccuracy.text = location.accuracy.toString()
                        gmsLatitude.text = location.latitude.toString()
                        gmsLongitude.text = location.longitude.toString()
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        mLocationRequest = LocationRequest.create()?.apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 500
        }!!
//        mLocationRequest.interval = 0
//        mLocationRequest.fastestInterval = 0
//        mLocationRequest.numUpdates = 1

//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
            mLocationCallback,
//            Looper.myLooper()
            Looper.getMainLooper()
        )
    }


    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                var mLastLocation: Location = locationResult.lastLocation
                gmsAccuracy.text = mLastLocation.accuracy.toString()
                gmsLatitude.text = mLastLocation.latitude.toString()
                gmsLongitude.text = mLastLocation.longitude.toString()

                val gmsFile = File(gmsFilePath)
                for(location in locationResult.locations) {
                    gmsFile.appendText(
                        listOf(
                            location.time.toString(),
                            location.accuracy.toString(),
                            location.latitude.toString(),
                            location.longitude.toString()
                        ).joinToString(separator = ",") + "\n"
                    )
                }
            }
        }
    }
}

