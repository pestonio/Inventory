package com.example.android.inventory;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventory.Data.InventoryContract;
import com.example.android.inventory.Data.InventoryDbHelper;

import static com.example.android.inventory.R.id.price;

/**
 * Created by pestonio on 16/10/2016.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    private InventoryDbHelper mDbHelper;

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        final TextView salesTextView = (TextView) view.findViewById(R.id.sales_to_date);
        final TextView qtyTextView = (TextView) view.findViewById(R.id.current_qty);
        TextView priceTextView = (TextView) view.findViewById(price);
        Button sellItemButton = (Button) view.findViewById(R.id.list_sell_button);

        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
        int salesColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_SALES);
        int qtyColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
        int iDColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);


        final String rowId = cursor.getString(iDColumnIndex);
        String itemName = cursor.getString(nameColumnIndex);
        final String sales = cursor.getString(salesColumnIndex);
        final Integer salesInt = Integer.parseInt(sales);
        final Integer quantityInt = cursor.getInt(qtyColumnIndex);    //Quantity Integer
        final String quantity = quantityInt.toString();
        Float priceFloat = cursor.getFloat(priceColumnIndex);
        String price = priceFloat.toString();

        nameTextView.setText(itemName);
        salesTextView.setText("Sales to Date: " + sales);
        qtyTextView.setText("Qty in Stock: " + quantity);
        priceTextView.setText("£ " + price);


        sellItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver resolver = v.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (quantityInt > 0) {
                    Integer itemId = Integer.parseInt(rowId);
                    Integer newstockValue = quantityInt - 1;
                    Integer newSalesValue = salesInt + 1;
                    values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK, newstockValue);
                    values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_SALES, newSalesValue);
                    Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, itemId);
                    resolver.update(currentItemUri, values, null, null);
                }
            }
        });
    }
}
