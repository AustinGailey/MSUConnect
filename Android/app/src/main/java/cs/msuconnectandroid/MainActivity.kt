package cs.msuconnectandroid

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.text.Layout
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toolbar
import bolts.Task.delay
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.support.annotation.RawRes
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.Tasks
import cs.msuconnectandroid.R.id.drawer_layout
import cs.msuconnectandroid.MSUConnectObjects.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.util.*
import com.google.firebase.firestore.GeoPoint


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        NavigationView.OnNavigationItemSelectedListener {



    override fun onInfoWindowClick(p0: Marker?) {
        if(p0 != null)
        {
            var eventID = p0.tag as Int
            if(eventID > 0)
                openURL(eventID.toString())
        }
    }

    // mMap initialization
    private lateinit var mMap: GoogleMap
    // location variables
    private val LOCATION_PERMISSION_REQUEST_CODE = 1 // request code 1:
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private lateinit var mEvents: List<CampusEvent>

    var mAuth = FirebaseAuth.getInstance()
    var db = FirebaseFirestore.getInstance()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    private fun getBuilding(): BuildingDataClass? {
        return try {
            //Get "PublicProfile" collection reference
            val aheu_buildingDocRef = db.collection("Maps")
                    .document("DoY34y18iX4JDNx6b5OK")
                    .collection("buildings")
                    .document("YMj99jTFTyFbhwh7Mkdw")
//            val aheu_buildingDocRef = db.collection("test")
//                    .document("me")

            val document = Tasks.await(aheu_buildingDocRef.get())
            //Check if data exists
            if (document.exists()) {
                //Cast the given DocumentSnapshot to our POJO class
                val aheuBuilding = document.toObject(BuildingDataClass::class.java)
                aheuBuilding

            } else null
            //Task successful
        } catch (e: Throwable) {
            //Manage error
            Log.e("Error", "CAN NOT FIND AHEC")
            Log.e("Error", e.toString())
            val text = "Error getting data!"
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(applicationContext, text, duration)
            toast.show()
            null
        }
    }

    // Activity cycle - OnCreate Method loads when Activity is ready
    override fun onCreate(savedInstanceState: Bundle?) {
        // restore savedInstanceState
        super.onCreate(savedInstanceState)
        // TODO: if user is not authenticated redirect to Login Activity, else load Main Activity
//        if (mAuth.currentUser == null) {
//            val intent = Intent(this, Login::class.java)
//            startActivity(intent)
////            setContentView(R.layout.activity_login)
//        } else {
//            setContentView(R.layout.activity_main)
//        }

        setContentView(R.layout.activity_main)
        nav_view.setNavigationItemSelectedListener(this)
        onNavigationItemSelected( nav_view.menu.getItem(1))

        // OM
        var locations = CampusLocations()
        var scraper = CalendarScrapper()
        mEvents = scraper.getEvents(readRaw(R.raw.msu_event), locations)
        // ~ OM


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Enable location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                // DO NOT ZOOM INTO YOUR LOCATION
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
        createLocationRequest()
    }

    // Android back button action
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    // Side Nav Bar actions
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.main_DrawerNav_Map -> {
                var mapsFragment = BlankFragment()
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment, mapsFragment)
                        .commitNow()
//                if(supportFragmentManager.findFragmentById(R.id.map)==null)Log.d("MRB", "No Map")
//                else Log.d("MRB", "Found map")
            }
            R.id.main_DrawerNav_Profile -> {
                // TODO:
//                supportFragmentManager
//                        .beginTransaction()
//                        .replace(R.id.content_main, Profile())
//                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
//                        .show(Profile())
//                        .commit()
            }
            R.id.main_DrawerNav_Settings -> {
//                supportFragmentManager
//                        .beginTransaction()
//                        .replace(R.id.content_main, Settings())
//                        .commit()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // code to get current location
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true;
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    private fun startLocationUpdates() {

        //1
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)

    }

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MainActivity,
                            MainActivity.REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MainActivity.REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val aheuData = getBuilding()


        val aheu = PolygonOptions()
        aheu.fillColor(Color.RED)
        aheu.clickable(true)
        if (aheuData != null) {
            aheuData.markers.forEachIndexed{ index: Int, geoPoint: GeoPoint ->
                aheu.add(LatLng(geoPoint.latitude, geoPoint.longitude))
                mMap.addMarker(MarkerOptions().position(LatLng(geoPoint.latitude, geoPoint.longitude)))

                var aheuPL = googleMap!!.addPolygon(aheu)
            }
        }
