package com.smartdeviceny.njts.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartdeviceny.njts.MainActivity;
import com.smartdeviceny.njts.NJTAlertJobService;
import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.SystemService;
import com.smartdeviceny.njts.adapters.ServiceConnected;
import com.smartdeviceny.njts.annotations.JSONObjectSerializer;
import com.smartdeviceny.njts.lib.ExpandableFabButtons;
import com.smartdeviceny.njts.utils.ConfigUtils;
import com.smartdeviceny.njts.utils.JobID;
import com.smartdeviceny.njts.utils.RailAlertDetails;
import com.smartdeviceny.njts.utils.RailDetailsContainer;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentAlertViewNewOne extends Fragment implements ServiceConnected {
    private List<RailAlertDetails> data;
    ExpandableFabButtons fbExpand;
    private FloatingActionButton fab;
    private FloatingActionButton fab_toggle;
    private FloatingActionButton fab_recycler_refresh;

    //private FloatingActionButton fab1;
    //private FloatingActionButton fab_manage;
    private RecyclerView mRecyclerView;
    private RecyclerAlertViewAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    SystemService systemService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_recycler_view, container, false);
        data = new ArrayList<>();


        //return super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    private void updateBackgroundData() {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                final ArrayList<RailAlertDetails> data = getAlertData(getContext());

                new Handler(getContext().getMainLooper()).post(() -> {
                    FragmentAlertViewNewOne.this.data = data;
                    adapter.setItems(data);
                    adapter.notifyDataSetChanged();

                });

                return "";
            }
        }.execute("");
    }

    private ArrayList<RailAlertDetails> getAlertData(Context context) {
        ArrayList<RailAlertDetails> data = new ArrayList<>();
        data.clear();
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        try {
            JSONObject json = new JSONObject(Utils.getConfig(config, Config.ALERT_JSON, "{}"));
            RailDetailsContainer cat = JSONObjectSerializer.unmarshall(RailDetailsContainer.class, json);

            Collections.reverse(cat.getTrain());
            for (RailAlertDetails v : cat.getTrain()) {
                data.add(v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private int getScreenWidthDp() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        systemService = ((MainActivity) getActivity()).systemService;

        initView(view);
        updateBackgroundData();
//        Toolbar toolbar = view.findViewById(R.id.toolbar_recycler_view);
//        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
//            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }

        super.onViewCreated(view, savedInstanceState);
    }



    private void initView(View activity_recycler_view) {
        fab = activity_recycler_view.findViewById(R.id.fab_recycler_view);
        fab_toggle = activity_recycler_view.findViewById(R.id.fab_recycler_toggle);
        fab_recycler_refresh = activity_recycler_view.findViewById(R.id.fab_recycler_refresh);

        fbExpand = new ExpandableFabButtons(getContext(), fab, R.drawable.ic_outline_close_24px, R.drawable.ic_outline_more_vert_24px);
        fbExpand.addFloatingActionButton(fab_recycler_refresh);
        fbExpand.addHiddenFloatingActionButton(fab_toggle );

        fbExpand.show(false);
        mRecyclerView = activity_recycler_view.findViewById(R.id.recycler_view_recycler_view);

        if (getScreenWidthDp() >= 1200) {
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(activity_recycler_view.getContext(), 3);
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else if (getScreenWidthDp() >= 700) {
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(activity_recycler_view.getContext(), 2);
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else {
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity_recycler_view.getContext());
            mRecyclerView.setLayoutManager(linearLayoutManager);
        }

        adapter = new RecyclerAlertViewAdapter(activity_recycler_view.getContext());
        mRecyclerView.setAdapter(adapter);
        adapter.setItems(data);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                //adapter.addItem(linearLayoutManager.findFirstVisibleItemPosition() + 1, insertData);
                fbExpand.toggle();
            }
        });
        fab_recycler_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbExpand.show(false);
                backgroundRefresh();

            }
        });

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        swipeRefreshLayout = activity_recycler_view.findViewById(R.id.swipe_refresh_layout_recycler_view);
        swipeRefreshLayout.setColorSchemeResources(R.color.google_blue, R.color.google_green, R.color.google_red, R.color.google_yellow);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // talk tot he looper ??
                backgroundRefresh();

            }
        });
    }

    private void backgroundRefresh() {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                if (systemService != null) {
                    SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
                    ConfigUtils.setLong(config, Config.LAST_ALERT_TIME, 0);

                    // send a broad cast out

                    PersistableBundle bundle = new PersistableBundle();
                    bundle.putBoolean("periodic", true);
                    Utils.scheduleJob(getContext().getApplicationContext(), JobID.NJTAlertJobService, NJTAlertJobService.class, (int) 2, false, bundle);
                }
                new Handler(getContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

                return "";
            }
        }.execute("");
    }

    @Override
    public void onTimerEvent(SystemService systemService) {

    }

    @Override
    public void onDepartureVisionUpdated(SystemService systemService) {
//        initData(systemService.getApplicationContext());
//        adapter.setItems(data);
//        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSystemServiceConnected(SystemService systemService) {
        this.systemService = systemService;
    }

    @Override
    public void configChanged(SystemService systemService) {

    }

    @Override
    public void onAlertsUpdated(SystemService systemService) {
        updateBackgroundData();
    }
}
