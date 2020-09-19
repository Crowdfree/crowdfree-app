package com.huawei.hackzurich

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.location.*
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


class MainActivity : AppCompatActivity() {
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val mLocationRequest: LocationRequest? = null
    lateinit var layoutManager:LinearLayoutManager
    lateinit var adapter: RestaurantAdapter

     override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         val textView = findViewById<View>(R.id.textView) as TextView
         textView.text = "Loading Searches..."
         textView.postDelayed({ textView.text = "Your Searches are:" }, 8500)
         layoutManager = LinearLayoutManager(this)

         getPermissions()
         getLocation()


     }

    override fun onResume() {
        super.onResume()
    }

    private suspend fun runHmsConfigurationCheck() {
        testHmsCorePresence()
        testAccountByRequestingPushNotificationsToken()
    }

    private suspend fun testAccountByRequestingPushNotificationsToken() {
        val pushToken = withContext(Dispatchers.IO) {
            HmsUtils.getPushNotificationsToken(this@MainActivity)
        }
        check(pushToken.isNotEmpty()) { "Push notifications token retrieved, but empty. Clear app data and try again." }
    }

    private fun testHmsCorePresence() {
        check(HmsUtils.isHmsAvailable(this)) { "Please make sure you have HMS Core installed on the test device." }
    }

    private fun getLocation(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mLocationCallback: LocationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                  var currentLatitude = locationResult.lastLocation.latitude
                 var currentLongitude = locationResult.lastLocation.longitude
                Log.e("CrowdFree", "hi" + currentLatitude + " " + currentLongitude);
                getNearbyPlaces(locationResult);


            }

        }

        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1800000  // change the interval as needed
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.getMainLooper()
        ).addOnSuccessListener{
            Log.e("CrowdFree", "It works!");



        }.addOnFailureListener{

               Log.e("CrowdFree", it.localizedMessage);
            }

        }


    private fun getNearbyPlaces(locationResult: LocationResult){
        lateinit var siteRestaurantList: List<Site>
        var request = NearbySearchRequest()
        request.poiType = LocationType.RESTAURANT
        request.location = Coordinate(
            locationResult.lastLocation.latitude,
            locationResult.lastLocation.longitude
        )
        request.radius = 6000   // meter
        request.pageIndex = 1
        val API_KEY = "client/api_key"
        val apiKey = AGConnectServicesConfig.fromContext(this).getString(API_KEY)
        val searchService = SearchServiceFactory.create(this, getApiKey(apiKey))
        searchService.nearbySearch(request, object : SearchResultListener<NearbySearchResponse> {
            override fun onSearchError(status: SearchStatus?) {
                Log.e("onSearchError", "" + status)
            }

            override fun onSearchResult(response: NearbySearchResponse?) {
                response?.let {
                    siteRestaurantList = it.sites
                    Log.e("CrowdFree", "Listreturned")
                    setRestaurantRecyclerView(siteRestaurantList)
                }
            }
        })
    }

    private fun getApiKey(apiKey: String): String? {
        // get apiKey from AppGallery Connect
        // need encodeURI the apiKey
        return try {
            URLEncoder.encode(apiKey, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            Log.e("CrowdFree", "encode apikey error")
            null
        }
    }
    private fun getPermissions(){
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i("CrowdFree", "sdk < 28 Q")
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED

            ) {
                val strings = arrayOf<String>(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,

                    )
                ActivityCompat.requestPermissions(this, strings, 1)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    "android.permission.ACCESS_BACKGROUND_LOCATION"
                ) != PackageManager.PERMISSION_GRANTED

            ) {
                val strings = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    "android.permission.ACCESS_BACKGROUND_LOCATION"

                )
                ActivityCompat.requestPermissions(this, strings, 2)
            }
        }
     }
    private fun setRestaurantRecyclerView(siteRestaurantList: List<Site>){
        recyclerView.layoutManager = layoutManager

        adapter = RestaurantAdapter(siteRestaurantList)
        recyclerView.adapter =adapter
    }
}

