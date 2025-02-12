package ru.newlevel.mycitroenc5x7.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.newlevel.mycitroenc5x7.repository.CanRepo
import ru.newlevel.mycitroenc5x7.repository.CanUtils
import ru.newlevel.mycitroenc5x7.repository.DayTripRepository
import ru.newlevel.mycitroenc5x7.ui.MainViewModel
import ru.newlevel.mycitroenc5x7.ui.alerts.AlertsViewModel
import ru.newlevel.mycitroenc5x7.ui.dashboard.DashboardViewModel
import ru.newlevel.mycitroenc5x7.ui.home.HomeViewModel


@OptIn(DelicateCoroutinesApi::class)
val uiModule = module {
    viewModel {
        HomeViewModel(
            canRepo = get(),
            dayTripRepository = get()
        )
    }
    single<CoroutineScope> { GlobalScope }
    single <CanRepo> {
        CanRepo(CanUtils(),  get<CoroutineScope>())
    }
    single { DayTripRepository(androidApplication()) }

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