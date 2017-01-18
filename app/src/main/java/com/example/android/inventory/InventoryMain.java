package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventory.Data.InventoryContract;

public class InventoryMain extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_main);

        getIntent();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryMain.this, AddOrEditProduct.class);
                startActivity(intent);
            }
        });

        ListView inventoryListView = (ListView) findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);

        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(InventoryMain.this, DetailView.class);
                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                intent.setData(currentItemUri);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }


    private void insertDummyItem() {
        Uri path = Uri.parse("android.resource://com.example.android.inventory/" + R.drawable.ic_lego_ghostbusters_ecto_1);
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME, "LEGO ECTO-1");
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_DESC, "LEGO Ghostbusters Vehicle");
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_SALES, 100);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK, 50);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE, 45.99);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_IMG, path.toString());

        Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
    }

    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
        Log.v("InventoryMain", rowsDeleted + " rows deleted from Inventory database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_inventory_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.insert_dummy_data:
                insertDummyItem();
                return true;
            case R.id.delete_all:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_ITEM_NAME,
                InventoryContract.InventoryEntry.COLUMN_ITEM_DESC,
                InventoryContract.InventoryEntry.COLUMN_ITEM_SALES,
                InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK,
                InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE};
        return new CursorLoader(this,
                InventoryContract.InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
