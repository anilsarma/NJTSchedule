package com.smartdeviceny.njts;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartdeviceny.njts.fragments.RecyclerDepartureViewHolder;
import com.smartdeviceny.njts.utils.StopDetails;
import com.smartdeviceny.njts.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RouteListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<RouteListRecyclerData> mItems;
    private int color = 0;
    private View parentView;
    String current_header = "N/A";

    public static final int TYPE_NORMAL = 1;
    //    public static final int TYPE_FOOTER = 2;
    public static final int TYPE_HEADER = 3;
//    private final String FOOTER = "footer";
//    private final String HEADER = "header";


    public RouteListViewAdapter(Context context) {
        this.context = context;
        mItems = new ArrayList();
    }

    public void setItems(List<StopDetails> data) {
        for (StopDetails str : data) {
            this.mItems.add(new RouteListRecyclerData(str));
        }
        notifyDataSetChanged();
    }

    public RouteListRecyclerData getItem(int position) {
        return mItems.get(position);
    }

    public void addItem(int position, StopDetails insertData) {
        mItems.add(position, new RouteListRecyclerData(insertData));
        notifyItemInserted(position);
    }

//    public void addItem(int position, RouteListRecyclerData insertData) {
//        mItems.add(position, insertData);
//        notifyItemInserted(position);
//    }
//
//    public void addItems(List<StopDetails> data) {
//        //current_header = new Date().toString();
//        addHeader();
//        for (StopDetails str : data) {
//            this.mItems.add(new RouteListRecyclerData(str, str));
//        }
//        notifyItemInserted(mItems.size() - 1);
//    }

    public void addHeader(String header, String route_name, String block_id) {
        StopDetails d = new StopDetails(header, "B", "C");
        d.route_name = route_name;
        d.block_id = block_id;
        d.stop_name = header;
        this.mItems.add(new RouteListRecyclerData(d));
        current_header = header;
    }

//    public void addFooter() {
////        mItems.add(FOOTER);
////        notifyItemInserted(mItems.size() - 1);
//    }
//
//    public void removeFooter() {
////        mItems.remove(mItems.size() - 1);
////        notifyItemRemoved(mItems.size());
//    }

    public void setColor(int color) {
        this.color = color;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        parentView = parent;
        if (viewType == TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false);
            return new RecyclerDepartureViewHolder(null, null, view);
         }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_header, parent, false);
          return new HeaderViewHolder(view);

