package com.adi.ho.jackie.pricecomparisonapp.EbayAPI;

import android.os.AsyncTask;

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
 * Created by Todo on 3/12/2016.
 */
public class Ebay {

    private double mPrice;

    private static String sandboxAppID = "generala-comadiho-SBX-b38ccaf50-0e858d47";
    private static String sandboxDevID = "9ed697e2-366b-4846-989e-1aeff5f39364";
    private static String sandboxCertID = "SBX-38ccaf50e169-3f1f-4f72-ac02-5bec";
    private static String productionAppID = "generala-comadiho-PRD-438ccaf50-460fbf19";
    private static String productionDevID = "9ed697e2-366b-4846-989e-1aeff5f39364";
    private static String productionCertID = "PRD-38ccaf50cfa6-1c67-4825-b2ce-483b";

    private static double price;

    private static String ebayLookupUpc = "http://svcs.ebay.com/services/search/FindingService/v1?OPERATION-NAME=findItemsByProduct" +
            "&SERVICE-VERSION=1.0.0" +
            "&SECURITY-APPNAME=" + productionAppID +
            "&RESPONSE-DATA-FORMAT=JSON" +
            "&REST-PAYLOAD" +
            "&paginationInput.entriesPerPage=2" +
            "&productId.@type=UPC" +
            "&productId=";

    private static String ebayLookupKeyword = "http://svcs.ebay.com/services/search/FindingService/v1?OPERATION-NAME=findItemsByKeywords" +
            "&SERVICE-VERSION=1.0.0" +
            "&SECURITY-APPNAME=" + productionAppID +
            "&RESPONSE-DATA-FORMAT=JSON" +
            "&REST-PAYLOAD" +
            "&keywords=";

    public Ebay(){}

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

    public static class EbayAsyncTask extends AsyncTask<String,Void,Double> {
        String data= " ";

        @Override
        protected Double doInBackground(String... urls) {

            try {
                URL url = new URL(ebayLookupUpc+urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                data = getInputData(inputStream);


            } catch (Throwable thr) {
                thr.fillInStackTrace();

            }
            try {
                JSONObject dataObject = new JSONObject(data);
                JSONArray itemArray= dataObject.optJSONArray("findItemsByProductResponse");
                JSONObject firstObject = itemArray.optJSONObject(0);
                JSONArray searchResult = firstObject.optJSONArray("searchResult");
                if(searchResult!=null){
                    JSONObject firstObject1 = searchResult.optJSONObject(0);
                    JSONArray itemArray2 = firstObject1.optJSONArray("item");
                    JSONObject firstObject2 = itemArray2.optJSONObject(0);
                    JSONArray sellingStatusArray = firstObject2.optJSONArray("sellingStatus");
                    JSONObject firstObject3 = sellingStatusArray.optJSONObject(0);
                    JSONArray currentPriceArray = firstObject3.optJSONArray("currentPrice");
                    JSONObject priceObject = currentPriceArray.optJSONObject(0);
                    price = priceObject.optDouble("__value__");}
                else {price = 0.0;}

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