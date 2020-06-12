package la.friendtracker.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.activity_all_people.*
import kotlinx.android.synthetic.main.activity_all_people.material_search_bar
import kotlinx.android.synthetic.main.activity_friend_request.*
import la.friendtracker.`interface`.IFirebaseLoadDone
import la.friendtracker.R
import la.friendtracker.model.User
import la.friendtracker.util.Common
import la.friendtracker.viewholder.FriendRequestViewHolder
import java.util.*
import kotlin.collections.ArrayList

class FriendRequestActivity : AppCompatActivity(), IFirebaseLoadDone {

    var adapter: FirebaseRecyclerAdapter<User, FriendRequestViewHolder>?=null
    private var searchAdapter: FirebaseRecyclerAdapter<User, FriendRequestViewHolder>?=null

    lateinit var iFirebaseLoadDone: IFirebaseLoadDone
    var suggestList:List<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request)

        //Init View
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
                if(search.toLowerCase(Locale.ROOT).contentEquals(material_search_bar.text.toLowerCase(
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
                        recycler_friend_request.adapter = adapter
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString())
            }

        })

        recycler_friend_request.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler_friend_request.layoutManager = layoutManager
        recycler_friend_request.addItemDecoration(DividerItemDecoration(this,layoutManager.orientation))

        iFirebaseLoadDone = this
        loadFriendRequestList()
        loadSearchData()

    }

    private fun loadSearchData() {
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

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
            .child(Common.FRIEND_REQUEST)
            .orderByChild("email")
            .startAt(search_string)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query,User::class.java)
            .build()

        searchAdapter = object:FirebaseRecyclerAdapter<User,FriendRequestViewHolder>(options)
        {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_friend_request, parent,false )
                return FriendRequestViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int, model: User) {
                //Event
                holder.txt_user_email.text = model.email
                holder.btn_decline.setOnClickListener{
                    //Delete
                    deleteFriendRequest(model,true)
                }
                holder.btn_accept.setOnClickListener{
                    deleteFriendRequest(model,false)
                    addToAcceptList(model) //Add user to friend list
                    addUserToFriendContact(model) // add yourself to user contacts
                }

            }

        }
        searchAdapter!!.startListening()
        recycler_all_people.adapter = searchAdapter
    }

    private fun loadFriendRequestList() {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query,User::class.java)
            .build()

        adapter = object:FirebaseRecyclerAdapter<User,FriendRequestViewHolder>(options){
            override fun onCreateViewHolder(
                p0: ViewGroup,
                p1: Int
            ): FriendRequestViewHolder {
                val itemView = LayoutInflater.from(p0.context)
                    .inflate(R.layout.layout_friend_request, p0,false )
                return FriendRequestViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: FriendRequestViewHolder, p1: Int, model: User) {
                holder.txt_user_email.text = model.email
                holder.btn_decline.setOnClickListener{
                    //Delete
                    deleteFriendRequest(model,true)
                }
                holder.btn_accept.setOnClickListener{
                    deleteFriendRequest(model,false)
                    addToAcceptList(model) //Add user to friend list
                    addUserToFriendContact(model) // add yourself to user contacts
                }
            }

        }
        adapter!!.startListening()
        recycler_friend_request.adapter = adapter
    }

    private fun addUserToFriendContact(model: User) {
        val acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(model.uid!!)
            .child(Common.ACCEPT_LIST)

        acceptList.child(Common.loggedUser!!.uid!!).setValue(Common.loggedUser)
    }

    private fun addToAcceptList(model: User) {
        val acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

        acceptList.child(model.uid!!).setValue(model)

    }

    private fun deleteFriendRequest(model: User, isShowMessage: Boolean) {
        val friendRequest = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

        friendRequest.child(model.uid!!).removeValue()
            .addOnSuccessListener {
                if(isShowMessage)
                    Toast.makeText(this@FriendRequestActivity,"Friend Request Denied!",Toast.LENGTH_SHORT).show()
            }

    }

    override fun onStop(){
        if(adapter != null)
            adapter!!.stopListening()
        if(searchAdapter != null)
            searchAdapter!!.stopListening()
        super.onStop()
    }
    override fun onFirebaseLoadUserDone(lstEmail: List<String>) {
        material_search_bar.lastSuggestions = lstEmail
    }

    override fun onFirebaseLoadFailed(message: String) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
    }
}