package fr.prayfortalent.model

import me.liuwj.ktorm.schema.*

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
    val is_humanresources by boolean("is_humanresources")
}

enum class Skill(val s: String, val isHard: Boolean) {
    PYTHON("Python", true),
    JAVA("Java", true),
    JAVASCRIPT("Javascript", true),
    SCRUM("Scrum", false),
    MANAGEMENT("Management", false),
}