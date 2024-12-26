package com.example.software2.dapp.AmbulanceViewAccident;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GetAllData extends IntentService {
    private static final String IDENTIFIER = "GetAddressIntentService";
    /**An identifier is a name that identifies either a unique object or
     *a unique class of objects from another java activities
     **/
    private ResultReceiver addressResultReceiver;

    /**create object ResultReceiver
     *to receive the address result**/
    static String addressDetails;

    public GetAllData() {
        super(IDENTIFIER);

        /** super keyword is used to access methods of the parent class
         *while this is used to access methods of the current class.
         *this is a reserved keyword in java i.e, we can't use it as
         *an identifier. This is used to refer current class's instance
         *as well as static members.**/
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String msg;
        addressResultReceiver = Objects.requireNonNull(intent).getParcelableExtra("add_receiver");
        if (addressResultReceiver == null) {
            return;
        }
        Location location = intent.getParcelableExtra("add_location");
        if (location == null) {
            msg = "No location, can't go further without location";
            sendResultsToReceiver(0, msg);
            return;
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (addresses == null || addresses.isEmpty()) {
            msg = "No address found for the location";
            sendResultsToReceiver(1, msg);
        } else {
            Address address = addresses.get(0);
            addressDetails = "Locality is, " + address.getSubLocality() + "." + "\n" + "City is ," + address.getSubAdminArea() + "." + "\n" +
                    "State is, " + address.getAdminArea() + "." + "\n" + address.getCountryName() + "." + "\n";
            sendResultsToReceiver(2, addressDetails);
        }
    }

    private void sendResultsToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString("address_result", message);
        addressResultReceiver.send(resultCode, bundle);
    }
}