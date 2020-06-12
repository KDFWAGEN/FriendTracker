package la.friendtracker.remote

import android.app.DownloadManager
import io.reactivex.Observable
import la.friendtracker.model.MyResponse
import la.friendtracker.model.Request
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.*

interface IFCMService {
    @Headers("Content-Type:application/json",
        "Authorization:key=AAAAaxbVwwE:APA91bEVgmRQzE4m2WtYM7Yrdin_OOipMvl2gRpOcTn_To0agNFVCSR99iA2-P-BTSVcyyOynx1Mplnfsp0YSX4001jyx37wthEKcoFFl8CbKen4pThiXczciWVhDG61g4VpmZfKbtX7")
    @POST("fcm/send")
    fun sendFriendRequestToUser(@Body body: Request):Observable<MyResponse>
}