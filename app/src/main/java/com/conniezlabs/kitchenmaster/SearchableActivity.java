package com.conniezlabs.kitchenmaster;
// this file used the following sources:
// http://www.androiddesignpatterns.com/2012/07/understanding-loadermanager.html
// https://developer.android.com/training/search/index.html
// https://inducesmile.com/android/android-search-dialog-implementation-example/
// Most helpful tool to fix non-clickable search widget:
// http://stackoverflow.com/questions/17311434/search-dialog-is-not-called-onsearchrequested

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SearchableActivity extends ListActivity {

    private static final String TAG = "SearchableActivity";
    ListView listView;
    private ItemsDbAdapter mDbHelper = new ItemsDbAdapter(this);
    private static final int ACTIVITY_EDIT=1;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "Started Searchable onCreate");
        mDbHelper.open();
        // The following is for Search Functionality
        handleIntent(getIntent());

        setContentView(R.layout.search_layout);

        }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.e(TAG, "inside onNewIntent");

        handleIntent(intent);
    }



    // One of the methods for search functionality, this will handle the Search intent
    private void handleIntent(Intent intent) {
        Log.e(TAG, "inside Searchable handleIntent");

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.e(TAG, "inside Searchable handleIntent - got string extra");

            //use the query to search the data somehow
            Cursor c = mDbHelper.fetchItem(query);
            Log.e(TAG, "inside Searchable handleIntent - fetched item");

            //sanity check - did we find any items?
            if(c.getCount() > 0) {
                Toast.makeText(getApplicationContext(), query + " found " + c.getCount() + " items",
                        Toast.LENGTH_LONG).show();
            }

            //process Cursor and display results
            fillData(c);
        }
    }



    private void fillData(Cursor c) {
    	Log.e(TAG, "entered fillData");

        ArrayList<Entry> items = new ArrayList<Entry>();
        if(c.moveToFirst()){
            do{
                items.add(new Entry(c.getString(c.getColumnIndexOrThrow(ItemsDbAdapter.KEY_ROWID)),
                        c.getString(c.getColumnIndexOrThrow(ItemsDbAdapter.KEY_NAME)),
                        c.getString(c.getColumnIndexOrThrow(ItemsDbAdapter.KEY_INVQTY)),
                        c.getString(c.getColumnIndexOrThrow(ItemsDbAdapter.KEY_BUYQTY))));
                Log.e(TAG, "added " + c.getString(c.getColumnIndexOrThrow(ItemsDbAdapter.KEY_NAME))
                        + " item with inventory " + c.getString(c.getColumnIndexOrThrow(ItemsDbAdapter.KEY_INVQTY))
                        + " and to buy " + c.getString(c.getColumnIndexOrThrow(ItemsDbAdapter.KEY_BUYQTY)));

            }while(c.moveToNext());
        }
        c.close();

        EntryAdapter adapter = new EntryAdapter(SearchableActivity.this, items);
        setListAdapter(adapter);

        Log.e(TAG, "finished fillData");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // R.id.search is pulled by id from options_menu.xml, where there could be other menu items.
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        // The SearchView attempts to start an activity with the ACTION_SEARCH when a user submits a search query.
        // A searchable activity filters for the ACTION_SEARCH intent and searches for the query in some sort of data set.
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//        Log.e(TAG, "entered onListItemClick");
//        super.onListItemClick(l, v, position, id);
//
//        Intent i = new Intent(this, ItemEdit.class);
//        i.putExtra(ItemsDbAdapter.KEY_ROWID, id);
//
//        startActivityForResult(i, ACTIVITY_EDIT);
//        Log.e(TAG, "finished onListItemClick");
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        Log.e(TAG, "entered onActivityResult");
//        super.onActivityResult(requestCode, resultCode, intent);
//        //fillListRows();
//        Log.e(TAG, "finished onActivityResult");
//    }

}
