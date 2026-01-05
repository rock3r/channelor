package dev.sebastiano.channelor.di

import android.content.Context
import android.net.wifi.WifiManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWifiManager(@ApplicationContext context: Context): WifiManager {
        return context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    @Provides
    @Singleton
    fun provideWifiRepository(
        wifiRepositoryImpl: dev.sebastiano.channelor.data.WifiRepositoryImpl
    ): dev.sebastiano.channelor.data.WifiRepository {
        return wifiRepositoryImpl
    }
}
