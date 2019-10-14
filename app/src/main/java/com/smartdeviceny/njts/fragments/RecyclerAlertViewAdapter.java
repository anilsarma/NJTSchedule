package com.smartdeviceny.njts.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.recyclerview.widget.RecyclerView;

import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.utils.RailAlertDetails;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class RecyclerAlertViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements onMoveAndSwipedListener {

    class StopHolder {
        public String item = "";
        public RailAlertDetails stop;
        public double miles = 0;

        public StopHolder(String item, RailAlertDetails stop) {
            this.stop = stop;
            this.item = item;
        }
    }

    ;
    private Context context;
    private List<StopHolder> mItems;
    private int color = 0;
    private View parentView;

    private final int TYPE_NORMAL = 1;
//    private final int TYPE_FOOTER = 2;
//    private final int TYPE_HEADER = 3;
//    private final String FOOTER = "footer";
//    private final String HEADER = "header";

    public RecyclerAlertViewAdapter(Context context) {
        this.context = context;
        mItems = new ArrayList();
    }

    public void setItems(List<RailAlertDetails> data) {
        this.mItems.clear();
        ArrayList<StopHolder> sh = new ArrayList<>();

        for (RailAlertDetails s : data) {
            StopHolder h = new StopHolder("", s);
            sh.add(h);
        }
        this.mItems.addAll(sh);
        notifyDataSetChanged();
    }

//    public void addItem(int position, RecycleDepartureVisionData insertData) {
//        StopHolder h = new StopHolder("", insertData);
//        mItems.add(position, h);
//        notifyItemInserted(position);
//    }

//    public void addItems(List<RecycleDepartureVisionData> data) {
//        StopHolder h = new StopHolder(HEADER, null);
//        mItems.add(h);
//
//        ArrayList<StopHolder> sh = new ArrayList<>();
//        for(RecycleDepartureVisionData s:data) {
//             h = new StopHolder("", s);
//
//            sh.add(h);
//        }
//        sh.addAll(sh);
//        notifyItemInserted(mItems.size() - 1);
//    }

//    public void addHeader() {
//        StopHolder h = new StopHolder(HEADER, null);
//        mItems.add(h);
//        notifyItemInserted(mItems.size() - 1);
//    }
//
//    public void addFooter() {
//        StopHolder h = new StopHolder(FOOTER, null);
//        mItems.add(h);
//        notifyItemInserted(mItems.size() - 1);
//    }

//    public void removeFooter() {
//        mItems.remove(mItems.size() - 1);
//        notifyItemRemoved(mItems.size());
//    }

    public void setColor(int color) {
        this.color = color;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        parentView = parent;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false);
        return new RecyclerDepartureViewHolder(null, null, view);
    }

    String getFromHtmlColor(String color) {
        switch (color.toLowerCase()) {
            case "cornflowerblue":
                return "#6495ed";
            case "white":
                return "#ffffff";
            case "red":
                return "#ff0000";
            case "blue":
                return "#0000ff";
            case "green":
                return "#008000";
            case "black":
                return "#000000";
            case "brown":
                return "#a52a2a";
            case "yellow":
                return "#ffff00";
        }
        return "#008000";
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        StopHolder stop = mItems.get(position);

        if (holder instanceof RecyclerDepartureViewHolder) {
            final RecyclerDepartureViewHolder recyclerViewHolder = (RecyclerDepartureViewHolder) holder;

            Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
            recyclerViewHolder.mView.startAnimation(animation);

            AlphaAnimation aa1 = new AlphaAnimation(1.0f, 0.1f);
            aa1.setDuration(400);
            recyclerViewHolder.rela_round.startAnimation(aa1);

            AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
            aa.setDuration(400);

            recyclerViewHolder.rela_round.setVisibility(View.INVISIBLE);
            recyclerViewHolder.rela_round.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.google_blue)));
            recyclerViewHolder.tv_dv_item_train_name.setText(stop.stop.getLong_name()); //stop.stop.station_code + " #" + stop.stop.block_id + " " + stop.stop.time );
            recyclerViewHolder.tv_dv_block_id.setText("");

            //recyclerViewHolder.tv_recycler_item_2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            recyclerViewHolder.tv_recycler_item_2.setText("");
            recyclerViewHolder.tv_recycler_item_2.setVisibility(View.GONE);
            recyclerViewHolder.tv_dv_time.setText(new SimpleDateFormat("dd MMM hh:mm:ss a").format(stop.stop.getTimeDate()));
            recyclerViewHolder.tv_dv_time.setTextColor(context.getResources().getColor(R.color.white));
            recyclerViewHolder.tv_recycler_item_2.setTypeface(Typeface.DEFAULT_BOLD);

            recyclerViewHolder.card_view_backgroup_layout.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.blue_jay)));
            recyclerViewHolder.mView.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.app_blue_dark)));

            recyclerViewHolder.tv_recycler_item_3.setText(stop.stop.getAlertText());
            recyclerViewHolder.tv_recycler_item_3.setTypeface(Typeface.DEFAULT );
            recyclerViewHolder.tv_recycler_item_3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            recyclerViewHolder.tv_recycler_item_3.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white)));

            if (stop.stop.getAlertText().isEmpty()) {
                recyclerViewHolder.tv_recycler_item_3.setVisibility(View.GONE);
            } else {
                recyclerViewHolder.tv_recycler_item_3.setVisibility(View.VISIBLE);
            }
            recyclerViewHolder.tv_recycler_item_4.setVisibility(View.GONE);
            //recyclerViewHolder.tv_recycler_item_4.setText("refreshed " + stop.stop.tableTime);
            recyclerViewHolder.tv_dv_item_train_name.setTypeface(Typeface.DEFAULT_BOLD);


