package la.friendtracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationResult
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.paperdb.Paper
import la.friendtracker.util.Common

class MyLocationReceiver:BroadcastReceiver(){
    private var publicLocation:DatabaseReference = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION)
    lateinit var uid:String

    companion object{
        const val ACTION = "la.friendtracker.UPDATE_LOCATION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Paper.init(context!!)

        uid = Paper.book().read(Common.USER_UID_SAVE_KEY)
        if (intent != null ){
            val action = intent.action
            if (action == ACTION){
                val result = LocationResult.extractResult(intent)
                if (result !=null){
                    val location = result.lastLocation
                    if(Common.loggedUser !=null){

                        //App is running
                        publicLocation.child(Common.loggedUser!!.uid!!).setValue(location)
                    }
                    else{
                        //App is onStop()
                        publicLocation.child(uid).setValue(location)

                    }
                }
            }
        }

    }

}