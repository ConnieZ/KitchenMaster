package com.conniezlabs.kitchenmaster;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class KitchenMaster extends ListActivity {
	
	//for logging and debugging
	private static final String TAG = "KitchenMaster-Main";
	
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int SHOP_LIST_ID = Menu.NONE;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private ItemsDbAdapter mDbHelper;
   

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.e(TAG, "entered onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen_master);
        mDbHelper = new ItemsDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
        Log.e(TAG, "success onCreate");
    }

    private void fillData() {
    	Log.e(TAG, "entered fillData");
    	// Get all of the rows from the database and create the item list
        Cursor itemsCursor = mDbHelper.fetchAllItems();
        startManagingCursor(itemsCursor);

        // Create an array to specify the fields we want to display in the list 
        String[] from = new String[]{ItemsDbAdapter.KEY_NAME, ItemsDbAdapter.KEY_INVQTY, ItemsDbAdapter.KEY_BUYQTY};

        // and an array of the text fields we want to bind those db fields to 
        int[] to = new int[]{R.id.text_name, R.id.text_invqty, R.id.text_buyqty};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter items = 
            new SimpleCursorAdapter(this, R.layout.item_row, itemsCursor, from, to);
        setListAdapter(items);
        
        Log.e(TAG, "finished fillData");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.e(TAG, "entered onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        //new menu option to pull up the Shopping List
        menu.add(0, SHOP_LIST_ID, 0, R.string.shop_list);
        Log.e(TAG, "finished onCreateOptionsMenu");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	Log.e(TAG, "entered onMenuItemSelected");
        switch(item.getItemId()) {
            case INSERT_ID:
                createItem();
                return true;
            case SHOP_LIST_ID:
                openShopList();
                return true;
        }
        Log.e(TAG, "finished onMenuItemSelected");
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
    	Log.e(TAG, "entered onCreateContextMenu");
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
        Log.e(TAG, "finished onCreateContextMenu");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
            	Log.e(TAG, "entered onContextItemSelected");
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteItem(info.id);
                fillData();
                return true;
        }
        Log.e(TAG, "finished onContextItemSelected");
        return super.onContextItemSelected(item);
    }

    private void createItem() {
    	Log.e(TAG, "entered createItem");
        Intent i = new Intent(this, ItemEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
        Log.e(TAG, "finished createItem");
    }
    
    //call to activity that will open shopping list
    private void openShopList() {
    	Log.e(TAG, "entered openShopList");
        Intent i = new Intent(this, ShoppingList.class);
        startActivity(i);
        Log.e(TAG, "finished openShopList");
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	Log.e(TAG, "entered onListItemClick");
    	super.onListItemClick(l, v, position, id);
        
        Intent i = new Intent(this, ItemEdit.class);
        i.putExtra(ItemsDbAdapter.KEY_ROWID, id);
        
        startActivityForResult(i, ACTIVITY_EDIT);
        Log.e(TAG, "finished onListItemClick");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Log.e(TAG, "entered onActivityResult");
    	super.onActivityResult(requestCode, resultCode, intent);
        fillData();
        Log.e(TAG, "finished onActivityResult");
    }
}

