package la.friendtracker.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.activity_all_people.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.content_home.material_search_bar
import la.friendtracker.`interface`.IFirebaseLoadDone
import la.friendtracker.`interface`.IRecyclerItemClickListener
import la.friendtracker.R
import la.friendtracker.model.User
import la.friendtracker.service.MyLocationReceiver
import la.friendtracker.util.Common
import la.friendtracker.viewholder.UserViewHolder
import java.util.*
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    IFirebaseLoadDone {

    var adapter: FirebaseRecyclerAdapter<User, UserViewHolder>?=null
    private var searchAdapter: FirebaseRecyclerAdapter<User, UserViewHolder>?=null

    lateinit var iFirebaseLoadDone: IFirebaseLoadDone
    var suggestList:List<String> = ArrayList()

    private lateinit var locationRequest:LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            startActivity(Intent(this@HomeActivity,AllPeopleActivity::class.java))
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        val header = nav_view.getHeaderView(0)
        val user_email = header.findViewById<View>(R.id.user_email) as TextView
        user_email.text = Common.loggedUser!!.email!!

        //View
        material_search_bar.setCardViewElevation(10)
        material_search_bar.addTextChangeListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                TODO("Not yet implemented")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val suggest = ArrayList<String>()
                for(search in suggestList)
                    if (search.toLowerCase(Locale.ROOT).contentEquals(material_search_bar.text.toLowerCase(
                            Locale.ROOT
                        )
                        )
                    )
                    suggest.add(search)
                material_search_bar.lastSuggestions = (suggest)
            }
        })
        material_search_bar.setOnSearchActionListener(object: MaterialSearchBar.OnSearchActionListener{
            override fun onButtonClicked(buttonCode: Int) {
                TODO("Not yet implemented")
            }

            override fun onSearchStateChanged(enabled: Boolean) {
                if (!enabled){
                    //close search -> return default
                    if(adapter != null)
                        recycler_all_people.adapter = adapter
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString())
            }

        })

        recycler_friend_list.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler_friend_list.layoutManager = layoutManager
        recycler_friend_list.addItemDecoration(DividerItemDecoration(this,layoutManager.orientation))

        loadFriendList()
        loadSearchData()

        iFirebaseLoadDone = this

        updateLocation()

    }

    private fun updateLocation() {
        buildLocationRequest()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,getPendingIntent())
    }

    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(this@HomeActivity, MyLocationReceiver::class.java)
        intent.action = MyLocationReceiver.ACTION
        return PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT )

    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.smallestDisplacement = 10f
        locationRequest.fastestInterval = 3000
        locationRequest.interval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun loadSearchData() {
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

        userList.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                iFirebaseLoadDone.onFirebaseLoadFailed(p0.message )
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (userSnapshot in p0.children ){
                    val user = userSnapshot.getValue(User::class.java)
                    lstUserEmail.add(user!!.email!!)
                }
                iFirebaseLoadDone.onFirebaseLoadUserDone(lstUserEmail)
            }
        })
    }

    private fun startSearch(search_string:String){
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)
            .orderByChild("email")
            .startAt(search_string)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query,User::class.java)
            .build()

        searchAdapter = object:FirebaseRecyclerAdapter<User,UserViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_user, parent,false )
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                if(model.email.equals(Common.loggedUser!!.email)){
                    holder.txt_user_email.text = StringBuilder(model.email!!).append("(me)")
                    holder.txt_user_email.setTypeface(holder.txt_user_email.typeface, Typeface.ITALIC)
                }
                else{
                    holder.txt_user_email.text = StringBuilder(model.email!!)
                }

                //Event
                holder.setClick(object: IRecyclerItemClickListener {
                    override fun onItemClickListener(view: View, position: Int) {
                        Common.trackingUser = model
                        startActivity(Intent(this@HomeActivity,TrackingActivity::class.java))
                    }

                })

            }

        }
        searchAdapter!!.startListening()
        recycler_all_people.adapter = searchAdapter
    }

    private fun loadFriendList() {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query,User::class.java)
            .build()

        adapter = object:FirebaseRecyclerAdapter<User,UserViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_user, parent,false )
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, p1: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.setClick(object:IRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        Common.trackingUser = model
                        startActivity(Intent(this@HomeActivity,TrackingActivity::class.java))

                    }

                })

            }

        }
        adapter!!.startListening()
        recycler_friend_list.adapter = adapter
    }

    override fun onStop() {
        if(adapter != null)
            adapter!!.stopListening()
        if(searchAdapter != null)
            searchAdapter!!.stopListening()

        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if(adapter != null)
            adapter!!.startListening()
        if(searchAdapter != null)
            searchAdapter!!.startListening()
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_show_people -> {
                startActivity(Intent(this@HomeActivity,AllPeopleActivity::class.java))
            }
            R.id.nav_friend_request -> {
                startActivity(Intent(this@HomeActivity,FriendRequestActivity::class.java))
            }
            R.id.nav_sign_out -> {

            }

        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onFirebaseLoadUserDone(lstEmail: List<String>) {
        material_search_bar.lastSuggestions = lstEmail
    }

    override fun onFirebaseLoadFailed(message: String) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
    }
}
