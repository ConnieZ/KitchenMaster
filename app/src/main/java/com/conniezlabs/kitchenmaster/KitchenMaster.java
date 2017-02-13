package com.conniezlabs.kitchenmaster;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;
import android.app.SearchManager;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import java.util.ArrayList;
public class KitchenMaster extends AppCompatActivity {

    //for logging and debugging
    private static final String TAG = "KitchenMaster-Main";

    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int SHOP_LIST_ID = Menu.NONE;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private ItemsDbAdapter mDbHelper;
    ListView listView;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "entered onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen_master);

        // trying to display the logo
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);

        mDbHelper = new ItemsDbAdapter(this);
        mDbHelper.open();
        Cursor itemsCursor = mDbHelper.fetchAllItems();

        fillData(itemsCursor);

        // The following is for Search Functionality
        // Handle the Search intent when received
        handleIntent(getIntent());
        Log.e(TAG, "success onCreate");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.e(TAG, "inside onNewIntent");

        handleIntent(intent);
    }


    /**
     * One of the methods for search functionality.
     * This will handle the Search intent
     * @param intent - intent received for this activity
     */
    private void handleIntent(Intent intent) {
        Log.e(TAG, "inside Searchable handleIntent");

        // If the intent is ACTION_SEARCH as expected
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Get the search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.e(TAG, "inside Searchable handleIntent - got string extra");

            //use the query to search the data somehow
            Cursor c = mDbHelper.fetchItem(query);
            Log.e(TAG, "inside Searchable handleIntent - fetched item");

            //sanity check - did we find any items?
//            if(c.getCount() > 0) {
//                Toast.makeText(getApplicationContext(), query + " found " + c.getCount() + " items",
//                        Toast.LENGTH_LONG).show();
//            }

            //process Cursor and display results
            fillData(c);
        }
    }

    private void fillData(Cursor itemsCursor) {
        Log.e(TAG, "entered fillData");


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

        EntryAdapter adapter = new EntryAdapter(KitchenMaster.this, items);
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Here you can get the position and access your item
                Log.e(TAG, "inside setOnItemClickListener");
                Entry e = (Entry) adapterView.getItemAtPosition(position);
                String rowid = e.getId();
                // The following is used for debugging;
//                Toast.makeText(getApplicationContext(),
//                        "Clicked on position " + position + ", id = " + id + ", obj - " + rowid,
//                        Toast.LENGTH_LONG).show();

                Intent i = new Intent(KitchenMaster.this, ItemEdit.class);
                i.putExtra(ItemsDbAdapter.KEY_ROWID, Long.parseLong(rowid));

                startActivityForResult(i, ACTIVITY_EDIT);

            }
        });

       Log.e(TAG, "finished fillData");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e(TAG, "entered onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        // this adds the ToolBar on top of the app with menu items from options_menu.xml
        inflater.inflate(R.menu.main_menu, menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        //new menu option to pull up the Shopping List
        menu.add(0, SHOP_LIST_ID, 0, R.string.shop_list);
        Log.e(TAG, "finished onCreateOptionsMenu");

        // Used to enable search in this activity
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getApplicationContext(), KitchenMaster.class)));
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e(TAG, "entered onOptionsItemSelected");
        if(item.getTitle().equals("Search")) {
//            Toast.makeText(getApplicationContext(), "Search = "+onSearchRequested(), Toast.LENGTH_LONG).show();
            return onSearchRequested();
        }
        switch(item.getItemId()) {
            case INSERT_ID:
                createItem();
                return true;
            case SHOP_LIST_ID:
                openShopList();
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
            	Log.e(TAG, "entered onContextItemSelected");
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteItem(info.id);
                fillData(mDbHelper.fetchAllItems());
                return true;
        }
        Log.e(TAG, "finished onContextItemSelected");
        return super.onContextItemSelected(item);
    }

    // call to activity that will create a new item in the database
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
        startActivityForResult(i, SHOP_LIST_ID);
        Log.e(TAG, "finished openShopList");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Log.e(TAG, "entered onActivityResult");
    	super.onActivityResult(requestCode, resultCode, intent);
        fillData(mDbHelper.fetchAllItems());
        Log.e(TAG, "finished onActivityResult");
    }
}

