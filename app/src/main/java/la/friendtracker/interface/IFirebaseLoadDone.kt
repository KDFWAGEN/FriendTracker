package la.friendtracker.`interface`

interface IFirebaseLoadDone {
    fun onFirebaseLoadUserDone(lstEmail:List<String>)
    fun onFirebaseLoadFailed(message:String)
}