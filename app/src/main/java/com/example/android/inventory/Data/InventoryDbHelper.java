package com.example.android.inventory.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by pestonio on 16/10/2016.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    //Database name
    public static final String DATABASE_NAME = "inventory.db";

    //Database version
    public static final int DATABASE_VERSION = 1;

    //Constructor
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE =
                "CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + " ("
                        + InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + InventoryContract.InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                        + InventoryContract.InventoryEntry.COLUMN_ITEM_DESC + " TEXT NOT NULL, "
                        + InventoryContract.InventoryEntry.COLUMN_ITEM_SALES + " INTEGER, "
                        + InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK + " INTEGER NOT NULL DEFAULT 0, "
                        + InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE + " FLOAT NOT NULL DEFAULT 0, "
                        + InventoryContract.InventoryEntry.COLUMN_ITEM_IMG + " TEXT);";
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //Not needed
    }
}
