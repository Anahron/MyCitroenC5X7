package ru.newlevel.mycitroenc5x7.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.newlevel.mycitroenc5x7.repository.CanRepo
import ru.newlevel.mycitroenc5x7.repository.CanUtils
import ru.newlevel.mycitroenc5x7.ui.MainViewModel
import ru.newlevel.mycitroenc5x7.ui.dashboard.DashboardViewModel
import ru.newlevel.mycitroenc5x7.ui.home.HomeViewModel
import ru.newlevel.mycitroenc5x7.ui.alerts.AlertsViewModel
import kotlin.coroutines.coroutineContext


@OptIn(DelicateCoroutinesApi::class)
val uiModule = module {
    viewModel {
        HomeViewModel(
            canRepo = get()
        )
    }
    single<CoroutineScope> { GlobalScope }
    single <CanRepo> {
        CanRepo(CanUtils(),  get<CoroutineScope>())
    }

    viewModel {
        AlertsViewModel(
            canRepo = get()
        )
    }
    viewModel {
        DashboardViewModel(
            canRepo = get()
        )
    }
    viewModel {
        MainViewModel(
            canRepo = get()
        )
    }
}