//        aheu.add(LatLng(aheuData.markers[0].latitude, aheuData.markers[0].longitude))
//        aheu.add(LatLng(39.743922, -105.006919))
//        aheu.add(LatLng(39.744289, -105.005972))
//        aheu.add(LatLng(39.743434, -105.005331))

        val tivoli = PolygonOptions()
        tivoli.fillColor(Color.RED)
        tivoli.clickable(true)
        tivoli.add(LatLng(39.74453774, -105.00595106))
        tivoli.add(LatLng(39.74548642, -105.00667526))
        tivoli.add(LatLng(39.74596076, -105.00569894))
        tivoli.add(LatLng(39.74492959, -105.00493182))
        var tivoliPL = googleMap!!.addPolygon(tivoli)

        val msub = PolygonOptions()
        msub.fillColor(Color.RED)
        msub.clickable(true)
        msub.add(LatLng(39.74350294, -105.00504351))
        msub.add(LatLng(39.74373393, -105.00522322))
        msub.add(LatLng(39.7437793, -105.00512398))
        msub.add(LatLng(39.74408041, -105.00537879))
        msub.add(LatLng(39.7440723, -105.00542902))
        msub.add(LatLng(39.74409756, -105.00544981))
        msub.add(LatLng(39.7441749, -105.00529357))
        msub.add(LatLng(39.74438269, -105.00545868))
        msub.add(LatLng(39.744591, -105.004995))
        msub.add(LatLng(39.74445189, -105.00487509))
        msub.add(LatLng(39.74440011, -105.00496103))
        msub.add(LatLng(39.74434855, -105.00491946))
        msub.add(LatLng(39.74442434, -105.00474981))
        msub.add(LatLng(39.74412612, -105.00451861))
        msub.add(LatLng(39.74387582, -105.00505081))
        msub.add(LatLng(39.74359779, -105.00483881))
        var msubPL = googleMap!!.addPolygon(msub)

        val aero = PolygonOptions()
        aero.fillColor(Color.RED)
        aero.clickable(true)
        aero.add(LatLng(39.744748, -105.008936))
        aero.add(LatLng(39.744781, -105.008989))
        aero.add(LatLng(39.744748, -105.009032))
        aero.add(LatLng(39.744863, -105.009123))
        aero.add(LatLng(39.744876, -105.009102))
        aero.add(LatLng(39.744946, -105.009182)) //
        aero.add(LatLng(39.744971, -105.009135))
        aero.add(LatLng(39.744991, -105.009172))
        aero.add(LatLng(39.745428, -105.008561)) //
        aero.add(LatLng(39.745362, -105.008470))
        aero.add(LatLng(39.745354, -105.008486))
        aero.add(LatLng(39.745152, -105.008250))
        aero.add(LatLng(39.745094, -105.008320))
        aero.add(LatLng(39.745144, -105.008374))
        var aeroPL = googleMap!!.addPolygon(aero)

        // Add a marker in Sydney and move the camera
        val msu = LatLng(39.743064, -105.006219)
        mMap.addMarker(MarkerOptions().position(msu).title("Marker in MSU Denver"))
        val zoomLevel = 16.0f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(msu, zoomLevel))

        for(event in mEvents)
        {
            var eLatLng = event.getLatLng()
            if(eLatLng != null)
            {
                var randomList = (-2..2).shuffled()
                var xOffset = randomList.first() / 10000.0
                var yOffset = randomList.last() / 10000.0
                var marker = mMap.addMarker(MarkerOptions().position(LatLng(eLatLng.latitude + xOffset, eLatLng.longitude + yOffset)).title(event.getTitle()))
                marker.tag = event.getEventID()
                mMap.setOnInfoWindowClickListener (this)
            }
        }

        setUpMap()
    }

    fun openURL(url : String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://msudenver.edu/events/?trumbaEmbed=view%3Devent%26eventid%3D$url"))
        startActivity(browserIntent)
    }

    fun Context.readRaw(@RawRes resourceId: Int): String {
        return resources.openRawResource(resourceId).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    private fun listenMessages() {
        db.collection("Maps")
                .addSnapshotListener { documentSnapshot, error ->
                    if (error != null) {
                        //Manage error
                    } else if (documentSnapshot != null) {
                        //Manage our documentSnapshot

                    }
                }
    }

    public override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)

    }
}
