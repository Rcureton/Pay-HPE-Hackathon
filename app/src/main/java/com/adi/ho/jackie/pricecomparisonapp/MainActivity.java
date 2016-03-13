package com.adi.ho.jackie.pricecomparisonapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import android.support.design.widget.FloatingActionButton;

import android.support.v4.view.MenuItemCompat;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adi.ho.jackie.pricecomparisonapp.EbayAPI.Ebay;
import com.adi.ho.jackie.pricecomparisonapp.WalmartAPI.Walmart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hod.api.hodclient.HODApps;
import hod.api.hodclient.HODClient;
import hod.api.hodclient.IHODClientCallback;
import hod.response.parser.AutoCompleteResponse;
import hod.response.parser.BarcodeRecognitionResponse;
import hod.response.parser.HODResponseParser;
import hod.response.parser.SentimentAnalysisResponse;

public class MainActivity extends AppCompatActivity implements IHODClientCallback {
    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;
    private String mImageFullPathAndName = "";
    private String localImagePath = "";
    private static final int OPTIMIZED_LENGTH = 1024;

    private TextView mWalmartComparison, mEbayComparison;
    private Toolbar mToolbar;
    private ActionBar mActionbar;
    private Button mButton;
    private CardView mCard;
    public String mUPCofProduct;
    public ArrayList<String> mReviews;
    private ArrayList<Walmart> mListFromUpc;
    private static String productionAppID = "generala-comadiho-PRD-438ccaf50-460fbf19";
    private static String walmartLookupUpc = "http://api.walmartlabs.com/v1/items?apiKey=jcpk6chshjwn5nbq2khnrvm9&upc=";
    private static String walmartReviewById1 = "http://api.walmartlabs.com/v1/reviews/";
    private static String walmartReviewById2 = "?format=json&apiKey=jcpk6chshjwn5nbq2khnrvm9";
    private static String walmartLookupKeyword = "http://api.walmartlabs.com/v1/search?query=";
    private static final String walmart_api_key ="&format=json&apiKey=5hbnkvrdvq3dafvfax34meez";
    private SearchView searchView;
    private ArrayList<String> mAutoCompleteList;
    private ArrayAdapter<String> mAdapter;
    private ListView mListView;

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
    public static final String REVIEW_ARRAY_KEY = "review";

