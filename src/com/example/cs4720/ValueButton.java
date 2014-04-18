package com.example.cs4720;

import java.util.ArrayList;

import android.content.Context;
import android.widget.Button;

public class ValueButton extends Button {

	private ArrayList<String> values;
	public ValueButton(Context context) {
		super(context);
		
		values = new ArrayList<String>();
	}
	
	public void addValue(String value)
	{
		values.add(value);
	}
	
	public boolean removeValue(String value)
	{
		return values.remove(value);
	}
	
	public boolean removeValue(int position)
	{
		return values.remove(position) != null;
	}
	
	public String getValue(int position)
	{
		return values.get(position);
	}

}
