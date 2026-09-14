package com.example.lensclickapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Photographer::class, Budget::class, User::class],
    version = 2,
    exportSchema = false
)
abstract class LensClickDatabase : RoomDatabase() {
    abstract fun lensClickDao(): LensClickDao

    companion object {
        @Volatile private var instance: LensClickDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'client'")
                db.execSQL("ALTER TABLE photographers ADD COLUMN userId INTEGER")
                db.execSQL("ALTER TABLE photographers ADD COLUMN city TEXT NOT NULL DEFAULT 'São Paulo, SP'")
                db.execSQL("ALTER TABLE photographers ADD COLUMN bio TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_photographers_userId ON photographers(userId)")
            }
        }

        fun getInstance(context: Context): LensClickDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LensClickDatabase::class.java,
                    "lens_click.db"
                ).addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
