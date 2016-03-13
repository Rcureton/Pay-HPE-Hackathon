package com.adi.ho.jackie.pricecomparisonapp.WalmartAPI;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Ra on 3/12/16.
 */
public class Walmart {

    private String mPrice;
    private int mItemId;
    private String mUPC;


    private static ArrayList<String> reviews;


    public Walmart(
    ) {
    }

    public static ArrayList<String> getReviews() {
        return reviews;
    }

    public static void setReviews(ArrayList<String> reviews) {
        Walmart.reviews = reviews;
    }


    public void setmUPC(String mUPC) {
        this.mUPC = mUPC;
    }
    public String getmUPC(){
        return mUPC;
    }

    public String getmPrice() {
        return mPrice;
    }

    public void setmPrice(String mPrice) {
        this.mPrice = mPrice;
    }

    public int getmItemId() {
        return mItemId;
    }

    public void setmItemId(int mItemId) {
        this.mItemId = mItemId;
    }
}