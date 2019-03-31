package com.smartdeviceny.njts.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.smartdeviceny.njts.MainActivity;
import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.RouteListRecyclerViewActivity;
import com.smartdeviceny.njts.SystemService;
import com.smartdeviceny.njts.parser.DepartureVisionData;
import com.smartdeviceny.njts.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

class NJTScheduleViewHolder extends RecyclerView.ViewHolder {
    private NJTScheduleAdapter njtScheduleAdapter;
    View recyclerView;
    TextView tvRows;
    TextView njts_srv_blockID;
    TextView tv_njts_rvs_train_name;
    TextView tv_njts_dvs_departure;
    TextView tv_njts_dvs_arrival;
    TextView njts_intime_text;
    TextView tv_njts_rv_intime;
    View njts_rv_intime_layout;
    TextView njts_rvs_livestatus;



    TextView tv_njts_rv_track;
    View njts_rv_track_layout;

    TextView tv_njtsrv_status;
    View layout_njtsrv_track_background;

    View njtsrv_sidebar;
    CountDownTimer timer;
    ImageView ntsrv_fav_img;

    Context context;
    NJTScheduleData data;

    NJTScheduleViewHolder(NJTScheduleAdapter njtScheduleAdapter, Context context, View itemView) {
        super(itemView);
        this.njtScheduleAdapter = njtScheduleAdapter;
        recyclerView = itemView;
        this.context = context;
        njts_srv_blockID = itemView.findViewById(R.id.njts_srv_blockID);
        tv_njts_rvs_train_name = itemView.findViewById(R.id.tv_njts_rvs_train_name);
        tv_njts_dvs_departure = itemView.findViewById(R.id.tv_njts_dvs_departure);
        tv_njts_dvs_arrival = itemView.findViewById(R.id.tv_njts_dvs_arrival);
        tvRows = itemView.findViewById(R.id.tv_travel_sq_time);
        tv_njts_rv_intime = itemView.findViewById(R.id.tv_njts_rv_intime);
        njts_intime_text = itemView.findViewById(R.id.njts_intime_text);
        njts_rv_intime_layout = itemView.findViewById(R.id.njts_rv_intime_layout);
        njts_rvs_livestatus = itemView.findViewById(R.id.njts_rvs_livestatus);

        tv_njts_rv_track = itemView.findViewById(R.id.tv_njts_rv_track);
        njts_rv_track_layout = itemView.findViewById(R.id.njts_rv_track_layout);
        tv_njtsrv_status = itemView.findViewById(R.id.tv_njtsrv_status);
        layout_njtsrv_track_background = itemView.findViewById(R.id.layout_njtsrv_track_background);
        njtsrv_sidebar = itemView.findViewById(R.id.njtsrv_sidebar);
        ntsrv_fav_img = itemView.findViewById(R.id.ntsrv_fav_img);

    }

    public View getView() {
        return recyclerView;
    }
    public NJTScheduleData getData() {
        return data;
    }

    void bindData(int position) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
        recyclerView.startAnimation(animation);

