package ua.kpi.iot_lighting.prac2.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import ua.kpi.iot_lighting.prac2.Prac2Application
import ua.kpi.iot_lighting.prac2.di.AppContainer

/** Дістає DI-контейнер із CreationExtras для фабрик ViewModel. */
fun CreationExtras.appContainer(): AppContainer {
    val application = this[APPLICATION_KEY] as Prac2Application
    return application.container
}
