package com.example.android.inventory.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by pestonio on 16/10/2016.
 */

public class InventoryContract {

    //Private constructor
    private InventoryContract() {
    }

    //Content Authority
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";

    public static final class InventoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        //MIME type for complete table
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //MIME type for individual item
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //Table column titles
        public static final String TABLE_NAME = "Inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_NAME = "Name";
        public static final String COLUMN_ITEM_DESC = "Description";
        public static final String COLUMN_ITEM_SALES = "Sales";
        public static final String COLUMN_ITEM_STOCK = "Stock";
        public static final String COLUMN_ITEM_PRICE = "Price";
        public static final String COLUMN_ITEM_IMG = "Image";
    }
}