        data = njtScheduleAdapter.getDataInPosition(position);
        DepartureVisionData dv = njtScheduleAdapter.departureVision.get(njtScheduleAdapter.make_key(data.rt.station_code, data.rt.block_id));
        boolean current_trains = false; // current train could
        try {
            if (dv != null) {
                Date tm = Utils.makeDate(Utils.getTodayYYYYMMDD(null), dv.time, "yyyyMMdd HH:mm"); // departure time has no am/pm

                long diff = data.rt.departure_time_as_date.getTime() - dv.adjusted_time.getTime();

                // this adjustment is needed because the time in the departure vision is not a 24 hr clock
//                if (data.rt.departure_time_as_date.getHours() > 12) {
//                    diff -= TimeUnit.HOURS.toMillis(12);
//                }
                if (Math.abs(diff) < TimeUnit.HOURS.toMillis(1)) {
                    current_trains = true;
                }

                Log.d("RECSAnew",
                        "dv update " + dv.track + " " + dv.block_id + " " + dv.time + " current:" + current_trains + " Route Departs:" + data.rt.departure_time + ", " + data.rt.departure_time_as_date + " DV Time:" + tm + " diff:" + TimeUnit.MICROSECONDS.toMinutes(
                                diff) + " (min)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date now = new Date();
        long diff = data.rt.arrival_time_as_date.getTime() - data.rt.departure_time_as_date.getTime();
        long indiff = data.rt.departure_time_as_date.getTime() - now.getTime();

        njts_srv_blockID.setText("#" + data.rt.block_id);
        tv_njts_rvs_train_name.setText(data.rt.route_name);
        tv_njts_dvs_departure.setText(Utils.formatPrintableTime(data.rt.departure_time_as_date, null));
        tv_njts_dvs_arrival.setText(Utils.formatPrintableTime(data.rt.arrival_time_as_date, null));
        tvRows.setText("" + TimeUnit.MILLISECONDS.toMinutes(diff));
        if (timer != null) {
            try {
                timer.cancel();
                timer = null;
            } catch (Exception e) {
                e.printStackTrace();
                ;
            }
            timer = null;
        }
        if (TimeUnit.MILLISECONDS.toMinutes(indiff) > -45 && TimeUnit.MILLISECONDS.toMinutes(indiff) < 120) {
            tv_njts_rv_intime.setText("" + Math.abs(TimeUnit.MILLISECONDS.toMinutes(indiff)));
            if(indiff>0) {
                tv_njts_rv_intime.setTextColor(njtScheduleAdapter.resid_round_white);
            } else {
                tv_njts_rv_intime.setTextColor(njtScheduleAdapter.resid_round_red);
            }

            njts_intime_text.setText(indiff>0?"in min": "ago");
            timer = new CountDownTimer(Math.abs(indiff), 30000) {

                public void onTick(long millisUntilFinished) {
                    long indiff = data.rt.departure_time_as_date.getTime() - now.getTime();
                    tv_njts_rv_intime.setText("" + Math.abs(TimeUnit.MILLISECONDS.toMinutes(indiff)));
                    njts_intime_text.setText(indiff>0?"in min": "ago");
                    if(indiff>0) {
                        tv_njts_rv_intime.setTextColor(njtScheduleAdapter.resid_round_white);
                    } else {
                        tv_njts_rv_intime.setTextColor(njtScheduleAdapter.resid_round_red);
                    }
                }

                public void onFinish() {
                    //tv_njts_rv_intime.setText;
                    njts_rv_intime_layout.setVisibility(View.INVISIBLE);
                }
            }.start();

            njts_rv_intime_layout.setVisibility(View.VISIBLE);
        } else {
            njts_rv_intime_layout.setVisibility(View.INVISIBLE);
        }

        String track = "";
        String status = "";
        String color = "";
        if (current_trains) {
            track = dv.track;
            status = dv.status;
            if (dv.background != null) {
                color = dv.background;
            }
            njts_rvs_livestatus.setVisibility(View.VISIBLE);
        } else {
            njts_rvs_livestatus.setVisibility(View.GONE);
        }
        if (track.isEmpty()) {
            njts_rv_track_layout.setVisibility(View.INVISIBLE);
            njtsrv_sidebar.setBackgroundColor(njtScheduleAdapter.blue_light);
        } else {
            njts_rv_track_layout.setVisibility(View.VISIBLE);
            tv_njts_rv_track.setText(track);
            njtsrv_sidebar.setBackgroundColor(njtScheduleAdapter.resid_round_green);
            if (indiff >= -TimeUnit.MINUTES.toMillis(5)) {
                layout_njtsrv_track_background.setBackgroundTintList(ColorStateList.valueOf(njtScheduleAdapter.resid_round_green));
            } else {
                layout_njtsrv_track_background.setBackgroundTintList(ColorStateList.valueOf(njtScheduleAdapter.resid_round_gray));
            }
        }
        if (status.isEmpty()) {
            tv_njtsrv_status.setVisibility(View.INVISIBLE);
        } else {
            tv_njtsrv_status.setVisibility(View.VISIBLE);
            tv_njtsrv_status.setText(status);
            if (status.toLowerCase().contains("cancel") || status.toLowerCase().contains("suspend") || status.toLowerCase().contains("delay")) {
                njtsrv_sidebar.setBackgroundColor(njtScheduleAdapter.resid_round_red);
            }
        }
        if (data.rt.favorite) {
            //itemView.setBackgroundResource(resid_selected);
            ntsrv_fav_img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_favorite_24px));
            //Glide.with(context).load(R.drawable.ic_baseline_favorite_24px).apply(new RequestOptions().fitCenter()).into( holder.img_fav);
        } else {
            //r.itemView.setBackgroundResource(resid_normal);
            ntsrv_fav_img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_outline_favorite_border_24px));
        }
        //
//            if(color.isEmpty()) {
//                njtsrv_sidebar.setBackgroundColor(resid_round_red);
//            } else {
//                njtsrv_sidebar.setBackgroundColor(Color.parseColor(RecyclerDepartureViewAdapter.getFromHtmlColor(color)));
//            }

        // tvRows.setText("saber" + position);
        //((ViewGroup) tvRows.getParent()).setBackgroundColor(Color.parseColor("#ffffff"));
    }

}
