package com.adi.ho.jackie.pricecomparisonapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ReviewActivity extends AppCompatActivity {

    private ActionBar mActionBar;
    private ImageView mImageView;
    private ArrayList<String> mReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        mImageView = (ImageView)findViewById(R.id.background);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mReviews = getIntent().getStringArrayListExtra(MainActivity.REVIEW_ARRAY_KEY);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setTitle("Product Review");

        Picasso.with(this)
                .load(R.drawable.cafe)
                .fit().centerCrop()
                .into(mImageView);
    }
}
