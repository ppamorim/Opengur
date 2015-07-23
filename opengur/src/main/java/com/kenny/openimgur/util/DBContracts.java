package com.kenny.openimgur.util;

import android.provider.BaseColumns;

/**
 * Created by kcampagna on 7/25/14.
 */
public class DBContracts {

    public static class UserContract implements BaseColumns {
        public static final String TABLE_NAME = "user";

        public static final String COLUMN_ACCESS_TOKEN = "access_token";

        public static final String COLUMN_REFRESH_TOKEN = "refresh_token";

        public static final String COLUMN_ACCESS_TOKEN_EXPIRATION = "at_expiration";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_CREATED = "created";

        public static final String COLUMN_PRO_EXPIRATION = "pro_expiration";

        public static final String COLUMN_REPUTATION = "rep";

        public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (" + _ID + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL," +
                COLUMN_ACCESS_TOKEN + " TEXT NOT NULL," +
                COLUMN_REFRESH_TOKEN + " TEXT NOT NULL," +
                COLUMN_CREATED + " INTEGER," +
                COLUMN_ACCESS_TOKEN_EXPIRATION + " INTEGER," +
                COLUMN_PRO_EXPIRATION + " INTEGER," +
                COLUMN_REPUTATION + " INTEGER);";

        public static int COLUMN_INDEX_ID = 0;

        public static int COLUMN_INDEX_NAME = 1;

        public static int COLUMN_INDEX_ACCESS_TOKEN = 2;

        public static int COLUMN_INDEX_REFRESH_TOKEN = 3;

        public static int COLUMN_INDEX_CREATED = 4;

        public static int COLUMN_INDEX_ACCESS_TOKEN_EXPIRATION = 5;

        public static int COLUMN_INDEX_PRO_EXPIRATION = 6;

        public static int COLUMN_INDEX_REPUTATION = 7;
    }

    public static class ProfileContract implements BaseColumns {
        public static final String TABLE_NAME = "profiles";

        public static final String COLUMN_USERNAME = "username";

        public static final String COLUMN_REP = "rep";

        public static final String COLUMN_BIO = "bio";

        public static final String COLUMN_LAST_SEEN = "last_seen";

        public static final String COLUMN_CREATED = "created";

        public static final int COLUMN_INDEX_ID = 0;

        public static final int COLUMN_INDEX_USERNAME = 1;

        public static final int COLUMN_INDEX_REP = 2;

        public static final int COLUMN_INDEX_BIO = 3;

        public static final int COLUMN_INDEX_LAST_SEEN = 4;

        public static final int COLUMN_INDEX_CREATED = 5;

        public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (" + _ID + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT NOT NULL, " +
                COLUMN_REP + " INTEGER, " +
                COLUMN_BIO + " TEXT, " +
                COLUMN_LAST_SEEN + " INTEGER, " +
                COLUMN_CREATED + " INTEGER);";

        public static final String SEARCH_USER_SQL = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COLUMN_USERNAME + " LIKE '%s' LIMIT 0,1";
    }

    public static class UploadContract implements BaseColumns {
        public static final String TABLE_NAME = "uploads";

        public static final String COLUMN_URL = "url";

        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_DELETE_HASH = "delete_hash";

        public static final String COLUMN_IS_ALBUM = "is_album";

        public static final String COLUMN_COVER_ID = "cover_id";

        public static final int COLUMN_INDEX_ID = 0;

        public static final int COLUMN_INDEX_URL = 1;

        public static final int COLUMN_INDEX_DATE = 2;

        public static final int COLUMN_INDEX_DELETE_HASH = 3;

        public static final int COLUMN_INDEX_IS_ALBUM = 4;

        public static final int COLUMN_INDEX_COVER_ID = 5;

        public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (" + _ID + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                COLUMN_URL + " TEXT NOT NULL, " +
                COLUMN_DATE + " INTEGER, " +
                COLUMN_DELETE_HASH + " TEXT, " +
                COLUMN_IS_ALBUM + " INTEGER, " +
                COLUMN_COVER_ID + " TEXT);";

        public static final String GET_UPLOADS_SQL = "SELECT * FROM " + TABLE_NAME +
                " ORDER BY " + COLUMN_DATE + " %s";

        public static final String DELETE_PHOTO_SQL = " DELETE FROM " + TABLE_NAME +
                " WHERE " + _ID + "=%d";
    }

    public static class TopicsContract implements BaseColumns {
        public static final String TABLE_NAME = "topics";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_DESC = "description";

        public static final int COLUMN_INDEX_ID = 0;

        public static final int COLUMN_INDEX_NAME = 1;

        public static final int COLUMN_INDEX_DESC = 2;

        public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (" + _ID + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_DESC + " TEXT NOT NULL);";

        public static final String GET_TOPICS_SQL = "SELECT * FROM " + TABLE_NAME
                + " ORDER BY " + COLUMN_NAME + " COLLATE NOCASE ASC";

        public static final String GET_TOPIC_SQL = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                _ID + " = %d LIMIT 0,1";

        public static final String DELETE_TOPIC_SQL = "DELETE FROM " + TABLE_NAME + " WHERE " +
                _ID + " =%d";
    }

    public static class SubRedditContract implements BaseColumns {
        public static final String TABLE_NAME = "subreddits";

        public static final String COLUMN_NAME = "name";

        public static final int COLUMN_INDEX_ID = 0;

        public static final int COLUMN_INDEX_NAME = 1;

        public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (" + _ID + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL UNIQUE)";

        public static final String GET_SUBREDDITS_SQL = "SELECT * FROM " + TABLE_NAME;

        public static final String SEARCH_SUBREDDIT_SQL = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                COLUMN_NAME + " LIKE ?";
    }

    public static class MemeContract implements BaseColumns {
        public static final String TABLE_NAME = "meme";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_LINK = "link";

        public static final int COLUMN_INDEX_ID = 0;

        public static final int COLUMN_INDEX_TITLE = 1;

        public static final int COLUMN_INDEX_LINK = 2;

        public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (" + _ID + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_LINK + " TEXT)";

        public static final String GET_MEMES_SQL = "SELECT * FROM " + TABLE_NAME;
    }

    public static class GallerySearchContract implements BaseColumns {
        public static final String TABLE_NAME = "gallery_search";

        public static final String COLUMN_NAME = "name";

        public static final int COLUMN_INDEX_ID = 0;

        public static final int COLUMN_INDEX_NAME = 1;

        public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (" + _ID + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL UNIQUE)";

        public static final String GET_PREVIOUS_SEARCHES_SQL = "SELECT * FROM " + TABLE_NAME;

        public static final String SEARCH_GALLERY_SQL = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                COLUMN_NAME + " LIKE ?";
    }

    public static class MuzeiContract implements BaseColumns {
        public static final String TABLE_NAME = "muzei";

        public static final String COLUMN_LINK = "link";

        public static final String COLUMN_LAST_SEEN = "last_seen";

        public static final int COLUMN_INDEX_ID = 0;

        public static final int COLUMN_INDEX_LINK = 1;

        public static final int COLUMN_INDEX_LAST_SEEN = 2;

        public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (" + _ID + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                COLUMN_LINK + " TEXT NOT NULL UNIQUE, " +
                COLUMN_LAST_SEEN + " INTEGER)";

        public static final String GET_LAST_SEEN_SQL = "SELECT " + COLUMN_LAST_SEEN + " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_LINK + " ='%s' LIMIT 0,1";
    }
}
