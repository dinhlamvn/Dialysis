package com.dialysis.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.execSQL
import com.dialysis.app.domain.water.HydrationRatio

/**
 * v5 -> v6: water_entries gets `client_id` (stable id for `POST mobile/sync`), `weight_ratio` and
 * `created_at_local`. The table is rebuilt so the result matches Room's v6 schema exactly; every
 * existing row (synced or pending) is kept and gets a new UUID-shaped client_id.
 */
internal val MIGRATION_5_6_STATEMENTS: List<String> = listOf(
    "CREATE TABLE IF NOT EXISTS `water_entries_new` (" +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
        "`client_id` TEXT NOT NULL, " +
        "`drink_name` TEXT NOT NULL, " +
        "`amount_ml` INTEGER NOT NULL, " +
        "`weight_ratio` REAL NOT NULL, " +
        "`created_at` INTEGER NOT NULL, " +
        "`created_at_local` INTEGER NOT NULL, " +
        "`synced_id` INTEGER)",
    "INSERT INTO `water_entries_new` " +
        "(`id`, `client_id`, `drink_name`, `amount_ml`, `weight_ratio`, `created_at`, `created_at_local`, `synced_id`) " +
        "SELECT `id`, " +
        "lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4' || " +
        "substr(lower(hex(randomblob(2))), 2) || '-' || " +
        "substr('89ab', 1 + (abs(random()) % 4), 1) || substr(lower(hex(randomblob(2))), 2) || '-' || " +
        "lower(hex(randomblob(6))), " +
        "`drink_name`, `amount_ml`, ${HydrationRatio.DEFAULT}, `created_at`, `created_at`, `synced_id` " +
        "FROM `water_entries`",
    "DROP TABLE `water_entries`",
    "ALTER TABLE `water_entries_new` RENAME TO `water_entries`",
    "CREATE UNIQUE INDEX IF NOT EXISTS `index_water_entries_client_id` ON `water_entries` (`client_id`)",
)

val MIGRATION_5_6: Migration = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        MIGRATION_5_6_STATEMENTS.forEach(db::execSQL)
    }

    override fun migrate(connection: SQLiteConnection) {
        MIGRATION_5_6_STATEMENTS.forEach { connection.execSQL(it) }
    }
}
