package com.smartdeviceny.njts.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.SystemService;
import com.smartdeviceny.njts.adapters.RecycleDepartureVisionData;
import com.smartdeviceny.njts.adapters.RecyclerDepartureWithHeaderViewAdapter;
import com.smartdeviceny.njts.parser.DepartureVisionData;
import com.smartdeviceny.njts.parser.Route;
import com.smartdeviceny.njts.utils.ConfigUtils;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RecyclerDepartureViewHolder extends RecyclerView.ViewHolder {
    public Context context;
    public RecyclerDepartureWithHeaderViewAdapter adapter;
    public View mView;
    public ConstraintLayout rela_round;
    public TextView tv_round_track;
    public TextView tv_dv_item_train_name;
    public TextView tv_recycler_item_2;
    public TextView tv_recycler_item_3;
    public TextView tv_recycler_item_4;
    public TextView tv_dv_block_id;
    public TextView tv_dv_time;
    public TextView tv_dv_live;
    public CardView card_view_item_recycler_viev;

    public ConstraintLayout card_view_backgroup_layout;

    public RecyclerDepartureViewHolder(RecyclerDepartureWithHeaderViewAdapter adapter, Context context, View itemView) {
        super(itemView);
        this.adapter = adapter;
        this.context = context;
        mView = itemView;
        rela_round = itemView.findViewById(R.id.layout_njtsrv_track_background);
        tv_round_track = itemView.findViewById(R.id.tv_round_track);

        tv_dv_item_train_name = itemView.findViewById(R.id.tv_njts_rvs_train_name);
        tv_recycler_item_2 = itemView.findViewById(R.id.tv_njtsrv_status);
        tv_recycler_item_3 = itemView.findViewById(R.id.tv_recycler_item_3);
        tv_recycler_item_4 = itemView.findViewById(R.id.tv_recycler_item_4);
        tv_dv_block_id = itemView.findViewById(R.id.tv_dv_block_id);
        tv_dv_time = itemView.findViewById(R.id.tv_njts_dvs_departure);
        tv_dv_live = itemView.findViewById(R.id.tv_dv_live);
        card_view_backgroup_layout = itemView.findViewById(R.id.card_view_backgroup_layout);
        card_view_item_recycler_viev = itemView.findViewById(R.id.card_view_item_recycler_viev);
    }


    public void bindData(int position) {
        if(adapter==null) {
            return;
        }
        RecycleDepartureVisionData stop = adapter.getDataInPosition(position);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
        final RecyclerDepartureViewHolder recyclerViewHolder = this;
        recyclerViewHolder.mView.startAnimation(animation);

        AlphaAnimation aa1 = new AlphaAnimation(1.0f, 0.1f);
        aa1.setDuration(400);
        if(rela_round!=null) {
            rela_round.startAnimation(aa1);
        }

        AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
        aa.setDuration(400);

        if( rela_round != null) {
            rela_round.setVisibility(View.INVISIBLE);
            rela_round.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.google_blue)));
        } else {
            tv_round_track.setVisibility(View.INVISIBLE);
        }
        tv_dv_item_train_name.setText(stop.stop.line); //stop.stop.station_code + " #" + stop.stop.block_id + " " + stop.stop.time );
        tv_dv_block_id.setText("#" + stop.stop.block_id + " Departs ");
        tv_dv_time.setText(stop.stop.time);

        tv_recycler_item_2.setText(stop.stop.station_long_name + " \u279F " + stop.stop.to);
        tv_recycler_item_3.setText(stop.stop.status);
        if (stop.stop.status.isEmpty()) {
            tv_recycler_item_3.setVisibility(View.GONE);
        } else {
            tv_recycler_item_3.setVisibility(View.VISIBLE);
        }
        tv_recycler_item_4.setVisibility(View.GONE);
        tv_recycler_item_4.setText("refreshed " + stop.stop.tableTime);
        tv_dv_item_train_name.setTypeface(Typeface.DEFAULT_BOLD);
        String status = stop.stop.status.toLowerCase();
        if (status.contains("cancel") || status.contains("delay")) {
            tv_dv_live.setText("Alert");
            tv_dv_live.setBackground(context.getResources().getDrawable(R.drawable.alert_background));

        } else {
            tv_dv_live.setText("Live");
            tv_dv_live.setBackground(context.getResources().getDrawable(R.drawable.live_background));
        }

        Date now = new Date();
        if( (now.getTime()-stop.stop.getHackCreateTime().getTime()) > TimeUnit.MINUTES.toMillis(5)) {
            tv_dv_live.setBackground(context.getResources().getDrawable(R.drawable.stale_background));
            tv_dv_live.setText("Stale");
        } else {
            tv_dv_live.setBackground(context.getResources().getDrawable(R.drawable.live_background));
            tv_dv_live.setText("Live");
        }

        tv_dv_live.startAnimation(AnimationUtils.loadAnimation(context, R.anim.bounce));

        if(rela_round!=null) {
            rela_round.startAnimation(aa);
        } else {
            tv_round_track.startAnimation(aa);
        }
        tv_round_track.setText(stop.stop.track);
        if (!stop.stop.track.isEmpty()) {
            if(rela_round!=null) {
                rela_round.setVisibility(View.VISIBLE);
            } else {
                tv_round_track.setVisibility(View.VISIBLE);
            }
        }
        card_view_backgroup_layout.setBackgroundColor(Color.parseColor(Utils.getFromHtmlColor(stop.stop.background)));
//            if( stop.stop.background.toLowerCase().equals("yellow")) {
//                tv_dv_item_train_name.setTextColor(Color.BLACK);
//                tv_recycler_item_2.setTextColor(Color.BLACK);
//                tv_recycler_item_3.setTextColor(Color.BLACK);
//                //tv_recycler_item_4.setTextColor(Color.BLACK);
//            } else {
//                tv_dv_item_train_name.setTextColor(Color.WHITE);
//                tv_recycler_item_2.setTextColor(Color.WHITE);
//                tv_recycler_item_3.setTextColor(Color.WHITE);
//               // tv_recycler_item_4.setTextColor(Color.WHITE);
//            }
        int color = Color.parseColor(Utils.getFromHtmlColor(stop.stop.foreground));
        if (stop.stop.background.toLowerCase().equals("yellow")) {
            tv_dv_item_train_name.setTextColor(Color.BLACK);
        } else {
            tv_dv_item_train_name.setTextColor(Color.parseColor(Utils.getFromHtmlColor(stop.stop.background)));
        }
        tv_recycler_item_2.setTextColor(color);
        tv_recycler_item_3.setTextColor(color);
        tv_dv_block_id.setTextColor(color);
        tv_dv_time.setTextColor(color);
        // tv_recycler_item_4.setTextColor(color);
    }



}
