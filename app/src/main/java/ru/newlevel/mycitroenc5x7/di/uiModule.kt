package ru.newlevel.mycitroenc5x7.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.newlevel.mycitroenc5x7.repository.CanRepo
import ru.newlevel.mycitroenc5x7.repository.CanUtils
import ru.newlevel.mycitroenc5x7.ui.MainViewModel
import ru.newlevel.mycitroenc5x7.ui.dashboard.DashboardViewModel
import ru.newlevel.mycitroenc5x7.ui.home.HomeViewModel
import ru.newlevel.mycitroenc5x7.ui.suspension.SuspensionViewModel


val uiModule = module {
    viewModel {
        HomeViewModel(
            canRepo = get()
        )
    }
    single <CanRepo> {
        CanRepo(CanUtils())
    }

    viewModel {
        SuspensionViewModel(
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