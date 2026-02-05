package com.francoherrero.ai_agent_multiplatform.di

import com.francoherrero.ai_agent_multiplatform.BuildConfig
import com.francoherrero.ai_agent_multiplatform.api.createHttpClient
import com.francoherrero.ai_agent_multiplatform.auth.AuthApi
import com.francoherrero.ai_agent_multiplatform.auth.AuthRepository
import com.francoherrero.ai_agent_multiplatform.auth.TokenStorage
import com.francoherrero.ai_agent_multiplatform.auth.TokenStorageImpl
import com.francoherrero.ai_agent_multiplatform.trips.UserTripsApi
import com.francoherrero.ai_agent_multiplatform.trips.UserTripsRepository
import com.francoherrero.ai_agent_multiplatform.ui.auth.LoginViewModel
import com.francoherrero.ai_agent_multiplatform.ui.auth.RegisterViewModel
import com.francoherrero.ai_agent_multiplatform.ui.trips.TripDetailsViewModel
import com.francoherrero.ai_agent_multiplatform.ui.trips.TripViewModel
import com.parkwoocheol.kmpdatastore.TypeSafeDataStore
import io.ktor.client.HttpClient
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.*

val dataModule = module {
    single<TypeSafeDataStore> { TypeSafeDataStore("app_preferences") }

    single<TokenStorageImpl> { TokenStorageImpl(get()) } bind TokenStorage::class

    single<HttpClient> {
        createHttpClient(BuildConfig.SERVER_BASE_URL, get())
    }
}

val apiModule = module {
    single<AuthApi> {
        AuthApi(
           get()
        )
    }

    single<UserTripsApi> {
        UserTripsApi(
            client = get(),
        )
    }

    single<UserTripsRepository> {
        UserTripsRepository(
            userTripsApi = get()
        )
    }

    single<AuthRepository> {
        AuthRepository(
            authApi = get(),
            tokenStorage = get()
        )
    }
}

val viewModelModule = module {
    viewModel<LoginViewModel>()
    viewModel<RegisterViewModel>()
    viewModel<TripDetailsViewModel>()
    viewModel<TripViewModel>()
}