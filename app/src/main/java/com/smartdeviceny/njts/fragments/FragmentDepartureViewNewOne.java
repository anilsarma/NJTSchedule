package com.smartdeviceny.njts.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.smartdeviceny.njts.MainActivity;
import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.SystemService;
import com.smartdeviceny.njts.adapters.ServiceConnected;
import com.smartdeviceny.njts.annotations.JSONObjectSerializer;
import com.smartdeviceny.njts.parser.DepartureVisionData;
import com.smartdeviceny.njts.parser.DepartureVisionParser;
import com.smartdeviceny.njts.parser.DepartureVisionWrapper;
import com.smartdeviceny.njts.utils.ConfigUtils;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentDepartureViewNewOne extends Fragment implements ServiceConnected {
    private List<DepartureVisionData> data;
    private FloatingActionButton fab;
    private RecyclerView mRecyclerView;
    private RecyclerAlertViewAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    SystemService systemService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.activity_recycler_view, container, false);

//
//
//        Toolbar toolbar = inflater.findViewById(R.id.toolbar_recycler_view);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
        initData(inflater.getContext());

        //return super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    private void initData(Context context) {
        data = new ArrayList<>();
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        try {
            JSONObject json = new JSONObject(Utils.getConfig(config, Config.DEPARTURE_VISION, ConfigDefault.DEPARTURE_VISION));
            DepartureVisionWrapper wrapper =(DepartureVisionWrapper) JSONObjectSerializer.unmarshall(DepartureVisionWrapper.class, json);
//            String data = (String) json.get("data");
//            String code = (String) json.get("code");
//            long time = json.getLong("time");

            String code = wrapper.code;
            long time = wrapper.time.getTime();
            {
                //data = Utils.decodeToString(data);
                //DepartureVisionParser parser = new DepartureVisionParser();
                //HashMap<String, DepartureVisionData> dv = parser.parseDepartureVision(code, Jsoup.parse(data));
                for (DepartureVisionData v: wrapper.entries) {
                    Date dt = new Date(time);
                    v.createTime = Utils.makeDate(Utils.getTodayYYYYMMDD(dt), v.tableTime, "yyyyMMdd HH:mm a");
                    this.data.add(v);
                }
            }
            this.data.sort((d0, d1) -> Integer.compare(d0.index, d1.index) );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int getScreenWidthDp() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        systemService = ((MainActivity)getActivity()).systemService;
        initView(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView(View activity_recycler_view) {
        fab = activity_recycler_view.findViewById(R.id.fab_recycler_view);
        mRecyclerView = activity_recycler_view.findViewById(R.id.recycler_view_recycler_view);

        if (getScreenWidthDp() >= 1200) {
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(activity_recycler_view.getContext(), 3);
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else if (getScreenWidthDp() >= 800) {
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(activity_recycler_view.getContext(), 2);
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else {
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity_recycler_view.getContext());
            mRecyclerView.setLayoutManager(linearLayoutManager);
        }

        adapter = new RecyclerAlertViewAdapter(activity_recycler_view.getContext());
        mRecyclerView.setAdapter(adapter);
        //adapter.addHeader();
        adapter.setItems(data);
        //adapter.addFooter();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                //adapter.addItem(linearLayoutManager.findFirstVisibleItemPosition() + 1, insertData);
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
                new AsyncTask<String, Void, String>() {
                    @Override
                    protected String doInBackground(String... strings) {
                        if(systemService !=null ) {
                            SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
                            String departureVisionCode = ConfigUtils.getConfig(config, Config.DV_STATION, ConfigDefault.DV_STATION);
                            systemService.schdeuleDepartureVision(departureVisionCode, 5000);
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
        });
    }
    @Override
    public void onTimerEvent(SystemService systemService) {

    }

    @Override
    public void onDepartureVisionUpdated(SystemService systemService) {
        initData(systemService.getApplicationContext());
        adapter.setItems(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSystemServiceConnected(SystemService systemService) {
        this.systemService = systemService;
    }

    @Override
    public void configChanged(SystemService systemService) {

    }
}
