package com.aka.assignment.adapter;

import android.content.Context;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aka.assignment.R;
import com.aka.assignment.model.Captures;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by akshayaggarwal99 on 16-02-2017.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context mContext;
    private List<Captures> capturesList;
    private DatabaseReference mDatabaseReference;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
        }
    }


    public MyAdapter(Context mContext, List<Captures> capturesList) {
        this.mContext = mContext;
        this.capturesList = capturesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("images");

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyAdapter.MyViewHolder holder, final int position) {

        String captures = capturesList.get(position).getThumbnail();
        // loading Captures cover using Glide library
//        Glide.with(mContext).load(captures).into(holder.thumbnail);
        Picasso.with(mContext)
                .load(captures)
                .into(holder.thumbnail);
        holder.title.setText(capturesList.get(position).getName());
        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(holder.overflow,position);
            }
        });
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopup(View view, final int position) {
        Context wrapper = new ContextThemeWrapper(view.getContext(), R.style.PopupMenu);
        final PopupMenu popup = new PopupMenu(wrapper, view);
        popup.getMenuInflater().inflate(R.menu.menu_item, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    mDatabaseReference.child(capturesList.get(position).getKey()).setValue(null);
                }
                popup.dismiss();
                return true;
            }
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return capturesList.size();
    }
}
