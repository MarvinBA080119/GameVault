package com.example.gamevault.onboarding.personal.model

data class UserProfile(
    val id: String = "",
    val nombre: String = "",
    val segundoNombre: String = "",
    val primerApellido: String = "",
    val segundoApellido: String = "",
    val nombreUsuario: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",
    val fotoUrl: String = ""
) {
    fun nombreCompleto(): String =
        listOf(nombre, segundoNombre, primerApellido, segundoApellido)
            .filter { it.isNotBlank() }
            .joinToString(" ")
}