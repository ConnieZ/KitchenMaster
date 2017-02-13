package com.conniezlabs.kitchenmaster;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

import java.util.ArrayList;

public class ShoppingList extends AppCompatActivity {
	
	//for logging and debugging
	private static final String TAG = "KitchenMaster-ShopList";
	
    private static final int ACTIVITY_CREATE=0;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private ItemsDbAdapter mDbHelper;
    private Long mRowId;
    ListView listView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.e(TAG, "entered onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);
        mDbHelper = new ItemsDbAdapter(this);
        mDbHelper.open();
        setTitle(R.string.shop_list);
        Cursor itemsCursor = mDbHelper.getShopListItems();
        fillListRows(itemsCursor);
        //registerForContextMenu(getListView());

        //BOUGHT button
        Button boughtButton = (Button) findViewById(R.id.bought_button);

        //listener for BOUGHT button
        boughtButton.setOnClickListener(new View.OnClickListener () {
        	public void onClick(View view) {
        		//call to update the rows of data in database after shopping is done
        		updateInventory();
        		//finish the activity
                setResult(RESULT_OK);
                finish();
        	}
        });

        Log.e(TAG, "success onCreate");
    }

    private void fillListRows(Cursor itemsCursor) {
    	Log.e(TAG, "entered fillListRows");
    	// Get only the rows of data that have a BUY quantity
        ArrayList<Entry> items = new ArrayList<Entry>();

        if(itemsCursor.moveToFirst()){
            do{
                items.add(new Entry(itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(ItemsDbAdapter.KEY_ROWID)),
                        itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(ItemsDbAdapter.KEY_NAME)),
                        itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(ItemsDbAdapter.KEY_INVQTY)),
                        itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(ItemsDbAdapter.KEY_BUYQTY))));
                // The following is used for debugging;
//                Log.e(TAG, "added " + itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(ItemsDbAdapter.KEY_NAME))
//                        + " item with inventory " + itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(ItemsDbAdapter.KEY_INVQTY))
//                        + " and to buy " + itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(ItemsDbAdapter.KEY_BUYQTY)));

            }while(itemsCursor.moveToNext());
        }
        itemsCursor.close();

        // Bind to our new adapter.

        EntryAdapter adapter = new EntryAdapter(ShoppingList.this, items);
        listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(adapter);

        Log.e(TAG, "finished fillListRows");
    }

    //method to update the inventory with items that were bought
    private void updateInventory() {
    	// Get only the rows of data that have a BUY quantity
        Cursor tempItemsCursor = mDbHelper.getShopListItems();

        if (tempItemsCursor.moveToFirst()) {
        	//iterate through the cursor and update each item in the database
        	do {
        		mRowId = Long.parseLong(tempItemsCursor.getString(0));
        		Integer intInvQty = 0;
        		if (mRowId != null) {
        			//create a cursor for the item in question
        			Cursor item = mDbHelper.fetchItem(mRowId);
        			Log.e(TAG, "inside first if statement for ID " + mRowId);
        			startManagingCursor(item);
        			//add the BUY quantity to INV quantity and zero out BUY quantity
        			int currentInvQty = item.getInt(item.getColumnIndexOrThrow(ItemsDbAdapter.KEY_INVQTY));
        			Log.e(TAG, "declared currentInvQty");
        			Log.e(TAG, "currentInvQty " + currentInvQty);
        			intInvQty = currentInvQty + item.getInt(item.getColumnIndexOrThrow(ItemsDbAdapter.KEY_BUYQTY));

        			int emptyBuyQty = 0;
        			Log.e(TAG, "zeroed out buyQty");
        			mDbHelper.updateItem(mRowId, item.getString(item.getColumnIndexOrThrow(ItemsDbAdapter.KEY_NAME)), intInvQty, emptyBuyQty);
        		}

        	} while (tempItemsCursor.moveToNext());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.e(TAG, "entered onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        Log.e(TAG, "finished onCreateOptionsMenu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.e(TAG, "entered onMenuItemSelected");
        switch(item.getItemId()) {
            case INSERT_ID:
                createItem();
                return true;

        }
        Log.e(TAG, "finished onMenuItemSelected");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
    	Log.e(TAG, "entered onCreateContextMenu");
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
        Log.e(TAG, "finished onCreateContextMenu");
    }

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        switch(item.getItemId()) {
//            case DELETE_ID:
//            	Log.e(TAG, "entered onContextItemSelected");
//                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
//                mDbHelper.deleteItem(info.id);
//                fillListRows();
//                return true;
//        }
//        Log.e(TAG, "finished onContextItemSelected");
//        return super.onContextItemSelected(item);
//    }

    private void createItem() {
    	Log.e(TAG, "entered createItem");
        Intent i = new Intent(this, ItemEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
        Log.e(TAG, "finished createItem");
    }


//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//    	Log.e(TAG, "entered onListItemClick");
//    	super.onListItemClick(l, v, position, id);
//
//        Intent i = new Intent(this, ItemEdit.class);
//        i.putExtra(ItemsDbAdapter.KEY_ROWID, id);
//
//        startActivityForResult(i, ACTIVITY_EDIT);
//        Log.e(TAG, "finished onListItemClick");
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Log.e(TAG, "entered onActivityResult");
    	super.onActivityResult(requestCode, resultCode, intent);
    	fillListRows(mDbHelper.getShopListItems());
        Log.e(TAG, "finished onActivityResult");
    }

}
