package fr.prayfortalent

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.blob
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.schema.varchar

object Employee : Table<Nothing>("employee") {
    val name by varchar("name")
    val surname by varchar("surname")
    val hs_java by int("hs_java")
    val hs_python by int("hs_python")
    val hs_javascript by int("hs_javascript")
    val ss_scrum by int("ss_scrum")
    val ss_management by int("ss_management")
    val email by varchar("email").primaryKey()
    val photo by blob("photo")
    val password by varchar("password")
}