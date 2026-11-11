package ua.kpi.iot_lighting.prac2

import android.app.Application
import ua.kpi.iot_lighting.prac2.di.AppContainer

class Prac2Application : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
