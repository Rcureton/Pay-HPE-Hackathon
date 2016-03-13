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

    private double mPrice;
    private int mItemId;

    private static double price;
    private static int id;
    private static ArrayList<String> reviews;
    private static String walmartLookupUpc= "http://api.walmartlabs.com/v1/items?apiKey=jcpk6chshjwn5nbq2khnrvm9&upc=";
    private static String walmartReviewById1= "http://api.walmartlabs.com/v1/reviews/";
    private static String walmartReviewById2= "?format=json&apiKey=jcpk6chshjwn5nbq2khnrvm9";

    public Walmart(){}

    private static String getInputData(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder= new StringBuilder();
        BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream));
        String data;

        while ((data=bufferedReader.readLine()) !=null){
            stringBuilder.append(data);
        }
        bufferedReader.close();

        return stringBuilder.toString();
    }

    public static class WalmartAsyncTask extends AsyncTask<String,Void,Double> {
        String data= " ";

        @Override
        protected Double doInBackground(String... urls) {

            try {
                URL url = new URL(walmartLookupUpc+urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                data = getInputData(inputStream);


            } catch (Throwable thr) {
                thr.fillInStackTrace();

            }
            try {
                JSONObject dataObject = new JSONObject(data);
                JSONArray priceArray= dataObject.optJSONArray("items");
                JSONObject item = priceArray.optJSONObject(0);
                    price = item.optDouble("salePrice");

            } catch (JSONException e) {

                e.printStackTrace();
            }
            return price;
        }

        @Override
        protected void onPostExecute(Double s) {
            super.onPostExecute(s);

        }
    }
    public static class WalmartIDAsyncTask extends AsyncTask<String,Void,Integer> {
        String data= " ";

        @Override
        protected Integer doInBackground(String... urls) {

            try {
                URL url = new URL(walmartLookupUpc+urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                data = getInputData(inputStream);


            } catch (Throwable thr) {
                thr.fillInStackTrace();

            }
            try {
                JSONObject dataObject = new JSONObject(data);
                JSONArray priceArray= dataObject.optJSONArray("items");
                JSONObject item = priceArray.optJSONObject(0);
                id = item.optInt("itemId");

            } catch (JSONException e) {

                e.printStackTrace();
            }
            return id;
        }

        @Override
        protected void onPostExecute(Integer s) {
            super.onPostExecute(s);

        }
    }

    public static class WalmartReviewAsyncTask extends AsyncTask<Integer,Void,ArrayList<String>> {
        String JSONdata= " ";

        @Override
        protected ArrayList<String> doInBackground(Integer... id) {

            try {
                URL url = new URL(walmartReviewById1+id[0]+walmartReviewById2);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                JSONdata = getInputData(inputStream);


            } catch (Throwable thr) {
                thr.fillInStackTrace();

            }
            try {
                JSONObject dataObject = new JSONObject(JSONdata);
                JSONArray reviewArray= dataObject.optJSONArray("reviews");
                for (int x = 0; x<reviewArray.length(); x++){
                    JSONObject item = reviewArray.optJSONObject(x);
                    reviews.add(item.optString("reviewText",""));
                }

            } catch (JSONException e) {

                e.printStackTrace();
            }
            return reviews;
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            super.onPostExecute(s);

        }
    }


    public double getmPrice() {
        return mPrice;
    }

    public void setmPrice(double mPrice) {
        this.mPrice = mPrice;
    }

    public int getmItemId() {
        return mItemId;
    }

    public void setmItemId(int mItemId) {
        this.mItemId = mItemId;
    }
}