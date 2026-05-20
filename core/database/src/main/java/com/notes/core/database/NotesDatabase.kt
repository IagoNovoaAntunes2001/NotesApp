package com.notes.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.notes.core.database.dao.TopicDao
import com.notes.core.database.entity.TopicEntity

@Database(
    entities = [TopicEntity::class],
    version = 3,
    exportSchema = false
)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao

    companion object {
        /**
         * Migration 1 → 2: adiciona coluna `updatedAt` (Long, default 0).
         * Default 0L: registros antigos perdem para qualquer server timestamp no LWW.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE topics ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * Migration 2 → 3: adiciona coluna `syncStatus` (String, default 'SYNCED').
         * Default 'SYNCED': registros existentes são considerados já sincronizados,
         * pois vieram do servidor em syncs anteriores.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE topics ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'SYNCED'")
            }
        }
    }
}
