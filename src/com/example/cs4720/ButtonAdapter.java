package com.example.cs4720;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

public class ButtonAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> data;
    private OnClickListener clickListener;

    public ButtonAdapter(Context c, ArrayList<String> data, OnClickListener listener) {
        mContext = c;
        this.data = data;
        clickListener = listener;
    }

    public int getCount() {
    	return data.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }
    
    public void add( String s ) {
    	data.add(s);
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        Button button;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	button = new Button(mContext);
        	button.setLayoutParams(new GridView.LayoutParams(195, 190));
        	button.setPadding(5, 5, 5, 5);
        	button.setBackgroundResource(R.drawable.bluesquare);
        	button.setTextColor(Color.WHITE);
        	button.setTextSize(25);
        } else {
        	button = (Button) convertView;
        }
        
        button.setText(data.get(position));
        button.setOnClickListener(clickListener);

        return button;
    }
}
