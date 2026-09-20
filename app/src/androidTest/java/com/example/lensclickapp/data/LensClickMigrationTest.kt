package com.example.lensclickapp.data

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Guarda de instrumentação da regra "nenhuma senha real no Room": a migração 2→3
 * recria a tabela de usuários sem coluna de credencial e preserva os perfis.
 *
 * Usa FrameworkSQLiteOpenHelper diretamente (sem MigrationTestHelper), porque o
 * room-testing 2.8.5 tem incompatibilidade de kotlinx.serialization no runtime do
 * emulador (AbstractMethodError ao desserializar o bundle de schema). Aqui criamos
 * o banco v2 real e rodamos a migração Room verdadeira, validando o resultado.
 */
@RunWith(AndroidJUnit4::class)
class LensClickMigrationTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val testDb = "lensclick-migration-test"

    @After
    fun tearDown() {
        context.deleteDatabase(testDb)
    }

    @Test
    fun migration2To3DropsPasswordColumnAndKeepsUsers() {
        // Recria o schema v2 histórico: users COM passwordHash (a credencial que
        // esta entrega remove do Room).
        openDatabase(2).writableDatabase.apply {
            execSQL(
                """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    passwordHash TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'client',
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO users (id, name, email, passwordHash, role, createdAt)
                VALUES (1, 'Usuário de demonstração', 'seu@email.com', 'hash-legado', 'client', 1000)
                """.trimIndent()
            )
            execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_email ON users(email)")
            close()
        }

        // Abre como v3: o SupportSQLiteOpenHelper aplica MIGRATION_2_3 de verdade.
        val db = openDatabase(3).writableDatabase

        db.query("PRAGMA table_info(users)").use { cursor ->
            val columns = mutableSetOf<String>()
            while (cursor.moveToNext()) columns += cursor.getString(cursor.getColumnIndexOrThrow("name"))
            assertFalse("A coluna de senha não pode sobreviver à migração", "passwordHash" in columns)
            assertTrue("email" in columns)
            assertTrue("role" in columns)
        }

        db.query("SELECT id, name, email, role, createdAt FROM users").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1L, cursor.getLong(0))
            assertEquals("Usuário de demonstração", cursor.getString(1))
            assertEquals("seu@email.com", cursor.getString(2))
            assertEquals("client", cursor.getString(3))
            assertEquals(1000L, cursor.getLong(4))
        }

        db.query("SELECT COUNT(*) FROM sqlite_master WHERE type='index' AND name='index_users_email'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }

        db.close()
    }

    private fun openDatabase(version: Int): SupportSQLiteOpenHelper {
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(testDb)
            .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                override fun onCreate(db: SupportSQLiteDatabase) = Unit
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    if (oldVersion < 2) LensClickDatabase.MIGRATION_1_2.migrate(db)
                    if (oldVersion < 3) LensClickDatabase.MIGRATION_2_3.migrate(db)
                }
            })
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(configuration)
    }
}
