package la.friendtracker.viewholder

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_friend_request.view.*
import la.friendtracker.R

class FriendRequestViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
    var txt_user_email: TextView
    var btn_accept: ImageView
    var btn_decline: ImageView
    init {
        txt_user_email = itemView.findViewById(R.id.txt_user_email) as TextView
        btn_accept = itemView.findViewById(R.id.btn_accept) as ImageView
        btn_decline = itemView.findViewById(R.id.btn_decline) as ImageView
    }
}