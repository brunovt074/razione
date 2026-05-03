package com.recipecostcalculator.infrastructure.persistence.sqlite

import android.content.Context

object AndroidSQLiteRuntimeContext {
    private var runtimeContext: Context? = null

    fun set(context: Context) {
        runtimeContext = context.applicationContext
    }

    fun requireContext(): Context {
        return runtimeContext
            ?: throw IllegalStateException("Android context no inicializado para SQLite")
    }
}