//            Date now = new Date();
//            if( (now.getTime()-stop.stop.getTimeDate().getTime()) > TimeUnit.MINUTES.toMillis(1)) {
//                recyclerViewHolder.tv_dv_live.setBackground(context.getResources().getDrawable(R.drawable.stale_background));
//                recyclerViewHolder.tv_dv_live.setText("Stale");
//            } else {
//                recyclerViewHolder.tv_dv_live.setBackground(context.getResources().getDrawable(R.drawable.live_background));
//                recyclerViewHolder.tv_dv_live.setText("Live");
//            }
            recyclerViewHolder.tv_dv_live.startAnimation(AnimationUtils.loadAnimation(parentView.getContext(), R.anim.bounce));
            recyclerViewHolder.rela_round.startAnimation(aa);
            recyclerViewHolder.tv_round_track.setText("");

            recyclerViewHolder.rela_round.setVisibility(View.GONE);

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
        }
    }

    @Override
    public int getItemViewType(int position) {
        StopHolder s = mItems.get(position);
        switch (s.item) {
//            case HEADER:
//                return TYPE_HEADER;
//            case FOOTER:
//                return TYPE_FOOTER;
            default:
                return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        //Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(final int position) {
        // mItems.remove(position);
        //notifyItemRemoved(position);
        this.notifyDataSetChanged();

//        Snackbar.make(parentView, context.getString(R.string.item_swipe_dismissed), Snackbar.LENGTH_SHORT)
//                .setAction(context.getString(R.string.item_swipe_undo), new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        addItem(position, mItems.get(position));
//                    }
//                }).show();
    }


//    private class FooterViewHolder extends RecyclerView.ViewHolder {
//        private ProgressBar progress_bar_load_more;
//
//        private FooterViewHolder(View itemView) {
//            super(itemView);
//            progress_bar_load_more = itemView.findViewById(R.id.progress_bar_load_more);
//        }
//    }
//
//    private class HeaderViewHolder extends RecyclerView.ViewHolder {
//        private TextView header_text;
//
//        private HeaderViewHolder(View itemView) {
//            super(itemView);
//            header_text = itemView.findViewById(R.id.header_text);
//        }
//    }

}
