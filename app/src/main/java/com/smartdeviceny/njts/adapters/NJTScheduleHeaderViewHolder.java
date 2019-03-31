package com.smartdeviceny.njts.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.smartdeviceny.njts.R;

class NJTScheduleHeaderViewHolder extends RecyclerView.ViewHolder {

    private NJTScheduleAdapter njtScheduleAdapter;
    TextView njts_rvs_header_route;
    TextView njts_rvs_header_title;


    public NJTScheduleHeaderViewHolder(NJTScheduleAdapter njtScheduleAdapter, View itemView) {
        super(itemView);
        this.njtScheduleAdapter = njtScheduleAdapter;
        njts_rvs_header_route = itemView.findViewById(R.id.njts_rvs_header_route);
        njts_rvs_header_title = itemView.findViewById(R.id.njts_rvs_header_title);
    }

    public void bindData(int position) {
        NJTScheduleHeader data = njtScheduleAdapter.getHeaderDataInPosition(position);
        njts_rvs_header_route.setText(data.getRouteName());
        njts_rvs_header_title.setText(data.getTitle());
    }
}
