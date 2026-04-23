package com.erivaldogelson.remedios.core

import android.content.Context
import com.erivaldogelson.remedios.RemediosApplication

val Context.appContainer: AppContainer
    get() = (applicationContext as RemediosApplication).container

