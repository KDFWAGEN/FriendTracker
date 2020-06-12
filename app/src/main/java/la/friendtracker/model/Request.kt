package la.friendtracker.model

import java.util.*

class Request {
    lateinit var to:String
    lateinit var data:Map<String,String>

    constructor()
    constructor(to:String, data: Map<String,String>){
        this.to = to
        this.data = data
    }
}