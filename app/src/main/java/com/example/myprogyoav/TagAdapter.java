package com.example.myprogyoav;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class TagAdapter extends ArrayAdapter<String> {
    Context context;
    List<String> tags;

    public TagAdapter(Context context,int resource,int textViewResourceId,List<String> tagslist){
        super(context,resource,textViewResourceId,tagslist);

        this.context = context;
        this.tags = tagslist;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Reuse the convertView if it is not null, otherwise inflate a new view
        if (convertView == null) {
            LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.tag, parent, false);
        }

        TextView tag1 = (TextView) convertView.findViewById(R.id.TagText);



        // Get the current question from the list
        String temp = tags.get(position);
        Log.d("get", temp);
        tag1.setText(temp);
        return convertView;
    }
}
