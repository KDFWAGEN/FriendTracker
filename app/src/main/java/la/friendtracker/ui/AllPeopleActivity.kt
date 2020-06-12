package la.friendtracker.ui

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_all_people.*
import la.friendtracker.`interface`.IFirebaseLoadDone
import la.friendtracker.R
import la.friendtracker.`interface`.IRecyclerItemClickListener
import la.friendtracker.model.MyResponse
import la.friendtracker.model.Request
import la.friendtracker.model.User
import la.friendtracker.util.Common
import la.friendtracker.viewholder.UserViewHolder
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AllPeopleActivity : AppCompatActivity(), IFirebaseLoadDone {

    var adapter:FirebaseRecyclerAdapter<User, UserViewHolder>?=null
    var searchAdapter:FirebaseRecyclerAdapter<User,UserViewHolder>?=null

    lateinit var iFirebaseLoadDone:IFirebaseLoadDone
    var suggestList:List<String> = ArrayList()

    val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_people)

        //Init View
        material_search_bar.setCardViewElevation(10)
        material_search_bar.addTextChangeListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                TODO("Not yet implemented")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val suggest = ArrayList<String>()
                for (search in suggestList)
                    if(search.toLowerCase(Locale.ROOT).contentEquals(material_search_bar.text.toLowerCase(Locale.ROOT)))
                    suggest.add(search)
                material_search_bar.lastSuggestions = (suggest)
            }
        })
        material_search_bar.setOnSearchActionListener(object:MaterialSearchBar.OnSearchActionListener{
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


        recycler_all_people.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler_all_people.layoutManager = layoutManager
        recycler_all_people.addItemDecoration(DividerItemDecoration(this,layoutManager.orientation))

        iFirebaseLoadDone = this
        loadUserList()
        loadSearchData()
    }

    private fun loadUserList() {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query,User::class.java)
            .build()

        adapter = object:FirebaseRecyclerAdapter<User,UserViewHolder>(options)
        {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): UserViewHolder {
                val itemView = LayoutInflater.from(p0.context)
                    .inflate(R.layout.layout_user, p0,false )
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                if(model.email.equals(Common.loggedUser!!.email)){
                    holder.txt_user_email.text = StringBuilder(model.email!!).append(" (me)")
                    holder.txt_user_email.setTypeface(holder.txt_user_email.typeface,Typeface.ITALIC)
                }
                else{
                    holder.txt_user_email.text = StringBuilder(model.email!!)
                }

                //Event
                holder.setClick(object:IRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        showDialogRequest(model)
                    }

                })

            }

        }
        adapter!!.startListening()
        recycler_all_people.adapter = adapter
    }


    override fun onFirebaseLoadUserDone(lstEmail: List<String>) {
        material_search_bar.lastSuggestions = lstEmail
    }

    override fun onFirebaseLoadFailed(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    private fun startSearch(search_string:String){
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .orderByChild("email")
            .startAt(search_string)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query,User::class.java)
            .build()

        searchAdapter = object:FirebaseRecyclerAdapter<User,UserViewHolder>(options)
        {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_user, parent,false )
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                if(model.email.equals(Common.loggedUser!!.email)){
                    holder.txt_user_email.text = StringBuilder(model.email!!).append("(me)")
                    holder.txt_user_email.setTypeface(holder.txt_user_email.typeface,Typeface.ITALIC)
                }
                else{
                    holder.txt_user_email.text = StringBuilder(model.email!!)
                }

                //Event
                holder.setClick(object: IRecyclerItemClickListener {
                    override fun onItemClickListener(view: View, position: Int) {
                        showDialogRequest(model)

                    }

                })

            }

        }
        searchAdapter!!.startListening()
        recycler_all_people.adapter = searchAdapter
    }

    private fun loadSearchData() {
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)

        userList.addListenerForSingleValueEvent(object:ValueEventListener{
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

    private fun showDialogRequest(model: User) {
        val alertDialog = AlertDialog.Builder(this, R.style.MyRequestDialog)
        alertDialog.setTitle("Request Friend")
        alertDialog.setMessage("Do you want to send request to "+model.email)
        alertDialog.setIcon(R.drawable.ic_baseline_person_add_24)

        alertDialog.setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss()  }

        alertDialog.setPositiveButton("Send") { _, _ ->
            val acceptList = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser!!.uid!!)
                .child(Common.ACCEPT_LIST)

            //Check from User friend's list to make sure it isnt a friend already
            acceptList.orderByKey().equalTo(model.uid)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                       if(p0.value == null) //Is not a friend
                           sendFriendRequest(model)
                        else
                           Toast.makeText(this@AllPeopleActivity,"You and "+model.email+" already are friends!",Toast.LENGTH_SHORT).show()
                    }
                })
        }

        alertDialog.show()
    }

    private fun sendFriendRequest(model: User) {
        //Get token to send friend request
        val tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS)

        tokens.orderByKey().equalTo(model.uid)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.value == null) // token is not available
                        Toast.makeText(this@AllPeopleActivity, "Token Error",Toast.LENGTH_SHORT).show()
                    else{
                        //Create request
                        val request = Request()
                        val dataSend = HashMap<String,String>()
                        dataSend[Common.FROM_UID] = Common.loggedUser!!.uid!! //Logged User UID
                        dataSend[Common.FROM_EMAIL] = Common.loggedUser!!.email!! //Logged User Email
                        dataSend[Common.TO_UID] = model.uid!! //Logged User UID
                        dataSend[Common.TO_EMAIL] = model.email!! //Logged User Email

                        //Set request
                        request.to = p0.child(model.uid!!).getValue(String::class.java)!!
                        request.data = dataSend

                        //Send
                        compositeDisposable.add(Common.fcmService.sendFriendRequestToUser(request)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe ({ t: MyResponse? ->
                                if(t!!.success == 1)
                                    Toast.makeText(this@AllPeopleActivity, "Request sent!",Toast.LENGTH_SHORT).show()
                            },{t: Throwable? ->
                                Toast.makeText(this@AllPeopleActivity,t!!.message,Toast.LENGTH_SHORT).show()

                            }))
                    }
                }

            })
    }

    override fun onStop() {
        if(adapter != null)
            adapter!!.stopListening()
        if(searchAdapter != null)
            searchAdapter!!.stopListening()

        compositeDisposable.clear()
        super.onStop()
    }

}