package la.friendtracker.`interface`

import android.view.View

interface IRecyclerItemClickListener {
    fun onItemClickListener(view: View,position: Int)
}