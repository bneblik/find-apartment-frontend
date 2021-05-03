package com.example.findapartment.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.findapartment.R;
import com.example.findapartment.adapters.ApartmentsAdapter;
import com.example.findapartment.clients.ApartmentClient;
import com.example.findapartment.clients.IRequestCallback;
import com.example.findapartment.helpers.SortTypesEnum;
import com.example.findapartment.helpers.ToastService;
import com.example.findapartment.models.Apartment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ApartmentListActivity extends AppCompatActivity {
    private ApartmentClient apartmentClient;
    private ListView lvApartments;
    private ApartmentsAdapter apartmentsAdapter;
    private ProgressBar progressBar;
    private TextView noApartmentsTextView;

    private int page = 0;
    private int pageSize = 3;
    private int totalPages = 1;

    private String sortBy;
    private String priceFrom;
    private String priceTo;
    private String propertySizeFrom;
    private String propertySizeTo;
    private String location;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartment_list);

        getFilterParams();

        lvApartments = (ListView) findViewById(R.id.lvApartments);
        ArrayList<Apartment> apartments = new ArrayList<Apartment>();
        apartmentsAdapter = new ApartmentsAdapter(this, apartments);
        lvApartments.setAdapter(apartmentsAdapter);
        apartmentClient = new ApartmentClient();

        noApartmentsTextView = findViewById(R.id.noApartmentsTextView);

        Button filtersBtn = (Button) findViewById(R.id.openFiltersBtn);
        filtersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), FiltersActivity.class);
                startActivity(intent);

            }
        });

        lvApartments.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount != 0 && page < totalPages - 1 && firstVisibleItem + visibleItemCount == totalItemCount && progressBar.getVisibility() != View.VISIBLE) {
                    page++;
                    fetchApartments();
                }
            }
        });


        View footer = getLayoutInflater().inflate(
                R.layout.progress_footer, null);
        progressBar = (ProgressBar)
                footer.findViewById(R.id.pbFooterLoading);
        lvApartments.addFooterView(progressBar);

        setOrderSpinner();
    }

    private void getFilterParams() {
        Intent intentNow = getIntent();
        priceFrom = intentNow.getStringExtra("priceFrom");
        priceTo = intentNow.getStringExtra("priceTo");
        propertySizeFrom = intentNow.getStringExtra("propertySizeFrom");
        propertySizeTo = intentNow.getStringExtra("propertySizeTo");
        location = intentNow.getStringExtra("location");
    }

    private void setOrderSpinner(){
        Spinner spinner = (Spinner) findViewById(R.id.orderSpinner);

        ArrayAdapter<SortTypesEnum> adapter = new ArrayAdapter<SortTypesEnum>(this, android.R.layout.simple_spinner_item, SortTypesEnum.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                apartmentsAdapter.clear();
                page = 0;
                SortTypesEnum selected = SortTypesEnum.values()[position];
                sortBy = selected.name();
                fetchApartments();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void fetchApartments() {
        progressBar.setVisibility(View.VISIBLE);

        Uri.Builder builder = new Uri.Builder();
        builder.appendQueryParameter("page", String.valueOf(page));
        builder.appendQueryParameter("pageSize", String.valueOf(pageSize));
        builder.appendQueryParameter("sort", sortBy);
        if (priceFrom != null && priceFrom.length() > 0) builder.appendQueryParameter("priceFrom", priceFrom);
        if (priceFrom != null && priceFrom.length() > 0) builder.appendQueryParameter("priceTo", priceTo);
        if (priceFrom != null && priceFrom.length() > 0) builder.appendQueryParameter("propertySizeFrom", propertySizeFrom);
        if (priceFrom != null && priceFrom.length() > 0) builder.appendQueryParameter("propertySizeTo", propertySizeTo);
        if (priceFrom != null && priceFrom.length() > 0) builder.appendQueryParameter("location", location);
        String queryParams =  builder.build().toString();


        apartmentClient.getApartments(getApplicationContext(), queryParams, new IRequestCallback(){
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONObject data = null;
                    if (response != null) {
                        data = response.getJSONObject("data");
                        totalPages = Integer.parseInt(data.getString("pages"));
                        final ArrayList<Apartment> apartments = Apartment.fromJSON(data.getJSONArray("apartments"));
                        for (Apartment apartment: apartments){
                            apartmentsAdapter.add(apartment);
                        }
                        apartmentsAdapter.notifyDataSetChanged();
                        if (apartments.size() == 0) {
                            noApartmentsTextView.setVisibility(View.VISIBLE);
                        } else {
                            noApartmentsTextView.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onError(String result) throws Exception {
                ToastService.showErrorMessage("Nie można załadować ogłoszeń", getApplicationContext());
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}