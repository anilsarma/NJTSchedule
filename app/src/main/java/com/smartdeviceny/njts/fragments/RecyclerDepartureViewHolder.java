package com.smartdeviceny.njts.fragments;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.smartdeviceny.njts.R;

public class RecyclerDepartureViewHolder extends RecyclerView.ViewHolder {
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

    public RecyclerDepartureViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        rela_round = itemView.findViewById(R.id.rela_round);
        tv_round_track = itemView.findViewById(R.id.tv_round_track);

        tv_dv_item_train_name = itemView.findViewById(R.id.tv_dv_item_train_name);
        tv_recycler_item_2 = itemView.findViewById(R.id.tv_recycler_item_2);
        tv_recycler_item_3 = itemView.findViewById(R.id.tv_recycler_item_3);
        tv_recycler_item_4 = itemView.findViewById(R.id.tv_recycler_item_4);
        tv_dv_block_id = itemView.findViewById(R.id.tv_dv_block_id);
        tv_dv_time = itemView.findViewById(R.id.tv_dv_time);
        tv_dv_live = itemView.findViewById(R.id.tv_dv_live);
        card_view_backgroup_layout = itemView.findViewById(R.id.card_view_backgroup_layout);
        card_view_item_recycler_viev = itemView.findViewById(R.id.card_view_item_recycler_viev);
    }
}
