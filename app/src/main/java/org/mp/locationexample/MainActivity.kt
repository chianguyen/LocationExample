package org.mp.locationexample

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import java.nio.file.Files.size
import android.location.Geocoder
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Handler
import android.os.Message
import java.io.IOException
import java.util.*


/*
*   Thanks to these guys:
*
* 1. Longitude + Latitude:
*   https://www.youtube.com/watch?v=wUkMG3JGA84
*   https://github.com/kmvignesh/LocationExample
*
* > If you watch this ^, he does 2 location: Gps and Network, then
* > pick the one that is more precise.
* > Here I just use the gps one
*
* 2. Reverse Geocoder attempt:
*   https://stackoverflow.com/questions/472313/android-reverse-geocoding-getfromlocation
*
*
*  ** Put the following into Manifest:
*
*     <!--Allow Internet-->
    <uses-permission android:name="android.permission.INTERNET"/>

    <!--Allow location-->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>

*
*
*/

//it's outside because we're using 'const'
private const val PERMISSION_REQUEST = 10

class MainActivity : AppCompatActivity() {

    lateinit var locationManager: LocationManager
    private var hasGps = false

    //object to store location
    private var locationGps: Location? = null

    //permission
    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    //Geocoder handler, used for getting the address
    private var geocoderHandler: GeocoderHandler = GeocoderHandler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Disable this view if the user doesn't give permission
        disableView()

        //Ask for permission based on SDK version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(permissions)) {
                enableView()
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        } else {
            enableView()
        }

        //Get button click
        btn_get_location.setOnClickListener(object: View.OnClickListener{

            override fun onClick(v: View?) {
                getLocation()
            }

        })
    }

    //So guys this just to show/hide the view
    private fun disableView() {
        btn_get_location.isEnabled = false
        btn_get_location.alpha = 0.5F
    }

    private fun enableView() {
        btn_get_location.isEnabled = true
        btn_get_location.alpha = 1F
        btn_get_location.setOnClickListener { getLocation()}
        Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
    }

    //check permission
    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    //On the result of the permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Go to settings and enable the permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (allSuccess)
                enableView()

        }
    }

    //Get the location
    @SuppressLint("MissingPermission")
    private fun getLocation(){

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        //If there is gps
        if(hasGps){

            if(hasGps) {

                Log.d("CodeAndroidLocation", "hasGps")

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object : LocationListener {

                    //Real time location udate
                        override fun onLocationChanged(location: Location?) {
/*                           //This is for non-stop update
                                if (location != null) {
                                locationGps = location
                                text_result.append("\nGPS ")
                                text_result.append("\nLatitude: " + locationGps!!.latitude)
                                text_result.append("\nLongitude: " + locationGps!!.longitude)
                                Log.d("CodeAndroidLocation", " GPS Latitude: " + locationGps!!.latitude)
                                Log.d("CodeAndroidLocation", " GPS Longitude: " + locationGps!!.longitude)
                            }*/
                        }

                        //I think the "status" here is the
                        // 'connected' / 'disconnected'
                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) {

                        }

                        //Well I don't know what the Provider is
                        //But these 2 are the methods generated
                        override fun onProviderEnabled(provider: String?) {

                        }

                        override fun onProviderDisabled(provider: String?) {

                        }

                    })

                //get last known location
                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                //if it's not null, make it the location we want
                if (localGpsLocation != null)
                    locationGps = localGpsLocation

            }

            if(locationGps!=null){
                //The commented code below will show the longitude and latitude as numbers
                /*  text_result.append("\nGPS ")
                    text_result.append("\nLatitude: " + locationGps!!.latitude)
                    text_result.append("\nLongitude: " + locationGps!!.longitude)
                */
                    Log.d("CodeAndroidLocation", " GPS Latitude: " + locationGps!!.latitude)
                    Log.d("CodeAndroidLocation", " GPS Longitude: " + locationGps!!.longitude)

                    //The Handler already set the text_result view the address.
                    getAddressFromLocation(locationGps!!, this,  geocoderHandler)

            }

        } else {
            //if there's no gps, go to the settings
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

    }

    //Get address from the location
    fun getAddressFromLocation(location: Location, context: Context, handler: Handler) {

        val thread = object : Thread() {

            override fun run() {

                val geocoder = Geocoder(context, Locale.getDefault())

                //result store the address
                var result: String? = null

                try {
                    val list = geocoder.getFromLocation(
                        location.latitude, location.longitude, 1
                    )
                    if (list != null && list.size > 0) {
                        val address = list[0]

                        // sending back first address line and locality
                        result = address.getAddressLine(0) + ", " + address.locality
                    }
                }

                catch (e: IOException) {
                    Log.e("error", "Impossible to connect to Geocoder", e)
                }

                finally {
                    val msg = Message.obtain()
                    msg.setTarget(handler)
                    if (result != null) {
                        msg.what = 1
                        val bundle = Bundle()
                        bundle.putString("address", result)
                        msg.setData(bundle)
                    } else
                        msg.what = 0
                    msg.sendToTarget()
                }
            }
        }
        thread.start()
    }


    @SuppressLint("HandlerLeak")    //To handle leaks. generates if you don't write it
                                          //click on the GeocoderHandler
    private inner class GeocoderHandler : Handler() {

        override fun handleMessage(message: Message) {

            val result: String?

            when (message.what) {
                1 -> {
                    val bundle = message.data
                    result = bundle.getString("address")
                }
                else -> result = null
            }
            // Since it's the inner class,
            // it can access text_result from MainActivity
            text_result.setText(result)
        }
    }

}
