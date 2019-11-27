@file:Suppress("SENSELESS_COMPARISON")

package fr.prayfortalent

import io.ktor.locations.Location
import java.util.*

abstract class CheckedData {
    abstract fun valid(): Boolean
}

@Location("/session")
class Session {
    @Location("/login")
    class Login {
        // Post data
        data class Data(val mail: String, val password: String) : CheckedData() {
            override fun valid() = mail != null && password != null
        }

        data class Return(val mail: String, val name: String, val avatar: String, val isHR: Boolean)
    }

    @Location("/logout")
    class Logout
}

@Location("/employee")
class Employees {
    @Location("/search")
    class Search(val skills: ArrayList<String>) : CheckedData() {
        override fun valid() = skills != null


        data class ReturnSingle(val name: String, val surname: String, val mail: String, val photo: String)
    }

    @Location("/invite")
    class Invite {
        data class Data(val employee: List<String>, val subject: String, val body: String, val inviteDate: Date) :
            CheckedData() {
            override fun valid() = employee != null && subject != null && body != null && inviteDate != null
        }

        data class Return(val message: String)
    }

    @Location("/recommend")
    class Recommend {
        data class Data(val key: String, val employee: String) : CheckedData() {
            override fun valid() = employee != null && key != null
        }

        data class Return(val key: String, val message: String)
    }

    @Location("/all")
    class AllEmployee {
        data class Return(val mail: String, val surname: String, val name: String, val avatar: String)
    }
}


/*
   - Login and Logout from LDAP
   Find employees with a specific skill
   Send automated mail from an employee result
   Allow the user to recommend five employee on specified (hard and soft) skills per week
 */