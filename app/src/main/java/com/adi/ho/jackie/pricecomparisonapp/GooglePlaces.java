package com.adi.ho.jackie.pricecomparisonapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class GooglePlaces extends AppCompatActivity {
    int PLACE_PICKER_REQUEST = 1;
    private String TAG= " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_places);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
               String uriString= getUriString(place);
                Uri googleMaps= Uri.parse(uriString);
                Intent mapIntent= new Intent(Intent.ACTION_VIEW,googleMaps);
                mapIntent.setPackage("com.google.android.apps.maps");

                if(mapIntent.resolveActivity(getPackageManager()) !=null){
                    startActivity(mapIntent);
                }else{
                    Toast.makeText(GooglePlaces.this, "No Maps Installed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getUriString(Place place) {
        LatLng latLong= place.getLatLng();
        String lat= String.valueOf(latLong.latitude);
        String lon= String.valueOf(latLong.longitude);
        String address =place.getAddress().toString();
        String name= place.getName().toString();
        String encodedAddress= Uri.encode(name+", "+address);

        String uriString= "geo:"+lat+","+lon+"?q="+encodedAddress;

        return uriString;
    }
}