    private static final String API_KEY = "f3c5459e-f77d-40f6-b53f-43154e4559f9";
    HODClient hodClient = new HODClient(API_KEY, this);
    HODResponseParser parser = new HODResponseParser();
    String hodApp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWalmartComparison = (TextView) findViewById(R.id.walmartPriceComparison);
        mEbayComparison = (TextView) findViewById(R.id.ebayPriceComparison);
        mToolbar = (Toolbar) findViewById(R.id.maintoolbar);
        mButton = (Button) findViewById(R.id.reviewsButton);
        mListView = (ListView)findViewById(R.id.suggestion_list);
        mAutoCompleteList = new ArrayList<>();
        mAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,mAutoCompleteList);
        mCard = (CardView)findViewById(R.id.xmlPriceComparison);

        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(mToolbar);
        mActionbar = getSupportActionBar();
        mActionbar.setTitle("");
        mActionbar.setHomeButtonEnabled(true);
        mReviews = new ArrayList<>();
        mListFromUpc = new ArrayList<>();

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                String search =text.getText().toString();
                System.out.println(search);
                new WalmartKeywordAsyncTask().execute(search);

            }
        });
        CreateLocalImageFolder();

        FloatingActionButton button=(FloatingActionButton)findViewById(R.id.maps);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this,GooglePlaces.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void requestCompletedWithContent(String response) {
        if (hodApp.equals(HODApps.RECOGNIZE_BARCODES)) {

            Log.i("Request completed", response);
            BarcodeRecognitionResponse resp = parser.ParseBarcodeRecognitionResponse(response);

            if (resp != null) {
                if (resp.barcode.size()>1){
                        new WalmartMultiBarAsyncTask().execute(resp.barcode);

                } else {
                    mUPCofProduct = resp.barcode.get(0).text.substring(1);
                    Log.i("Response", "UPC IS: " + mUPCofProduct);

                    String walmartsPrice = "Product Unavailable";
                new WalmartAsyncTask().execute(mUPCofProduct);
                new EbayAsyncTask().execute(mUPCofProduct);
                mCard.setVisibility(View.VISIBLE);

                    new WalmartAsyncTask().execute(mUPCofProduct);
                    new EbayAsyncTask().execute(mUPCofProduct);
                }
            }
        } else if (hodApp.equals(HODApps.ANALYZE_SENTIMENT)) {
            Log.i("Request completed", response);
            SentimentAnalysisResponse resp = parser.ParseSentimentAnalysisResponse(response);
            if (resp != null) {
                if (resp.aggregate.score > 0.2) {
                    mWalmartComparison.setTextColor(Color.GREEN);
                    mEbayComparison.setTextColor(Color.GREEN);
                } else if (resp.aggregate.score <= 0.2 && resp.aggregate.score >= -0.2) {
                    mWalmartComparison.setTextColor(Color.GRAY);
                    mEbayComparison.setTextColor(Color.GRAY);
                } else {
                    mWalmartComparison.setTextColor(Color.RED);
                    mEbayComparison.setTextColor(Color.RED);
                }
            }

            mButton.setVisibility(View.VISIBLE);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ReviewActivity.class);
                    intent.putExtra(REVIEW_ARRAY_KEY,mReviews);
                    startActivity(intent);
                }
            });
        } else if (hodApp.equals(HODApps.AUTO_COMPLETE)){
            AutoCompleteResponse resp = parser.ParseAutoCompleteResponse(response);
            if (resp.words != null && !resp.words.isEmpty()){
                mAutoCompleteList.clear();
                mAutoCompleteList.addAll(resp.words);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void requestCompletedWithJobID(String response) {
        String jobID = parser.ParseJobID(response);
        if (jobID.length() > 0)
            hodClient.GetJobResult(jobID);
    }

    @Override
    public void onErrorOccurred(String errorMessage) {
        Log.e("HP ERROR", "Error occured on api call");

    }

    private void getListItemData(String query){
        hodApp = HODApps.AUTO_COMPLETE;
        Map<String,Object> params = new HashMap<>();
        params.put("text", query);
        hodClient.PostRequest(params, hodApp, HODClient.REQ_MODE.ASYNC);
    }

    // loading image part
    private void callHODAPI() {
        hodApp = HODApps.RECOGNIZE_BARCODES;
        Map<String, Object> params = new HashMap<>();
        params.put("file", mImageFullPathAndName);
        hodClient.PostRequest(params, hodApp, HODClient.REQ_MODE.ASYNC);
    }

    public void CreateLocalImageFolder() {
        if (localImagePath.length() == 0) {
            localImagePath = getFilesDir().getAbsolutePath() + "/orc/";
            File folder = new File(localImagePath);
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();
            }
            if (!success)
                Toast.makeText(this, "Cannot create local folder", Toast.LENGTH_LONG).show();
        }
    }

    public Bitmap decodeFile(File file) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        int mImageRealWidth = options.outWidth;
        int mImageRealHeight = options.outHeight;
        Bitmap pic = null;
        try {
            pic = BitmapFactory.decodeFile(file.getPath(), options);
        } catch (Exception ex) {
            Log.e("MainActivity", ex.getMessage());
        }
        return pic;
    }

    public Bitmap rescaleBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    private Bitmap rotateBitmap(Bitmap pic, int deg) {
        Matrix rotate90DegAntiClock = new Matrix();
        rotate90DegAntiClock.preRotate(deg);
        Bitmap newPic = Bitmap.createBitmap(pic, 0, 0, pic.getWidth(), pic.getHeight(), rotate90DegAntiClock, true);
        return newPic;
    }

    private String SaveImage(Bitmap image) {
        String fileName = localImagePath + "imagetoocr.jpg";
        try {

            File file = new File(fileName);
            FileOutputStream fileStream = new FileOutputStream(file);

            image.compress(Bitmap.CompressFormat.JPEG, 100, fileStream);
            try {
                fileStream.flush();
                fileStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fileName;
    }

    public void DoTakePhoto(View view) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, TAKE_PICTURE);
    }

    public void DoShowSelectImage(View v) {
        Intent i = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE || requestCode == TAKE_PICTURE) {
            if (resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mImageFullPathAndName = cursor.getString(columnIndex);
                cursor.close();
                File file = new File(mImageFullPathAndName);
                Bitmap mCurrentSelectedBitmap = decodeFile(file);

                if (mCurrentSelectedBitmap != null) {
                    // display the full size image
                    //ivSelectedImg.setImageBitmap(mCurrentSelectedBitmap);
                    // scale the image
                    // let check the resolution of the image.
                    // If it's too large, we can optimize it
                    int w = mCurrentSelectedBitmap.getWidth();
                    int h = mCurrentSelectedBitmap.getHeight();

                    int length = (w > h) ? w : h;
                    if (length > OPTIMIZED_LENGTH) {
                        // let's resize the image
                        float ratio = (float) w / h;
                        int newW, newH = 0;

                        if (ratio > 1.0) {
                            newW = OPTIMIZED_LENGTH;
                            newH = (int) (OPTIMIZED_LENGTH / ratio);
                        } else {
                            newH = OPTIMIZED_LENGTH;
                            newW = (int) (OPTIMIZED_LENGTH * ratio);
                        }
                        mCurrentSelectedBitmap = rescaleBitmap(mCurrentSelectedBitmap, newW, newH);
                    }
                    // let save the new image to our local folder
                    mImageFullPathAndName = SaveImage(mCurrentSelectedBitmap);
                    callHODAPI();
                }
            }
        }
    }

    private void checkReviewsSentiment(ArrayList<String> allReviews) {
        StringBuilder str = new StringBuilder(allReviews.get(0) + " ");
        String reviewTogether = "";
        for (int i = 1; i < allReviews.size(); i++) {
            str.append(allReviews.get(i) + " ");
        }
        reviewTogether = str.toString();
        getSentimentFromReviews(reviewTogether);
    }


    private void getSentimentFromReviews(String reviews){
        hodApp = HODApps.ANALYZE_SENTIMENT;
        Map<String,Object> params = new HashMap<>();
        params.put("text",reviews);
        hodClient.PostRequest(params, hodApp, HODClient.REQ_MODE.ASYNC);
    }

    public class WalmartAsyncTask extends AsyncTask<String, Void, ArrayList<Walmart>> {
        String data = " ";
        String price;
        private ArrayList<Walmart> walmartList;

        public WalmartAsyncTask() {
            walmartList = new ArrayList<>();
        }

        @Override
        protected ArrayList<Walmart> doInBackground(String... urls) {

            try {
                URL url = new URL(walmartLookupUpc + urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                data = getInputData(inputStream);


            } catch (Throwable thr) {
                thr.fillInStackTrace();

            }
            try {
                JSONObject dataObject = new JSONObject(data);
                JSONArray priceArray = dataObject.optJSONArray("items");
                JSONObject item = priceArray.optJSONObject(0);
                Walmart walmart = new Walmart();
                price = item.optString("salePrice", "Product Unavailable");
                walmart.setmPrice(price);
                walmart.setmItemId(item.getInt("itemId"));
                walmartList.add(walmart);

            } catch (JSONException e) {

                e.printStackTrace();
            }
            return walmartList;
        }

        @Override
        protected void onPostExecute(ArrayList<Walmart> walmartArrayList) {
            mListFromUpc = walmartArrayList;
            mWalmartComparison.setText("Price at Walmart is: $" + mListFromUpc.get(0).getmPrice());
             new WalmartReviewAsyncTask().execute(mListFromUpc.get(0).getmItemId());
            mCard.setVisibility(View.VISIBLE);
            //new WalmartReviewAsyncTask().execute(44465724);
        }
    }
    public class WalmartMultiBarAsyncTask extends AsyncTask<List<BarcodeRecognitionResponse.Barcode>, Void, ArrayList<Walmart>> {
        String data = " ";
        String price;
        private ArrayList<Walmart> walmartList;
        private List<BarcodeRecognitionResponse.Barcode> barcodeList;
        String barcodeUPC="";

        public WalmartMultiBarAsyncTask() {
            walmartList = new ArrayList<>();
            barcodeList = new ArrayList<>();
        }

        @Override
        protected ArrayList<Walmart> doInBackground(List<BarcodeRecognitionResponse.Barcode>... urls) {

            barcodeList = urls[0];
            for (BarcodeRecognitionResponse.Barcode barcode : barcodeList) {
                try {
                    barcodeUPC = barcode.text.substring(1);
                    URL url = new URL(walmartLookupUpc + barcode.text.substring(1));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    data = getInputData(inputStream);


                } catch (Throwable thr) {
                    thr.fillInStackTrace();
                    continue;

                }
                try {
                    JSONObject dataObject = new JSONObject(data);
                    JSONArray priceArray = dataObject.optJSONArray("items");
                    JSONObject item = priceArray.optJSONObject(0);
                    Walmart walmart = new Walmart();
                    price = item.optString("salePrice", "Product Unavailable");
                    walmart.setmPrice(price);
                    walmart.setmItemId(item.getInt("itemId"));
                    walmart.setmUPC(barcodeUPC);
                    walmartList.add(walmart);

                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }
                if (walmartList != null && walmartList.size()>0 && walmartList.get(0) != null){
                    return walmartList;
                }
            }
            return walmartList;
        }

        @Override
        protected void onPostExecute(ArrayList<Walmart> walmartArrayList) {
            mListFromUpc = walmartArrayList;
            mWalmartComparison.setText("Price at Walmart is: $" + mListFromUpc.get(0).getmPrice());
             new WalmartReviewAsyncTask().execute(mListFromUpc.get(0).getmItemId());
            new EbayAsyncTask().execute(mListFromUpc.get(0).getmUPC());
            mCard.setVisibility(View.VISIBLE);
            //new WalmartReviewAsyncTask().execute();
        }
    }


    public class WalmartReviewAsyncTask extends AsyncTask<Integer, Void, ArrayList<String>> {
        String JSONdata = " ";
        ArrayList<String> reviews;

        public WalmartReviewAsyncTask() {
            reviews = new ArrayList<>();
        }

        @Override
        protected ArrayList<String> doInBackground(Integer... id) {

            try {
                URL url = new URL(walmartReviewById1 + id[0] + walmartReviewById2);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                JSONdata = getInputData(inputStream);


            } catch (Throwable thr) {
                thr.fillInStackTrace();

            }
            try {
                JSONObject dataObject = new JSONObject(JSONdata);
                JSONArray reviewArray = dataObject.optJSONArray("reviews");
                if (reviewArray != null && reviewArray.length() > 0) {
                    for (int x = 0; x < reviewArray.length(); x++) {
                        JSONObject item = reviewArray.optJSONObject(x);
                        reviews.add(item.optString("reviewText", ""));
                    }
                }

            } catch (JSONException e) {

                e.printStackTrace();
            }
            return reviews;
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {

            if (!mReviews.isEmpty()) {
                mReviews.clear();
            }
            mReviews = s;

            if (mReviews.size() > 0) {
                checkReviewsSentiment(mReviews);
            }

        }
    }

    private String getInputData(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String data;

        while ((data = bufferedReader.readLine()) != null) {
            stringBuilder.append(data);
        }
        bufferedReader.close();

        return stringBuilder.toString();
    }

    public class EbayAsyncTask extends AsyncTask<String, Void, String> {
        String data = " ";
        String price = "";

        @Override
        protected String doInBackground(String... urls) {

            try {
                URL url = new URL(ebayLookupUpc + urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                data = getInputData(inputStream);


            } catch (Throwable thr) {
                thr.fillInStackTrace();

            }
            try {
                JSONObject dataObject = new JSONObject(data);
                JSONArray itemArray = dataObject.optJSONArray("findItemsByProductResponse");
                JSONObject firstObject = itemArray.optJSONObject(0);
                JSONArray searchResult = firstObject.optJSONArray("searchResult");
                if (searchResult != null) {
                    JSONObject firstObject1 = searchResult.optJSONObject(0);
                    JSONArray itemArray2 = firstObject1.optJSONArray("item");
                    JSONObject firstObject2 = itemArray2.optJSONObject(0);
                    JSONArray sellingStatusArray = firstObject2.optJSONArray("sellingStatus");
                    JSONObject firstObject3 = sellingStatusArray.optJSONObject(0);
                    JSONArray currentPriceArray = firstObject3.optJSONArray("currentPrice");
                    JSONObject priceObject = currentPriceArray.optJSONObject(0);

                    price = priceObject.optString("__value__", "Product unavailable");
                }

            } catch (JSONException e) {

                e.printStackTrace();
            }
            return price;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null & s.length() > 0) {
                mEbayComparison.setText("Price of Ebay is set at: $" + s);
            } else {
                mEbayComparison.setText("Price of item at Ebay: Product Unavailable");
            }

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_review, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >3) {
                    getListItemData(newText);
                    return true;
                }
                return false;
            }
        });
        return true;
    }
    public class WalmartKeywordAsyncTask extends AsyncTask<String, Void, ArrayList<Walmart>> {
        String data = " ";
        String price;
        private ArrayList<Walmart> walmartList;

        public WalmartKeywordAsyncTask() {
            walmartList = new ArrayList<>();
        }

        @Override
        protected ArrayList<Walmart> doInBackground(String... query) {

            String searchKeyword =query[0];
            searchKeyword = searchKeyword.replace(' ','+');
            try {
                URL url = new URL(walmartLookupKeyword + searchKeyword+walmart_api_key);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                data = getInputData(inputStream);


            } catch (Throwable thr) {
                thr.fillInStackTrace();

            }
            try {
                JSONObject dataObject = new JSONObject(data);
                JSONArray priceArray = dataObject.optJSONArray("items");
                JSONObject item = priceArray.optJSONObject(0);
                Walmart walmart = new Walmart();
                price = item.optString("salePrice", "Product Unavailable");
                walmart.setmPrice(price);
                walmart.setmItemId(item.getInt("itemId"));
                walmartList.add(walmart);

            } catch (JSONException e) {

                e.printStackTrace();
            }
            return walmartList;
        }

        @Override
        protected void onPostExecute(ArrayList<Walmart> walmartArrayList) {
            mListFromUpc = walmartArrayList;
            mWalmartComparison.setText("Price at Walmart is: $" + mListFromUpc.get(0).getmPrice());
            new WalmartReviewAsyncTask().execute(mListFromUpc.get(0).getmItemId());
            mCard.setVisibility(View.VISIBLE);
            //new WalmartReviewAsyncTask().execute(44465724);
        }
    }
}
