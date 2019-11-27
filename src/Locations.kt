package fr.prayfortalent

import io.ktor.locations.Location
import java.util.*
import kotlin.collections.ArrayList

@Location("/session")
class Session {
    @Location("/login")
    class Login {
        // Post data
        data class Data(val mail: String, val password: String)
        data class Return(val mail: String, val name: String, val avatar: String, val isHR: Boolean)
    }

    @Location("/logout")
    class Logout
}

@Location("/search")
class Search {
    data class Data(val skills: ArrayList<String>)
    data class ReturnSingle(val name: String, val surname: String, val mail: String, val photo: String)
}

@Location("/invite")
class Invite {
    data class Data(val employee: List<String>, val subject: String, val body: String, val inviteDate: Date)
    data class Return(val message: String)
}

@Location("/all")
class AllEmployee{
    data class Return(val surname: String, val name: String, val avatar: String)
}

@Location("")
class t {
    data class Data(val employee: List<String>)
    data class Return(val message: String)
}

@Location("/type/{name}")
data class Type(val name: String) {
    @Location("/edit")
    data class Edit(val type: Type)

    @Location("/list/{page}")
    data class List(val type: Type, val page: Int)
}


/*
   - Login and Logout from LDAP
   Find employees with a specific skill
   Send automated mail from an employee result
   Allow the user to recommend five employee on specified (hard and soft) skills per week
 */