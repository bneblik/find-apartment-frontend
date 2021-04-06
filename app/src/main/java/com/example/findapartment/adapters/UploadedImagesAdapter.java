package com.example.findapartment.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.findapartment.R;
import com.example.findapartment.activities.apartments.ApartmentActivity;
import com.example.findapartment.models.Apartment;

import java.util.ArrayList;

public class UploadedImagesAdapter extends ArrayAdapter<Uri> {

    private Context c;
    private ArrayList<Uri> uris;

    private static class ViewHolder {
        public ImageView uploadedImageIV;
    }

    public UploadedImagesAdapter(Context context, ArrayList<Uri> uris) {
        super(context,0,  uris);
        this.c = context;
        this.uris = uris;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Uri uri = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.fragment_uploaded_image, parent, false);
            viewHolder.uploadedImageIV = (ImageView) convertView.findViewById(R.id.uploadedImageIV);
            convertView.setTag(viewHolder);
        } else {
            // Existing view
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.uploadedImageIV.setImageURI(uri);

        ImageButton deleteImageBtn = (ImageButton) convertView.findViewById(R.id.deleteUploadedImageBtn);
        deleteImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO delete uploaded

            }
        });

        return convertView;
    }


}