package com.notes.presentation.navigation

object NavRoutes {
    object Args {
        const val MSG = "msg"
    }

    object Screens {
        const val SPLASH = "splash"
        const val HOME = "home?${Args.MSG}={${Args.MSG}}"
    }

    fun home(msg: String) = "home?${Args.MSG}=$msg"

    object DeepLinks {
        const val SPLASH = "myapp://notes/splash"
        const val HOME = "myapp://notes/home?${Args.MSG}={${Args.MSG}}"
    }
}
