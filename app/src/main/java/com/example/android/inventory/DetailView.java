package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.Data.InventoryContract;

/**
 * Created by pestonio on 22/10/2016.
 */

public class DetailView extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_INV_LOADER = 0;
    private Uri mCurrentItemUri;
    private TextView mNameText;
    private TextView mDescText;
    private TextView mSalesText;
    private TextView mQtyText;
    private TextView mPriceText;
    private ImageView mItemImage;
    private Button mSellButton;
    private Button mReceiveButton;
    private EditText mQtyEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        setTitle("Product Details");
        getLoaderManager().initLoader(EXISTING_INV_LOADER, null, this);

        mNameText = (TextView) findViewById(R.id.product_name_detail);
        mDescText = (TextView) findViewById(R.id.product_desc_detail);
        mSalesText = (TextView) findViewById(R.id.sales_to_date_detail);
        mQtyText = (TextView) findViewById(R.id.current_qty_detail);
        mPriceText = (TextView) findViewById(R.id.price_detail);
        mItemImage = (ImageView) findViewById(R.id.product_img);
        mSellButton = (Button) findViewById(R.id.sell_button);
        mReceiveButton = (Button) findViewById(R.id.receive_button);
        mQtyEditText = (EditText) findViewById(R.id.adjust_qty_by);

        mReceiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReceive();
            }
        });
        mSellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSell();
            }
        });
    }
    public void onSell(){
        String existingStock = mQtyText.getText().toString();
        String stockToSell = mQtyEditText.getText().toString();
        if (stockToSell.isEmpty()) {
            Toast.makeText(DetailView.this, "Enter a Quantity", Toast.LENGTH_SHORT).show();
            return;
        } else {
            String salesToDateText = mSalesText.getText().toString();
            Integer existingStockInt = Integer.parseInt(existingStock);
            Integer stockToSellInt = Integer.parseInt(stockToSell);
            Integer salesToDateInt = Integer.parseInt(salesToDateText);
            Integer newQty = existingStockInt - stockToSellInt;
            if (newQty >= 0) {
                ContentValues values = new ContentValues();
                values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK, newQty);
                values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_SALES, stockToSellInt + salesToDateInt);
                getContentResolver().update(mCurrentItemUri, values, null, null);
                mQtyEditText.setText("");
            } else {
                Toast.makeText(DetailView.this, "You cannot sell what you don't have!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void onReceive() {
        String existingStock = mQtyText.getText().toString();
        String newStockToAdd = mQtyEditText.getText().toString();
        if (newStockToAdd.isEmpty()) {
            Toast.makeText(DetailView.this, "Enter a Quantity", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Integer existingStockInt = Integer.parseInt(existingStock);
            Integer newStockToAddInt = Integer.parseInt(newStockToAdd);
            Integer newQty = existingStockInt + newStockToAddInt;

            ContentValues values = new ContentValues();
            values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK, newQty);
            getContentResolver().update(mCurrentItemUri, values, null, null);
            mQtyEditText.setText("");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_item:
                Intent intent = new Intent(DetailView.this, AddOrEditProduct.class);
                intent.setData(mCurrentItemUri);
                startActivity(intent);
                return true;
            case R.id.resupply_item:
                itemResupply(new String[]{"itemresupply@nowhere.com"}, "Item Resupply: ");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void itemResupply(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject + mNameText.getText().toString());
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_ITEM_NAME,
                InventoryContract.InventoryEntry.COLUMN_ITEM_DESC,
                InventoryContract.InventoryEntry.COLUMN_ITEM_SALES,
                InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK,
                InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryContract.InventoryEntry.COLUMN_ITEM_IMG};
        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
            int descColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_DESC);
            int stockColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK);
            int salesColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_SALES);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
            int imgColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_IMG);

            String name = cursor.getString(nameColumnIndex);
            String desc = cursor.getString(descColumnIndex);
            String stock = cursor.getString(stockColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String sales = cursor.getString(salesColumnIndex);
            String img = cursor.getString(imgColumnIndex);
            if (img != null) {
                Uri imgUri = Uri.parse(img);
                mItemImage.setImageURI(imgUri);
            }

            mNameText.setText(name);
            mDescText.setText(desc);
            mQtyText.setText(stock);
            mPriceText.setText(price);
            mSalesText.setText(sales);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameText.setText("");
        mDescText.setText("");
        mQtyText.setText("");
        mPriceText.setText("");
        mSalesText.setText("");
    }
}
