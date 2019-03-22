package com.smartdeviceny.njts.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartdeviceny.njts.R;

public class RecyclerAlertViewHolder extends RecyclerView.ViewHolder {
    public View mView;
    public RelativeLayout rela_round;
    public TextView tv_round_track;
    public TextView tv_recycler_item_1;
    public TextView tv_recycler_item_2;
    public TextView tv_recycler_item_3;
    public RelativeLayout card_view_backgroup_layout;

    RecyclerAlertViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        rela_round = itemView.findViewById(R.id.rela_round);
        tv_round_track = itemView.findViewById(R.id.tv_round_track);

        tv_recycler_item_1 = itemView.findViewById(R.id.tv_recycler_item_1);
        tv_recycler_item_2 = itemView.findViewById(R.id.tv_recycler_item_2);
        tv_recycler_item_3 = itemView.findViewById(R.id.tv_recycler_item_3);
        card_view_backgroup_layout = itemView.findViewById(R.id.card_view_backgroup_layout);

    }
}
