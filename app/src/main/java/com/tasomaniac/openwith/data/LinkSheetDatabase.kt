package com.tasomaniac.openwith.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tasomaniac.openwith.data.migrations.Migration1to2
import fe.linksheet.data.dao.*
import fe.linksheet.data.entity.*

@Database(
    entities = [
        PreferredApp::class, AppSelectionHistory::class, WhitelistedBrowser::class,
        ResolvedRedirect::class, LibRedirectDefault::class, LibRedirectServiceState::class,
        DisableInAppBrowserInSelected::class
    ],
    version = 8,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8)
    ],
    exportSchema = true
)
abstract class LinkSheetDatabase : RoomDatabase() {

    abstract fun preferredAppDao(): PreferredAppDao

    abstract fun appSelectionHistoryDao(): AppSelectionHistoryDao

    abstract fun whitelistedBrowsersDao(): WhitelistedBrowsersDao
    abstract fun disableInAppBrowserInSelectedDao(): DisableInAppBrowserInSelectedDao
    abstract fun resolvedRedirectDao(): ResolvedRedirectDao

    abstract fun libRedirectDefaultDao(): LibRedirectDefaultDao

    abstract fun libRedirectServiceStateDao(): LibRedirectServiceStateDao
}
