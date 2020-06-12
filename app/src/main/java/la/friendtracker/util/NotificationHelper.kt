package la.friendtracker.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import la.friendtracker.R

class NotificationHelper(base: Context):ContextWrapper(base) {

    companion object{
        private val FAF_CHANNEL_ID = "la.friendtracker"
        private val FAF_CHANNEL_NAME = "friendtracker"
    }

    private var manager:NotificationManager?=null
    init{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(defaultUri: Uri?) {
        val fafChannel = NotificationChannel(FAF_CHANNEL_ID,FAF_CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT)

        fafChannel.enableLights(true)
        fafChannel.enableVibration(true)
        fafChannel.lockscreenVisibility=Notification.VISIBILITY_PRIVATE

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()
        fafChannel.setSound(defaultUri!!,audioAttributes)

        getManager()!!.createNotificationChannel(fafChannel)
    }

    public fun getManager(): NotificationManager {
        if(manager == null)
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return manager!!

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getRealTimeTrackingNotification(title:String, content:String):Notification.Builder{
        return Notification.Builder(applicationContext, FAF_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(false)
    }
}