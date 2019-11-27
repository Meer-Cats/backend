package fr.prayfortalent

import fr.prayfortalent.model.Employee
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.locations.Locations
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.*
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import io.ktor.util.hex
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import java.io.File
import kotlin.collections.set

fun main(args: Array<String>): Unit = io.ktor.server.tomcat.EngineMain.main(args)

@InternalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    if (MYSQLDB.isEmpty() || MYSQLIP.isEmpty())
        throw Exception("Invalid DB or IP for mysql server")
    Database.connect(
        "jdbc:mysql://$MYSQLIP:3306/$MYSQLDB",
        driver = "com.mysql.jdbc.Driver",
        user = USER,
        password = PASS
    )

    install(Locations) {
    }

    install(Sessions) {
        val key = hex("6819b57a326945c1968f45236589")

        cookie<SessionT>(
            "PFT_SESSION",
            directorySessionStorage(File(".sessions"), cached = true)
        ) {
            cookie.extensions["SameSite"] = "lax"
            cookie.path = "/" // Specify cookie's path '/' so it can be used in the whole site
            transform(
                SessionTransportTransformerMessageAuthentication(
                    key,
                    "HmacSHA256"
                )
            ) // sign the ID that travels to client
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        // Put Angular Here
        static("/s") {
            resources("static")
        }

        post<Session.Login> {
            val data = call.receiveOrNull<Session.Login.Data>()
                ?: return@post call.respond(HttpStatusCode.NotAcceptable, "Invalid Call")

            val q = Employee.select()
                .where {
                    (Employee.email eq data.mail) and (Employee.password eq data.password)
                }

            if (q.totalRecords != 1)
                call.respond(HttpStatusCode.Forbidden, "Invalid password")
            else {
                val employee = q.first()

                call.sessions.set(SessionT(email = data.mail))

                call.respond(
                    Session.Login.Return(
                        employee[Employee.email]!!,
                        employee[Employee.name]!!,
                        employee[Employee.photo]!!.encodeBase64(),
                        employee[Employee.is_humanresources] ?: false
                    )
                )
            }
        }

        post<Session.Logout> {
            if (call.sessions.get<SessionT>() != null)
                call.sessions.clear<SessionT>()
            call.respond("")
        }


        get<Invite> {
            val data = call.receiveOrNull<Invite.Data>()
                ?: return@get call.respond(HttpStatusCode.NotAcceptable, "Invalid Call")
            // sendMail(data.truc)
        }

        get<Search> {
            val data = call.receiveOrNull<Search.Data>()
                ?: return@get call.respond(HttpStatusCode.NotAcceptable, "Invalid Call")

            Employee.select()
                .orderBy(*data.skills.map { skill -> Employee["hs_$skill"].desc() }.toTypedArray())
                .limit(0, 10)
                .map {
                    Search.ReturnSingle(
                        it[Employee.name] ?: "NO NAME",
                        it[Employee.surname] ?: "NO SURNAME",
                        it[Employee.email] ?: "NO EMAIL",
                        it[Employee.photo]?.encodeBase64() ?: ""
                    )
                }.let { call.respond(it) }
        }

        // Register nested routes
        get<Type.Edit> {
            call.respondText("Inside $it")
        }
        get<Type.List> {
            call.respondText("Inside $it")
        }

        get("/session/increment") {
        }

        get("/json/gson") {
        }
    }
}

data class SessionT(val email: String)

