package org.awi.fitness.data

object Strings {
    private val strings = mapOf(
        Language.ENGLISH to mapOf(
            StringKey.APP_NAME to "Fitness App",
            StringKey.SIGN_IN to "Sign In",
            StringKey.SIGN_UP to "Sign Up",
            StringKey.EMAIL to "Email",
            StringKey.PASSWORD to "Password",
            StringKey.CREATE_ACCOUNT to "Create Account",
            StringKey.SIGN_IN_CONTINUE to "Sign in to continue",
            StringKey.ALREADY_HAVE_ACCOUNT to "Already have an account?",
            StringKey.DONT_HAVE_ACCOUNT to "Don't have an account?"
        ),
        Language.SPANISH to mapOf(
            StringKey.APP_NAME to "App de Fitness",
            StringKey.SIGN_IN to "Iniciar Sesión",
            StringKey.SIGN_UP to "Registrarse",
            StringKey.EMAIL to "Correo",
            StringKey.PASSWORD to "Contraseña",
            StringKey.CREATE_ACCOUNT to "Crear Cuenta",
            StringKey.SIGN_IN_CONTINUE to "Inicia sesión para continuar",
            StringKey.ALREADY_HAVE_ACCOUNT to "¿Ya tienes una cuenta?",
            StringKey.DONT_HAVE_ACCOUNT to "¿No tienes una cuenta?"
        )
    )

    fun get(key: StringKey, language: Language): String {
        return strings[language]?.get(key) ?: strings[Language.ENGLISH]?.get(key) ?: key.name
    }
} 