package la.friendtracker.util

import android.os.health.TimerStat
import la.friendtracker.model.User
import la.friendtracker.remote.IFCMService
import la.friendtracker.remote.RetrofitClient
import java.sql.Time
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


object Common{
    fun convertTimeStampToDate(time: Long): Date {
        return Date(Timestamp(time).time)

    }

    fun getDateFormatted(date: Date): String? {
        return SimpleDateFormat("dd-MM-yyyy HH:mm").format(date).toString()
    }
    var loggedUser: User ?= null
     var trackingUser: User?= null
    val PUBLIC_LOCATION: String = "PublicLocation"
    val FRIEND_REQUEST: String = "FriendRequest"
    val TO_EMAIL: String = "ToName"
    val TO_UID: String = "ToUid"
    val FROM_EMAIL: String = "FromName"
    val FROM_UID: String = "FromUid"
    val ACCEPT_LIST: String = "acceptList"
    val USER_UID_SAVE_KEY: String ="SAVE_KEY"
    val TOKENS: String = "Tokens"
    val USER_INFORMATION: String = "UserInformation"


    val fcmService: IFCMService
        get() = RetrofitClient.getClient("https://fcm.googleapis.com/")
            .create(IFCMService::class.java)

}