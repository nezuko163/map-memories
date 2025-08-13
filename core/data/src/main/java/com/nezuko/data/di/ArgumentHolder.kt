package com.nezuko.data.di

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.Volatile

@Singleton
class ArgumentHolder @Inject constructor() {

    @Volatile
    var query = ""
}
