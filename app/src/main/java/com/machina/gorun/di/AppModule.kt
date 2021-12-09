package com.machina.gorun.di

import android.content.Context
import androidx.room.Room
import com.machina.gorun.core.DefaultDispatchers
import com.machina.gorun.core.MyHelper
import com.machina.gorun.data.sources.room.GoRunDao
import com.machina.gorun.data.sources.room.GoRunDatabase
import com.machina.gorun.data.sources.shared_prefs.LocationSharedPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMyHelper(
        @ApplicationContext context: Context
    ): MyHelper = MyHelper(context)

    @Provides
    fun provideDefaultDispatchers() = DefaultDispatchers()

    @Singleton
    @Provides
    fun provideLocationSharedPrefs(
        @ApplicationContext context: Context
    ) = LocationSharedPrefs(context)


    private const val GO_RUN_DB_NAME = "go_run_database"

    /* Room Injection */
    @Singleton
    @Provides
    fun provideGoRunDatabase(
        @ApplicationContext appContext: Context
    ): GoRunDatabase {
        return Room.databaseBuilder(
            appContext,
            GoRunDatabase::class.java,
            GO_RUN_DB_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideNoteDatabaseDao(
        goRunDatabase: GoRunDatabase
    ): GoRunDao {
        return goRunDatabase.goRunDao()
    }
}