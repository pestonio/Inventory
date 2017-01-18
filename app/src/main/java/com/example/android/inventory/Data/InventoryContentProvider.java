package com.example.android.inventory.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.example.android.inventory.Data.InventoryDbHelper.LOG_TAG;

/**
 * Created by pestonio on 16/10/2016.
 */

public class InventoryContentProvider extends ContentProvider {


    private static final int INVENTORY = 100;
    private static final int INVENTORY_ID = 101;

    private InventoryDbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    //Initialise the provider and database helper object
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //Cursor variable to hold result
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot Query Unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues contentValues) {
        String name = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name.");
        }
        String desc = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_DESC);
        if (desc == null) {
            throw new IllegalArgumentException("Item requires a description.");
        }
        Integer stock = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK);
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Item requires an opening stock holding");
        }
        Float price = contentValues.getAsFloat(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Item requires a valid price");
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, contentValues);

        if (id == 0) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateInventory(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (contentValues.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME)) {
            String name = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }
        if (contentValues.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_DESC)) {
            String desc = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_DESC);
            if (desc == null) {
                throw new IllegalArgumentException("Item requires a description");
            }
        }
        if (contentValues.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK)) {
            Integer stock = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK);
            if (stock == null || stock < 0) {
                throw new IllegalArgumentException("Item requires a valid stock level");
            }
        }
        if (contentValues.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE)) {
            Float price = contentValues.getAsFloat(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Item requires a valid price");
            }
        }
        if (contentValues.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
