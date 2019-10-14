package com.smartdeviceny.njts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.fragments.RecyclerDepartureViewHolder;
import com.smartdeviceny.njts.fragments.onMoveAndSwipedListener;
import com.smartdeviceny.njts.lib.RouteOperations;
import com.smartdeviceny.njts.lib.StickHeaderRecyclerView;


public class RecyclerDepartureWithHeaderViewAdapter extends StickHeaderRecyclerView<RecycleDepartureVisionData, RecycleDepartureVisionHeaderData> implements onMoveAndSwipedListener {

    private RouteOperations handler;
    private Context context;

    public RecyclerDepartureWithHeaderViewAdapter(Context context, RouteOperations handler) {
        this.handler = handler;
        this.context = context;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case RecycleDepartureVisionHeaderData.HEADER_TYPE:
                return new RecycleDepartureVisionHeaderViewHolder(this, LayoutInflater.from(parent.getContext()).inflate(R.layout.njts_header_item_1, parent, false));
            default:
                return new RecyclerDepartureViewHolder(this, parent.getContext(),
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false));

        }
    }

    @Override
    public void bindHeaderData(View header, int headerPosition) {
        RecycleDepartureVisionHeaderViewHolder h = new RecycleDepartureVisionHeaderViewHolder(this, header);
        h.bindData(headerPosition);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
        aa.setDuration(400);
        if (holder instanceof RecyclerDepartureViewHolder) {
            ((RecyclerDepartureViewHolder) holder).bindData(position);
        } else if (holder instanceof RecycleDepartureVisionHeaderViewHolder) {
            ((RecycleDepartureVisionHeaderViewHolder) holder).bindData(position);
        }
    }


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(final int position) {
    }

    public RouteOperations getRouteOperations() {
        return handler;
    }

    public Context getContext() {
        return context;
    }
}