//        else if (viewType == TYPE_FOOTER) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_footer, parent, false);
//            return new FooterViewHolder(view);
//        } else {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_header, parent, false);
//            return new HeaderViewHolder(view);
//        }v
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
//        if (holder instanceof HeaderViewHolder) {
//            final HeaderViewHolder recyclerViewHolder = (HeaderViewHolder) holder;
//            recyclerViewHolder.header_text.setText("Footer " + position);
//        }
        if (holder instanceof RecyclerDepartureViewHolder) {
            final RecyclerDepartureViewHolder recyclerViewHolder = (RecyclerDepartureViewHolder) holder;

            Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
            recyclerViewHolder.mView.startAnimation(animation);

            AlphaAnimation aa1 = new AlphaAnimation(1.0f, 0.1f);
            aa1.setDuration(400);
            recyclerViewHolder.rela_round.startAnimation(aa1);

            AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
            aa.setDuration(400);

//            if (color == 1) {
//                recyclerViewHolder.rela_round.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.google_blue)));
//            } else if (color == 2) {
//                recyclerViewHolder.rela_round.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.google_green)));
//            } else if (color == 3) {
//                recyclerViewHolder.rela_round.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.google_yellow)));
//            } else if (color == 4) {
//                recyclerViewHolder.rela_round.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.google_red)));
//            } else {
//                recyclerViewHolder.rela_round.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.gray)));
//            }
            RouteListRecyclerData details = mItems.get(position);
            recyclerViewHolder.rela_round.startAnimation(aa);
            String stop_name = Utils.capitalize(details.data.getStop_name().trim());

            recyclerViewHolder.tv_dv_item_train_name.setText( stop_name.trim() );
            recyclerViewHolder.tv_dv_item_train_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);

            recyclerViewHolder.tv_dv_time.setText(details.data.getPrintableArrivalTime());
            //recyclerViewHolder.tv_dv_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            recyclerViewHolder.tv_dv_time.setVisibility(View.GONE);
            recyclerViewHolder.tv_dv_block_id.setText("");
            recyclerViewHolder.tv_dv_block_id.setVisibility(View.VISIBLE);
            String msg = "\t" + details.data.getPrintableArrivalTime();
            recyclerViewHolder.tv_recycler_item_2.setText(msg);
            //recyclerViewHolder.tv_recycler_item_2.setVisibility(View.GONE);
            //recyclerViewHolder.tv_recycler_item_3.setText("\tDeparts " + details.data.getPrintableDepartureTime());
            recyclerViewHolder.tv_recycler_item_3.setVisibility(View.GONE);
            recyclerViewHolder.tv_dv_live.setVisibility(View.GONE);
            recyclerViewHolder.tv_recycler_item_4.setVisibility(View.GONE);
            recyclerViewHolder.rela_round.setVisibility(View.GONE);
             {
                if( details.data.stop_name.toLowerCase().trim().equals(details.data.from.toLowerCase()) ) {
                    recyclerViewHolder.rela_round.setVisibility(View.VISIBLE);
                    recyclerViewHolder.tv_round_track.setVisibility(View.VISIBLE);
                    recyclerViewHolder.tv_round_track.setText("B");
                    recyclerViewHolder.rela_round.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.google_yellow)));
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        recyclerViewHolder.tv_round_track.setTooltipText("Start Station");
                    }
                }
                if( details.data.stop_name.toLowerCase().trim().equals(details.data.to.toLowerCase()) ) {
                    recyclerViewHolder.rela_round.setVisibility(View.VISIBLE);
                    recyclerViewHolder.tv_round_track.setVisibility(View.VISIBLE);
                    recyclerViewHolder.tv_round_track.setText("E");
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        recyclerViewHolder.tv_round_track.setTooltipText("Destination Station");
                    }
                    recyclerViewHolder.rela_round.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.google_red)));
                }
            }
            recyclerViewHolder.tv_dv_block_id.setTextColor(context.getResources().getColor(R.color.black));
            recyclerViewHolder.tv_recycler_item_2.setTextColor(context.getResources().getColor(R.color.black));
            recyclerViewHolder.tv_recycler_item_3.setTextColor(context.getResources().getColor(R.color.black));
            recyclerViewHolder.card_view_backgroup_layout.setBackgroundColor(context.getResources().getColor(R.color.white));
            recyclerViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent(context, ShareViewActivity.class);
//                    intent.putExtra("color", color);
//                    context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation
//                            ((Activity) context, recyclerViewHolder.rela_round, "shareView").toBundle());
                }
            });
        } else { // mus tbe header ...
            RouteListRecyclerData details = mItems.get(position);
            final HeaderViewHolder recyclerViewHolder = (HeaderViewHolder)holder;
            recyclerViewHolder.header_text.setText(current_header);
            recyclerViewHolder.textViewHeaderLine2.setText(details.data.route_name + " #" + details.data.block_id);
           // recyclerViewHolder.header_text.setTextColor(context.getResources().getColor(R.color.black));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if( position ==0) {
            return TYPE_HEADER;
        }
        return TYPE_NORMAL;
//        if( position >= mItems.size()) {
//            return TYPE_FOOTER;
//        }
//        if( position <0) {
//            return TYPE_FOOTER;// nothing here.
//        }
//        String s = mItems.get(position);
//        switch (s) {
//            case HEADER:
//                return TYPE_HEADER;
//            case FOOTER:
//                return TYPE_FOOTER;
//            default:
//                return TYPE_NORMAL;
//        }
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }


//    @Override
//    public boolean onItemMove(int fromPosition, int toPosition) {
//        Collections.swap(mItems, fromPosition, toPosition);
//        notifyItemMoved(fromPosition, toPosition);
//        return true;
//    }

//    @Override
//    public void onItemDismiss(final int position) {
//        mItems.remove(position);
//        notifyItemRemoved(position);
//
//        Snackbar.make(parentView, context.getString(R.string.item_swipe_dismissed), Snackbar.LENGTH_SHORT)
//                .setAction(context.getString(R.string.item_swipe_undo), new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        addItem(position, mItems.get(position));
//                    }
//                }).show();
//    }




    private class FooterViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar progress_bar_load_more;

        private FooterViewHolder(View itemView) {
            super(itemView);
            progress_bar_load_more = itemView.findViewById(R.id.progress_bar_load_more);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView header_text;
        private TextView textViewHeaderLine2;

        private HeaderViewHolder(View itemView) {
            super(itemView);
            header_text = itemView.findViewById(R.id.header_text);
            textViewHeaderLine2 = itemView.findViewById(R.id.textViewHeaderLine2);
        }
    }

}
