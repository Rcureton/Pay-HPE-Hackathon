package com.adi.ho.jackie.pricecomparisonapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Todo on 3/13/2016.
 */
public class ReviewRecyclerViewAdapter extends RecyclerView.Adapter<ReviewRecyclerViewAdapter.ViewHolder> {
    ArrayList<String> mListOfReviews;

    public ReviewRecyclerViewAdapter(ArrayList<String> array) {
        mListOfReviews = array;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView vCard;

        public ViewHolder(View itemView) {
            super(itemView);
            vCard = (TextView)itemView.findViewById(R.id.reviewText);
        }

    }

    @Override
    public ReviewRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.reviewcard, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ReviewRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.vCard.setText(mListOfReviews.get(position));
    }

    @Override
    public int getItemCount() {
        return mListOfReviews.size();
    }
}
