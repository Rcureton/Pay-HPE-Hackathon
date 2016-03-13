package com.adi.ho.jackie.pricecomparisonapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ReviewActivity extends AppCompatActivity {

    private ActionBar mActionBar;
    private ImageView mImageView;
    private ArrayList<String> mReviews;
    private RecyclerView mRecyclerView;
    public RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

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
        mActionBar.setTitle("Product Reviews");

        Picasso.with(this)
                .load(R.drawable.cafe)
                .fit().centerCrop()
                .into(mImageView);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ReviewRecyclerViewAdapter(mReviews);
        mRecyclerView.setAdapter(mAdapter);
    }
}
