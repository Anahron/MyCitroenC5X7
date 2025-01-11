package ru.newlevel.mycitroenc5x7.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.newlevel.mycitroenc5x7.repository.CanRepo
import ru.newlevel.mycitroenc5x7.repository.CanUtils
import ru.newlevel.mycitroenc5x7.ui.home.HomeViewModel


val uiModule = module {
    viewModel {
        HomeViewModel(
            canRepo = get()
        )
    }
    single <CanRepo> {
        CanRepo(CanUtils())
    }
}