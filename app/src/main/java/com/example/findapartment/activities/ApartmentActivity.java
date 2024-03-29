package com.example.findapartment.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.findapartment.R;
import com.example.findapartment.adapters.SliderAdapter;
import com.example.findapartment.clients.ApartmentClient;
import com.example.findapartment.clients.IRequestCallback;
import com.example.findapartment.fragments.GalleryDialogFragment;
import com.example.findapartment.fragments.ImageGallery;
import com.example.findapartment.fragments.ToolbarFragment;
import com.example.findapartment.helpers.AppViewNames;
import com.example.findapartment.helpers.ToastService;
import com.example.findapartment.helpers.TransactionTypeEnum;
import com.example.findapartment.models.Apartment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ApartmentActivity extends AppCompatActivity {

    private ApartmentClient apartmentClient;
    private String apartmentId;

    SliderAdapter mViewPagerAdapter;
    List<String> imagesPaths;

    ImageGallery imageGallery;
    ProgressBar progressBar;

    Apartment apartment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartment);

        Intent intentNow = getIntent();
        apartmentId = intentNow.getStringExtra("apartmentID");
        apartmentClient = new ApartmentClient();

        ToolbarFragment toolbarFragment = (ToolbarFragment) getSupportFragmentManager().findFragmentById(R.id.menuFragment);
        toolbarFragment.setImageTint(AppViewNames.APARTMENTS_LIST);

        imagesPaths = new ArrayList<String>();

        mViewPagerAdapter = new SliderAdapter(imagesPaths){
            @Override
            public void onBindViewHolder(@NonNull SliderAdapter.SliderViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                holder.itemView.findViewById(R.id.itemImageView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = GalleryDialogFragment.newInstance(mViewPagerAdapter);
                        newFragment.show(getSupportFragmentManager(), "newFragment");
                    }
                });
            }
        };

        FragmentManager fm = getSupportFragmentManager();
        imageGallery = (ImageGallery) fm.findFragmentById(R.id.apartmentGalleryFragment);
        imageGallery.setImagesAdapter(mViewPagerAdapter);

        progressBar = findViewById(R.id.apartmentActivityProgressBar);

        fetchApartment();
    }

    private String formatNumber(Float number) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setCurrencySymbol("");
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(number);
    }

    private void fetchApartment() {
        progressBar.setVisibility(View.VISIBLE);
        apartmentClient.getApartment(getApplicationContext(), apartmentId, new IRequestCallback(){
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONObject data = null;
                    if (response != null) {
                        data = response.getJSONObject("data");
                        apartment = Apartment.fromJSON(data);

                        ((TextView) findViewById(R.id.priceTv)).setText(formatNumber(apartment.getPrice()) + " zł");
                        ((TextView) findViewById(R.id.propertySizeTv)).setText(Html.fromHtml(formatNumber(apartment.getPropertySize())+ " " + getResources().getString(R.string.m2)));
                        ((TextView) findViewById(R.id.locationTv)).setText(apartment.getLocation());

                        ((TextView) findViewById(R.id.transactionTextView)).setText(TransactionTypeEnum.getEnumValueByName(apartment.getTransactionType()));

                        SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        Date date1= fromFormat.parse(apartment.getPublicationDate());
                        SimpleDateFormat targetFormat = new SimpleDateFormat("dd.MM.yyy HH:mm");
                        String date2 = targetFormat.format(date1);

                        ((TextView) findViewById(R.id.publicationDateTv)).setText(date2);
                        ((TextView) findViewById(R.id.phoneNumberTv)).setText(apartment.getPhoneNumber());
                        ((TextView) findViewById(R.id.emailTv)).setText(apartment.getEmail());
                        ((TextView) findViewById(R.id.descriptionTv)).setText(apartment.getDescription());


                        displayImages(apartment);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } finally {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onError(String result) throws Exception {
                ToastService.showErrorMessage(result, findViewById(R.id.rootView));
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void displayImages(Apartment apartment){

        if (apartment.getImages().size() > 0) {
            for (int i = 0; i < apartment.getImages().size(); i++) {
                imagesPaths.add(apartment.getImage(i));
            }
            imageGallery.showCircles();
            mViewPagerAdapter.notifyDataSetChanged();
        } else {
            imageGallery.showNoImagePlaceholder();
        }
    }

    public boolean isGalleryDialogOpened() {
        ImageGallery f = (ImageGallery)  getSupportFragmentManager().findFragmentById(R.id.galleryDialogFragment);
        return f != null && f.isAdded();
    }
}