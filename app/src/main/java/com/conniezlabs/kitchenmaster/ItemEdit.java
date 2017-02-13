package com.conniezlabs.kitchenmaster;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ItemEdit extends Activity {
    //for logging and debugging
    private static final String TAG = "KitchenMaster-ItemEdit";

    private EditText mNameText;
    private EditText mInvQtyText;
    private EditText mBuyQtyText;
    private Long mRowId;
    private ItemsDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.e(TAG, "entered onCreate");
        super.onCreate(savedInstanceState);

        //create an instance of the ItemsDbAdapter
        mDbHelper = new ItemsDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.item_edit);
        setTitle(R.string.edit_item);

        mNameText = (EditText) findViewById(R.id.name);
        mInvQtyText = (EditText) findViewById(R.id.invqty);
        mBuyQtyText = (EditText) findViewById(R.id.buyqty);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        //check the savedInstanceState for the mRowId, in case the item editing contains a saved state in the Bundle
        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(ItemsDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            //Log.e(TAG, "inside onCreate: extras - " + extras.getLong(ItemsDbAdapter.KEY_ROWID) + ", mRowid - " +mRowId);
            mRowId = extras != null ? extras.getLong(ItemsDbAdapter.KEY_ROWID)
                                    : null;
        Log.e(TAG, "finished onCreate");
        }

        //to populate the fields based on the mRowId if we have it
        populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

        	public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }


        });


    }

    //call this method to read the item out of the database again and populate the fields
    private void populateFields() {
    	Log.e(TAG, "entered populateFields");
        if (mRowId != null) {
            Cursor item = mDbHelper.fetchItem(mRowId);
            startManagingCursor(item);
            mNameText.setText(item.getString(
                        item.getColumnIndexOrThrow(ItemsDbAdapter.KEY_NAME)));
            mInvQtyText.setText(item.getString(
                    item.getColumnIndexOrThrow(ItemsDbAdapter.KEY_INVQTY)));
            mBuyQtyText.setText(item.getString(
                    item.getColumnIndexOrThrow(ItemsDbAdapter.KEY_BUYQTY)));
        }
        Log.e(TAG, "finished populateFields");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	Log.e(TAG, "entered onSaveInstanceState");
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(ItemsDbAdapter.KEY_ROWID, mRowId);
        Log.e(TAG, "finished onSaveInstanceState");
    }

    @Override
    protected void onPause() {
    	Log.e(TAG, "entered onPause");
        super.onPause();
        saveState();
        Log.e(TAG, "finished onPause");
    }

    @Override
    protected void onResume() {
    	Log.e(TAG, "entered onResume");
        super.onResume();
        populateFields();
        Log.e(TAG, "finished onResume");
    }

    //Define the saveState() method to put the data out to the database.
    private void saveState() {
    	Log.e(TAG, "entered saveState");
        String name = mNameText.getText().toString();
        int invqty = mInvQtyText.getText().toString().length() > 0? Integer.parseInt(mInvQtyText.getText().toString()):0;
        int buyqty = mBuyQtyText.getText().toString().length() > 0? Integer.parseInt(mBuyQtyText.getText().toString()):0;

        if (mRowId == null & name != null & name.length() > 0) {
            if(mDbHelper.fetchItem(name.toLowerCase()).moveToFirst()){
                // Warn the user that the item already exists and won't be duplicated
                Toast.makeText(getApplicationContext(), name + " item already exists.", Toast.LENGTH_LONG).show();
            } else{
                long id = mDbHelper.createItem(name.toLowerCase(), invqty, buyqty);
                if (id > 0) {
                    mRowId = id;
                }
            }
        } else if (mRowId != null) {
            mDbHelper.updateItem(mRowId, name, invqty, buyqty);
        } else {
            // do nothing
        }
        Log.e(TAG, "finished saveState");
    }
}
