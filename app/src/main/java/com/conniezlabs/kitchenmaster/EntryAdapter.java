package com.conniezlabs.kitchenmaster;


/**
 * Adapter to display entry items
 * Grateful for this source: http://stackoverflow.com/questions/11678909/use-array-adapter-with-more-views-in-row-in-listview
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EntryAdapter extends BaseAdapter {
    private List list;
    LayoutInflater inflater;
    Context context;

    public EntryAdapter(Context context, ArrayList<Entry> list) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(this.context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null) convertView = inflater.inflate(R.layout.item_row, parent, false);
        // assign the view we are converting to a local variable
        View v = convertView;
        Entry item = (Entry) list.get(position);
        if (item != null) {
            TextView t_name = (TextView) v.findViewById(R.id.text_name);
            TextView t_inv_qty = (TextView) v.findViewById(R.id.text_invqty);
            TextView t_buy_qty = (TextView) v.findViewById(R.id.text_buyqty);

            // check to see if each individual textview is null.
            // if not, assign some text!
            if (t_name != null){
                t_name.setText(item.getName());
            }
            if (t_inv_qty != null){
                t_inv_qty.setText(item.getInv_Qty());
            }
            if (t_buy_qty != null){
                t_buy_qty.setText(item.getBuy_Qty());
            }

        }

        // the view must be returned to our activity
        return v;
    }
}