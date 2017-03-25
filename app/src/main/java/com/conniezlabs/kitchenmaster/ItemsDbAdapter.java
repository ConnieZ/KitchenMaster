package com.conniezlabs.kitchenmaster;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;

/**
 * Simple items database access helper class. Defines the basic CRUD operations
 *, and gives the ability to list all items as well as
 * retrieve or modify a specific item.
 * Used this as source for singleton idea: http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
 *
 */
public class ItemsDbAdapter {
    private static final String TAG = "ItemsDbAdapter";

    public static final String KEY_NAME = "name";
    public static final String KEY_INVQTY = "invqty";
    public static final String KEY_BUYQTY = "buyqty";
    public static final String KEY_ROWID = "_id";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Item Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table inventory (_id integer primary key autoincrement, "
                    + "name text not null, invqty integer not null, buyqty integer not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "inventory";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;


    /**
     * Database Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public ItemsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the items database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ItemsDbAdapter open() throws SQLException {
        mDbHelper = DatabaseHelper.getInstance(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new item using the title and info provided. If the item is
     * successfully created return the new rowId for that item, otherwise return
     * a -1 to indicate failure.
     *
     * @param name the name of the item
     * @param invqty the inventory quantity of the item
     * @param buyqty the quantity of item to buy
     * @return rowId or -1 if failed
     */
    public long createItem(String name, int invqty, int buyqty) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_INVQTY, invqty);
        initialValues.put(KEY_BUYQTY, buyqty);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the item with the given rowId
     *
     * @param rowId id of item to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteItem(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all items in the database
     *
     * @return Cursor over all items
     */
    public Cursor fetchAllItems() {
        Log.e(TAG, "inside fetchAllItems");
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME,
                KEY_INVQTY, KEY_BUYQTY}, null, null, null, null, KEY_NAME + " COLLATE NOCASE ASC");
    }

    /**
     * Return a Cursor positioned at the item that matches the given rowId
     *
     * @param rowId id of item to retrieve
     * @return Cursor positioned to matching item, if found
     * @throws SQLException if item could not be found/retrieved
     */
    public Cursor fetchItem(long rowId) throws SQLException {
        Log.e(TAG, "inside fetchItem by id");
        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                                KEY_NAME, KEY_INVQTY, KEY_BUYQTY}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor.moveToFirst()) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }


    /**
     * Return a Cursor positioned at the item that matches the given name
     *
     * @param name name of item to retrieve
     * @return Cursor positioned to matching item, if found
     * @throws SQLException if item could not be found/retrieved
     */
    public Cursor fetchItem(String name) throws SQLException {
        Log.e(TAG, "inside fetchItem by name");
        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                                KEY_NAME, KEY_INVQTY, KEY_BUYQTY},
                        "lower(" + KEY_NAME + ") like lower('" + name + "%')", null,
                        null, null, null, null);
        if (mCursor.moveToFirst()) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Return items with Buy quantity filled out, which will be used to populate
     * the shopping list
     */
    public Cursor getShopListItems() {
        Cursor cursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                KEY_NAME, KEY_INVQTY, KEY_BUYQTY}, "buyqty is not null and buyqty > 0", null, null, null, KEY_NAME + " COLLATE NOCASE ASC", null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        Log.d(TAG, "getShopListItems executed");
        return cursor;
    }

    /**
     * Update the item using the details provided. The item to be updated is
     * specified using the rowId, and it is altered to use the name, invqty and buyqty
     * values passed in
     *
     * @param rowId id of item to update
     * @param name value to set item name to
     * @param invqty value to set inventory quantity to
     * @param buyqty value to set item buy quantity to
     * @return true if the item was successfully updated, false otherwise
     */
    public boolean updateItem(long rowId, String name, int invqty, int buyqty) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_INVQTY, invqty);
        args.put(KEY_BUYQTY, buyqty);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }


    /**
     * Helper class that is used to create the database object, which is then used by
     * other classes in the app.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;
        private static DatabaseHelper mInstance = null;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        public static DatabaseHelper getInstance(Context ctx) {

            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            // See this article for more information: http://bit.ly/6LRzfx
            if (mInstance == null) {
                mInstance = new DatabaseHelper(ctx.getApplicationContext());
            }
            return mInstance;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
            mDatabase = db;
            loadInitialItems();

        }


        private void loadInitialItems() {
            Log.e(TAG, "inside loadInitialItems");
//            new Thread(new Runnable() {
//                public void run() {
            try {
                loadItems();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//                }
//            }).start();
        }

        private void loadItems() throws IOException {
            Log.e(TAG, "inside loadItems");

            final Resources resources = mHelperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.initial_items);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.e(TAG, "inside loadItems, reading line: " + line);

                    String[] strings = TextUtils.split(line, ",");
                    if (strings.length < 2) continue;
                    long id = addItem(strings);
                    if (id < 0) {
                        Log.e(TAG, "unable to add word: " + strings[0].trim());
                    }
                }
            } finally {
                reader.close();
            }
        }

        public long addItem(String[] strings) {
            Log.e(TAG, "inside addItem");
            Log.e(TAG, "adding " + strings[0]);
            Log.e(TAG, "adding " + strings[1]);
            Log.e(TAG, "adding " + strings[2]);

            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_NAME, strings[0].trim());
            initialValues.put(KEY_INVQTY, strings[1].trim().length() > 0 ? Integer.parseInt(strings[1].trim()):0);
            initialValues.put(KEY_BUYQTY, strings[2].trim().length() > 0 ? Integer.parseInt(strings[1].trim()):0);

            return mDatabase.insert(DATABASE_TABLE, null, initialValues);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS inventory");
            onCreate(db);
        }
    }
}
