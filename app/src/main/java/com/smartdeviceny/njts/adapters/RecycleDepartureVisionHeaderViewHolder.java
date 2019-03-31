package com.smartdeviceny.njts.adapters;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartdeviceny.njts.R;

class RecycleDepartureVisionHeaderViewHolder extends RecyclerView.ViewHolder {

    private RecyclerDepartureWithHeaderViewAdapter adapter;
    TextView njts_rvs_header_route;
    TextView njts_rvs_header_title;
    TextView njts_rvs_header_subtitle;
    ImageView njts_rvs_refresh;



    public RecycleDepartureVisionHeaderViewHolder(RecyclerDepartureWithHeaderViewAdapter adapter, View itemView) {
        super(itemView);
        this.adapter = adapter;
        njts_rvs_header_route = itemView.findViewById(R.id.njts_rvs_header_route);
        njts_rvs_header_title = itemView.findViewById(R.id.njts_rvs_header_title);
        njts_rvs_header_subtitle = itemView.findViewById(R.id.njts_rvs_header_subtitle);
        njts_rvs_refresh = itemView.findViewById(R.id.njts_rvs_toggle);
    }


    public void bindData(int position) {
        RecycleDepartureVisionHeaderData data = adapter.getHeaderDataInPosition(position);
        njts_rvs_header_route.setText(data.title);
        njts_rvs_header_route.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        if( data.subtitle.isEmpty()) {
            njts_rvs_header_title.setVisibility(View.GONE);
        } else {
            njts_rvs_header_title.setVisibility(View.VISIBLE);
            njts_rvs_header_title.setText(data.subtitle);
        }

        //njts_rvs_header_route.setVisibility(View.GONE);
        //njts_rvs_header_title.setText(data.getTitle());
        njts_rvs_refresh.setSoundEffectsEnabled(true);
        njts_rvs_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animY = ObjectAnimator.ofFloat(v, "translationY", -100f, 0f);
                animY.setDuration(1000);//1sec
                animY.setInterpolator(new BounceInterpolator());
                animY.setRepeatCount(5);
                animY.start();
                adapter.getRouteOperations().toggleDestinations();
            }
        });
    }
}
