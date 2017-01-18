package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.Data.InventoryContract;

/**
 * Created by pestonio on 16/10/2016.
 */

public class AddOrEditProduct extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_INV_LOADER = 0;
    private Uri mCurrentItemUri;
    private EditText mNameEditText;
    private EditText mDescEditText;
    private EditText mQtyEditText;
    private EditText mPriceEditText;
    private ImageView mItemImage;
    private boolean mItemHasChanged = false;
    static final int REQUEST_IMAGE_GET = 1;
    private String photoString;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle("Add a product");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit a product");
            getLoaderManager().initLoader(EXISTING_INV_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.product_name_add);
        mDescEditText = (EditText) findViewById(R.id.product_desc_add);
        mQtyEditText = (EditText) findViewById(R.id.initial_stock_add);
        mPriceEditText = (EditText) findViewById(R.id.product_price_add);
        mItemImage = (ImageView) findViewById(R.id.edit_item_image);

        mNameEditText.setOnTouchListener(mTouchListener);
        mDescEditText.setOnTouchListener(mTouchListener);
        mQtyEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
    }

    private void saveItem() {
        String nameString = mNameEditText.getText().toString().trim();
        String descString = mDescEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String stockString = mQtyEditText.getText().toString().trim();

        int startingSales = 0;

        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_DESC, descString);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK, stockString);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE, priceString);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_IMG, photoString);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_SALES, startingSales);

        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "Error Saving Item", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item Saved", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, "Error updating item", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_edit_product, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (mQtyEditText.getText().toString().isEmpty() || mNameEditText.getText().toString().isEmpty() || mPriceEditText.getText().toString().isEmpty() || mDescEditText.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Please complete all details", Toast.LENGTH_SHORT).show();
                } else {
                    saveItem();
                    finish();
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(AddOrEditProduct.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(AddOrEditProduct.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            case R.id.add_photo:
                getItemImage();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getItemImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();
            photoString = fullPhotoUri.toString();
            mItemImage.setImageURI(fullPhotoUri);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
            int descColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_DESC);
            int stockColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_STOCK);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
            int imgColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_IMG);

            String name = cursor.getString(nameColumnIndex);
            String desc = cursor.getString(descColumnIndex);
            String stock = cursor.getString(stockColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String img = cursor.getString(imgColumnIndex);
            if (img != null) {
                Uri imgUri = Uri.parse(img);
                mItemImage.setImageURI(imgUri);
            }

            mNameEditText.setText(name);
            mDescEditText.setText(desc);
            mQtyEditText.setText(stock);
            mPriceEditText.setText(price);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mDescEditText.setText("");
        mPriceEditText.setText("");
        mQtyEditText.setText("");
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard changes and exit?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
                NavUtils.navigateUpFromSameTask(AddOrEditProduct.this);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, "Error deleting item", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
