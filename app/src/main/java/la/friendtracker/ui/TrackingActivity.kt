package la.friendtracker.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import la.friendtracker.R
import la.friendtracker.model.MyLocation
import la.friendtracker.util.Common

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback, ValueEventListener {

    private lateinit var mMap: GoogleMap
    lateinit var trackingUserLocation:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerEventRealtime()
    }

    private fun registerEventRealtime() {
        trackingUserLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION)
            .child(Common.trackingUser!!.uid!!)

        trackingUserLocation.addValueEventListener(this)
    }

    override fun onResume() {
        super.onResume()
        trackingUserLocation.addValueEventListener(this)
    }

    override fun onStop() {
        trackingUserLocation.removeEventListener(this )
        super.onStop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        //Skin
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.my_uber_style))
    }

    override fun onCancelled(p0: DatabaseError) {
        TODO("Not yet implemented")
    }

    override fun onDataChange(p0: DataSnapshot) {
        if(p0.value != null){
            val location = p0.getValue(MyLocation::class.java)
            //Add Marker
            val userMarker = LatLng(location!!.latitude,location.longitude)
            mMap.addMarker(MarkerOptions().position(userMarker).title(Common.trackingUser!!.email)
                .snippet(Common.getDateFormatted(Common.convertTimeStampToDate(location.time))))

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker,16f))
        }
    }
}