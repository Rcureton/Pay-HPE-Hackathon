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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.Map;

import hod.api.hodclient.HODApps;
import hod.api.hodclient.HODClient;
import hod.api.hodclient.IHODClientCallback;
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
    public String mUPCofProduct;
    public ArrayList<String> mReviews;
    private ArrayList<Walmart> mListFromUpc;
    private static String walmartLookupUpc = "http://api.walmartlabs.com/v1/items?apiKey=jcpk6chshjwn5nbq2khnrvm9&upc=";
    private static String walmartReviewById1 = "http://api.walmartlabs.com/v1/reviews/";
    private static String walmartReviewById2 = "?format=json&apiKey=jcpk6chshjwn5nbq2khnrvm9";
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
        setSupportActionBar(mToolbar);
        mActionbar = getSupportActionBar();
        mActionbar.setTitle("Jackie's Price Hooo");
        mActionbar.setHomeButtonEnabled(true);
        mReviews = new ArrayList<>();
        mListFromUpc = new ArrayList<>();
        CreateLocalImageFolder();
    }

    @Override
    public void requestCompletedWithContent(String response) {
        if (hodApp.equals(HODApps.RECOGNIZE_BARCODES)) {

            Log.i("Request completed", response);
            BarcodeRecognitionResponse resp = parser.ParseBarcodeRecognitionResponse(response);

            if (resp != null) {
                mUPCofProduct = resp.barcode.get(0).text.substring(1);
                Log.i("Response", "UPC IS: " + mUPCofProduct);

                String walmartsPrice = "Product Unavailable";

                new WalmartAsyncTask().execute(mUPCofProduct);

            }
        } else if (hodApp.equals(HODApps.ANALYZE_SENTIMENT)){
            Log.i("Request completed", response);
            SentimentAnalysisResponse resp = parser.ParseSentimentAnalysisResponse(response);
            if (resp != null){
                if (resp.aggregate.score > 0.2) {
                    mWalmartComparison.setTextColor(Color.GREEN);
                } else if (resp.aggregate.score<=0.2 && resp.aggregate.score>=-0.2){
                    mWalmartComparison.setTextColor(Color.GRAY);
                } else {
                    mWalmartComparison.setTextColor(Color.RED);
                }
            }

            mButton.setVisibility(View.VISIBLE);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ReviewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(REVIEW_ARRAY_KEY, mReviews);
                    startActivity(intent);
                }
            });
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
           // new WalmartReviewAsyncTask().execute(mListFromUpc.get(0).getmItemId());
           new WalmartReviewAsyncTask().execute(44465724);
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
}
