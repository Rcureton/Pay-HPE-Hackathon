package com.adi.ho.jackie.pricecomparisonapp.WalmartAPI;

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

/**
 * Created by Ra on 3/12/16.
 */
public class Walmart {

    private double mPrice;

    private static double price;
    private static String walmartLookupUpc= "http://api.walmartlabs.com/v1/items?apiKey=jcpk6chshjwn5nbq2khnrvm9&upc=";

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

    public double getmPrice() {
        return mPrice;
    }

    public void setmPrice(double mPrice) {
        this.mPrice = mPrice;
    }
}