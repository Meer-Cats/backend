package fr.prayfortalent

import fr.prayfortalent.model.Employee
import fr.prayfortalent.model.Skill
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.default
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.locations.Locations
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.sessions.*
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import io.ktor.util.hex
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import java.io.File
import java.time.Duration
import java.util.*
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

    // This adds Date and Server headers to each response, and allows custom additional headers
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)

    install(Locations)

    install(CORS) {
        // method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.ContentType)
        header(HttpHeaders.AccessControlAllowOrigin)
        allowCredentials = true
        anyHost()
        maxAge = Duration.ofDays(1)
        // method(HttpMethod.Options)
        /*
        header(HttpHeaders.XForwardedProto)
        exposeHeader(HttpHeaders.AccessControlAllowOrigin)
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Head)

        anyHost()



        allowCredentials = true
        allowNonSimpleContentTypes = true
        maxAge = Duration.ofDays(1)
         */
        /*
        header(HttpHeaders.AccessControlAllowOrigin)
        header(HttpHeaders.AccessControlAllowMethods)
        header(HttpHeaders.AccessControlAllowHeaders)


        anyHost()
        host("localhost:4200")

        allowCredentials = true
        allowNonSimpleContentTypes = true

        maxAge = Duration.ofDays(1)
        */
    }

    install(Sessions) {
        val key = hex("6819b57a326945c1968f45236589")

        cookie<SessionT>(
            "PFT_SESSION",
            directorySessionStorage(File(".sessions"), cached = false)
        ) {
            cookie.extensions["SameSite"] = "Lax"
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
        static {
            default("static/index.html")
            resources("static")
        }

        post<Session.Login> {
            val data = ca   ll.receiveOrNull<Session.Login.Data>()

            if (data == null || !data.valid())
                return@post call.respond(HttpStatusCode.NotAcceptable, "Invalid Call")

            val q = Employee.select()
                .where {
                    (Employee.email eq data.mail) and (Employee.password eq data.password)
                }

            if (q.totalRecords != 1)
                call.respond(HttpStatusCode.Forbidden, "Invalid password")
            else {
                val employee = q.first()

                call.sessions.set(SessionT(email = data.mail, gotQuestions = mutableListOf()))

                Session.Login.Return(
                    employee[Employee.email]!!,
                    employee[Employee.name]!!,
                    employee[Employee.photo]!!.encodeBase64(),
                    employee[Employee.is_humanresources] ?: false
                ).let {
                    call.respond(it)
                }
            }
        }

        post<Session.Logout> {
            if (call.sessions.get<SessionT>() != null)
                call.sessions.clear<SessionT>()
            call.respond("")
        }

        // Generate a request
        get<Employees.Recommend> {
            val session = call.sessions.get<SessionT>()
                ?: return@get call.respond(HttpStatusCode.Forbidden, "Invalid session")

            val skill = Skill.values().random()
            val key = UUID.randomUUID().toString()
            val message = "Who would you ask for help if you had a question about ${skill.s} ?"

            session.gotQuestions.add(Question(skill, key))
            call.respond(Employees.Recommend.Return(key, message))
        }

        // Send a recommendation
        post<Employees.Recommend> {
            val session = call.sessions.get<SessionT>()
                ?: return@post call.respond(HttpStatusCode.Forbidden, "Invalid session")
            val data = call.receiveOrNull<Employees.Recommend.Data>()

            if (data == null || !data.valid())
                return@post call.respond(HttpStatusCode.NotAcceptable, "Invalid Call")

            val matchingQuestion = session.gotQuestions.firstOrNull { it.key == data.key }
                ?: return@post call.respond(HttpStatusCode.Forbidden, "Wrong Key")

            // Employee[]
        }

        get<Employees.Invite> {
            val data = call.receiveOrNull<Employees.Invite.Data>()

            if (data == null || !data.valid())
                return@get call.respond(HttpStatusCode.NotAcceptable, "Invalid Call")
        }

        get<Employees.Search> { data ->
            if (!data.valid())
                return@get call.respond(HttpStatusCode.NotAcceptable, "Invalid Call")

            Employee.select()
                .orderBy(*data.skills.map { skill ->

                    val enumedSkill = Skill.values()
                        .firstOrNull { it.s.toLowerCase() == skill.toLowerCase() }
                        ?: Skill.JAVA

                    val r = (if (enumedSkill.isHard) "hs" else "ss") + (enumedSkill.s.toLowerCase())
                    Employee[r].desc()
                }.toTypedArray())
                .limit(0, 10)
                .map {
                    Employees.Search.ReturnSingle(
                        it[Employee.name] ?: "NO NAME",
                        it[Employee.surname] ?: "NO SURNAME",
                        it[Employee.email] ?: "NO EMAIL",
                        it[Employee.photo]?.encodeBase64() ?: ""
                    )
                }.let { call.respond(it) }
        }

        // Register nested routes
        get<Employees.AllEmployee> {
            call.respond(
                Employee.select(listOf(Employee.surname, Employee.name, Employee.photo, Employee.email))
                    .map {
                        Employees.AllEmployee.Return(
                            it[Employee.email] ?: "NO EMAIL",
                            it[Employee.surname] ?: "NO SURNAME",
                            it[Employee.name] ?: "NO NAME",
                            it[Employee.photo]?.encodeBase64() ?: ""
                        )
                    }
            )
        }
    }
}

data class SessionT(val email: String, val gotQuestions: MutableList<Question>)
data class Question(val skill: Skill, val key: String)

