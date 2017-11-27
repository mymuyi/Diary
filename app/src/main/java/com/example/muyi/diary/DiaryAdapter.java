package com.example.muyi.diary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by muyi on 17-11-20.
 */

public class DiaryAdapter extends ArrayAdapter<Diary> {

    private int resourceId;
    private Context mContext;

    public DiaryAdapter(Context context, int resource, List<Diary> objects) {
        super(context, resource, objects);
        resourceId = resource;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Diary diary = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        } else {
            view = convertView;
        }
        TextView title = view.findViewById(R.id.list_item_title);
        final TextView content = view.findViewById(R.id.list_item_content);
        TextView time = view.findViewById(R.id.list_item_time);
        title.setText(diary.getTitle());
        time.setText(diary.getTime());
        content.setText(diary.getContent());
        return view;
    }
}
