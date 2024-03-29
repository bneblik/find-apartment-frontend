package com.example.findapartment.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.findapartment.R;
import com.example.findapartment.activities.AddApartmentActivity;
import com.example.findapartment.activities.ApartmentActivity;
import com.example.findapartment.activities.ApartmentListActivity;
import com.example.findapartment.activities.MyAccountActivity;
import com.example.findapartment.clients.IRequestCallback;
import com.example.findapartment.helpers.ToastService;
import com.example.findapartment.models.Apartment;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;

public class ApartmentsAdapter extends ArrayAdapter<Apartment> {

    private Context c;
    private @LayoutRes int layout;

    private static class ViewHolder {
        public TextView tvLocation;
        public TextView tvPrice;
        public TextView tvTransactionType;
        public TextView tvPropertySize;
        public ImageView ivImage;
        public ProgressBar imageProgressBar;
    }

    public ApartmentsAdapter(Context context, ArrayList<Apartment> apartments, @LayoutRes int resource) {
        super(context,0,  apartments);
        this.c = context;
        this.layout = resource;
    }

    private void openApartmentDetails(Apartment apartment) {
        Intent intent = new Intent(c, ApartmentActivity.class);
        intent.putExtra("apartmentID", apartment.getId());
        c.startActivity(intent);
    }

    private String formatNumber(Float number) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setCurrencySymbol("");
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(number);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Apartment apartment = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, parent, false);
            viewHolder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
            viewHolder.tvPrice = (TextView) convertView.findViewById(R.id.tvPrice);
            viewHolder.tvTransactionType = (TextView) convertView.findViewById(R.id.tvTransactionType);
            viewHolder.tvPropertySize = (TextView) convertView.findViewById(R.id.tvPropertySize);
            viewHolder.ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
            viewHolder.imageProgressBar = (ProgressBar) convertView.findViewById(R.id.imageProgressBar);
            convertView.setTag(viewHolder);
        } else {
            // Existing view
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvPrice.setText(formatNumber(apartment.getPrice()) + " zł");
        viewHolder.tvLocation.setText(apartment.getLocation());
        viewHolder.tvTransactionType.setText(apartment.getTransactionType().equals("SALE") ? "sprzedaż" : "wynajem");
        viewHolder.tvPropertySize.setText(Html.fromHtml(formatNumber(apartment.getPropertySize()) + " " + c.getResources().getString(R.string.m2)) );

        if (apartment.getImages().size() > 0) {
            viewHolder.imageProgressBar.setVisibility(View.VISIBLE);
            Glide.with(c)
                    .load(apartment.getImage(0))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            viewHolder.imageProgressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            viewHolder.imageProgressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .error(R.drawable.imageplaceholder)
                    .into(viewHolder.ivImage);


        } else {
            viewHolder.ivImage.setImageDrawable(c.getResources().getDrawable(R.drawable.imageplaceholder));
        }
        RelativeLayout item = (RelativeLayout) convertView.findViewById(R.id.apartmentLayout);
        if (item != null) {
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openApartmentDetails(apartment);
                }
            });
        }

        if (c instanceof MyAccountActivity) {

            ImageButton moreActionsButton = (ImageButton) convertView.findViewById(R.id.moreActionsBtn);
            if (moreActionsButton != null) {
                moreActionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(c, v);
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.manage_announcement, popup.getMenu());
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getTitle().equals("zobacz ogłoszenie")) {
                                    openApartmentDetails(apartment);
                                } else if (item.getTitle().equals("edytuj ogłoszenie")) {
                                    onEditApartmentClick(apartment);
                                } else if (item.getTitle().equals("usuń ogłoszenie")) {
                                    ((MyAccountActivity) c).onDeleteApartmentClick(apartment.getId(), position);
                                }
                                return true;
                            }
                        });
                        popup.show();
                    }
                });
            }
        }

        return convertView;
    }



    private void onEditApartmentClick(Apartment apartment) {
        Intent i=new Intent(c, AddApartmentActivity.class);
        i.putExtra("editedApartmentId", apartment.getId());
//        i.putExtra("propertySizeEditText", apartment.getPropertySize().toString());
//        i.putExtra("locationEditText", apartment.getLocation());
//        i.putExtra("descriptionEditText", apartment.getDescription());
//        i.putExtra("priceEditText", apartment.getPrice().toString());
//        i.putExtra("transactionType", apartment.getTransactionType());
        c.startActivity(i);
    }
}
