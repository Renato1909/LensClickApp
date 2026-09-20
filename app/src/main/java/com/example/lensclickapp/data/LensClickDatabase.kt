package com.example.lensclickapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Photographer::class, Budget::class, User::class],
    version = 3,
    exportSchema = true
)
abstract class LensClickDatabase : RoomDatabase() {
    abstract fun lensClickDao(): LensClickDao

    companion object {
        @Volatile private var instance: LensClickDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'client'")
                db.execSQL("ALTER TABLE photographers ADD COLUMN userId INTEGER")
                db.execSQL("ALTER TABLE photographers ADD COLUMN city TEXT NOT NULL DEFAULT 'São Paulo, SP'")
                db.execSQL("ALTER TABLE photographers ADD COLUMN bio TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_photographers_userId ON photographers(userId)")
            }
        }

        // Keep local prototype profiles, but never keep password material in Room.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE users_without_credentials (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL,
                        role TEXT NOT NULL DEFAULT 'client',
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO users_without_credentials (id, name, email, role, createdAt)
                    SELECT id, name, email, role, createdAt FROM users
                """.trimIndent())
                db.execSQL("DROP TABLE users")
                db.execSQL("ALTER TABLE users_without_credentials RENAME TO users")
                db.execSQL("CREATE UNIQUE INDEX index_users_email ON users(email)")
            }
        }

        fun getInstance(context: Context): LensClickDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LensClickDatabase::class.java,
                    "lens_click.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
    }
}
