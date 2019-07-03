package com.smartdeviceny.njts.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.smartdeviceny.njts.MainActivity;
import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.RouteListRecyclerViewActivity;
import com.smartdeviceny.njts.SystemService;
import com.smartdeviceny.njts.fragments.onMoveAndSwipedListener;
import com.smartdeviceny.njts.lib.StickHeaderRecyclerView;
import com.smartdeviceny.njts.parser.DepartureVisionData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;


public class NJTScheduleAdapter extends StickHeaderRecyclerView<NJTScheduleData, NJTScheduleHeader> implements onMoveAndSwipedListener {
   // FragmentActivity activity;
    Context context;
    public HashMap<String, DepartureVisionData> departureVision = new HashMap<>();
    int resid_round_white;
    int resid_round_green;
    int resid_round_red;
    int resid_round_gray;
    int blue_light;

    public NJTScheduleAdapter(FragmentActivity activity, Context context) {
        this.context = context;
        Resources resources = context.getApplicationContext().getResources();
        resid_round_white = context.getResources().getColor(R.color.white);
        resid_round_green = context.getResources().getColor(R.color.google_green);
        resid_round_red = context.getResources().getColor(R.color.app_red);
        resid_round_gray = context.getResources().getColor(R.color.gray_light);
        blue_light = context.getResources().getColor(R.color.blue_light);
    }



    MainActivity getMainActivity() {
        if(context==null) {
            return null;
        }
        if( MainActivity.class.isAssignableFrom(context.getClass())) {
            return (MainActivity) context;
        }
        if( MainActivity.class.isAssignableFrom(context.getApplicationContext().getClass())) {
            return (MainActivity) context.getApplicationContext();
        }
        return null;
    }
    String make_key(String station, String block_id) {
        return station + "::" + block_id;
    }

    public void updateDepartureVision(@Nullable HashMap<String, DepartureVisionData> departureVision) {
        HashMap<String, DepartureVisionData> data = new HashMap<>();
        for (DepartureVisionData dv : departureVision.values()) {
            data.put(make_key(dv.station_code, dv.block_id), dv);
        }
        this.departureVision = data;
        Log.d("NJTSnew", "DV Size:" + departureVision.size());
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case NJTScheduleHeader.HEADER_TYPE_1:
                return new NJTScheduleHeaderViewHolder(this, LayoutInflater.from(parent.getContext()).inflate(R.layout.njts_header_item_1, parent, false));
            default:
                NJTScheduleViewHolder holder= new NJTScheduleViewHolder(this, parent.getContext(),
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.njts_item_recycler_schedule_view, parent, false));
                holder.getView().setOnClickListener(view13 -> showDetailDailog(holder));
                holder.ntsrv_fav_img.setOnClickListener(view13 -> handleFavoriteClick(view13, holder));
                return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
        aa.setDuration(400);
        if (holder instanceof NJTScheduleViewHolder) {
            ((NJTScheduleViewHolder) holder).bindData(position);

        } else if (holder instanceof NJTScheduleHeaderViewHolder) {
            ((NJTScheduleHeaderViewHolder) holder).bindData(position);
        }
    }

    @Override
    public void bindHeaderData(View header, int headerPosition) {
        NJTScheduleHeaderViewHolder h = new NJTScheduleHeaderViewHolder(this, header);
        h.bindData(headerPosition);
    }

    //    class Header2ViewHolder extends RecyclerView.ViewHolder {
//        TextView tvHeader;
//
//        Header2ViewHolder(View itemView) {
//            super(itemView);
//            tvHeader = itemView.findViewById(R.id.tvHeader);
//        }
//
//        void bindData(int position) {
//            tvHeader.setText(String.valueOf(position / 5));
//        }
//    }
    void showDetailDailog(NJTScheduleViewHolder holder) {

        if( holder.getData()==null|| context==null) {
            return;
        }
        Intent intent = new Intent(context.getApplicationContext(), RouteListRecyclerViewActivity.class);
        //intent.setClass(context, RouteListRecyclerViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("trip_id", holder.getData().rt.trip_id);
        bundle.putString("from", holder.getData().rt.from);
        bundle.putString("to", holder.getData().rt.to);
        bundle.putString("departure_time", holder.getData().rt.departure_time);
        bundle.putString("block_id", holder.getData().rt.block_id);
        bundle.putString("route_name", holder.getData().rt.route_name);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtras( bundle);
        context.getApplicationContext().startActivity(intent);
        if(true) {
            return ;
        }
        if( getMainActivity()==null) {
            return;
        }
        SystemService systemService = getMainActivity().systemService;
        if (systemService != null && holder.getData().rt != null) {
            ArrayList<HashMap<String, Object>> stops = systemService.getTripStops(holder.getData().rt.trip_id);
            ArrayList<HashMap<String, Object>> tmp = new ArrayList<>();
            HashMap<String, Object> header = new HashMap<>();
            header.put("arrival_time", "");
            SimpleDateFormat printFormat = new SimpleDateFormat("hh:mm a");
            header.put("stop_name", "#" + holder.getData().rt.block_id + " " + printFormat.format(holder.getData().rt.departure_time_as_date));
            tmp.add(header);
            for (HashMap<String, Object> o : stops) {
                tmp.add(o);
            }
            stops = tmp;
            //for(HashMap<String, Object> e:stops) {
            //    Log.d("REC", " " + Utils.capitalize(e.get("stop_name").toString()) + " " + e.get("arrival_time") + " " + e.get("departure_time"));
            //}
            //}
            //((MainActivity) mInflater.getContext()).getLayoutInflater().inflate()
            //TableLayout stopLayout = (TableLayout)((MainActivity) mInflater.getContext()).getLayoutInflater().inflate(R.layout.stop_entry_layout, null);
            ListView stopLayout = (ListView) getMainActivity().getLayoutInflater().inflate(R.layout.stop_entry_layout, null);

            Object data[] = stops.toArray();
            StopViewAdaptor adapter = new StopViewAdaptor(context, data);

            stopLayout.setAdapter(adapter);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);

            builder.setView(stopLayout);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    void handleFavoriteClick(View view, NJTScheduleViewHolder holder) {
        if (holder.getData().rt.favorite) {
            holder.getData().rt.favorite = false;
        } else {
            holder.getData().rt.favorite = true;
        }
        getMainActivity().updateFavorite(holder.getData().rt.favorite, holder.getData().rt.block_id);
//        if (holder.getData().rt.favorite) {
//            holder.itemView.setBackgroundResource(resid_selected);
//        } else {
//            holder.itemView.setBackgroundResource(resid_normal);
//        }

        holder.ntsrv_fav_img.setImageDrawable(
                context.getResources().getDrawable(holder.getData().rt.favorite ? R.drawable.ic_baseline_favorite_24px : R.drawable.ic_outline_favorite_border_24px));
        Snackbar.make(view,
                holder.getData().rt.favorite ? "Added #" + holder.getData().rt.block_id + " to favorites" : "Removed #" + holder.getData().rt.block_id + " from favorites", Snackbar.LENGTH_SHORT).show();

    }
}