package fr.prayfortalent

import io.ktor.locations.Location

@Location("/session")
class Session() {
    @Location("/login")
    data class Login(val mail: String, val password: String)

    @Location("/logout")
    class Logout()
}

@Location("/type/{name}") data class Type(val name: String) {
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