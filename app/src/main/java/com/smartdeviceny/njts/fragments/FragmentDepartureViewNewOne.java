package com.smartdeviceny.njts.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.smartdeviceny.njts.MainActivity;
import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.SystemService;
import com.smartdeviceny.njts.adapters.RecycleDepartureVisionData;
import com.smartdeviceny.njts.adapters.RecycleDepartureVisionHeaderData;
import com.smartdeviceny.njts.adapters.RecyclerDepartureWithHeaderViewAdapter;
import com.smartdeviceny.njts.adapters.ServiceConnected;
import com.smartdeviceny.njts.annotations.JSONObjectSerializer;
import com.smartdeviceny.njts.lib.ExpandableFabButtons;
import com.smartdeviceny.njts.lib.RouteOperations;
import com.smartdeviceny.njts.parser.DepartureVisionData;
import com.smartdeviceny.njts.parser.DepartureVisionWrapper;
import com.smartdeviceny.njts.utils.ConfigUtils;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentDepartureViewNewOne extends Fragment implements ServiceConnected, RouteOperations {
    private List<DepartureVisionData> data;
    private FloatingActionButton fab;
    private FloatingActionButton fab_toggle;
    private FloatingActionButton fab_recycler_refresh;


    private RecyclerView mRecyclerView;
    private RecyclerDepartureWithHeaderViewAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    SystemService systemService;
    SharedPreferences config;
    MainActivity mainActivity;
    boolean expanded = false;

    ExpandableFabButtons fbExpand;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_recycler_view, container, false);


        //return super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    void updateBackground(boolean swiping) {
        new AsyncTask<Context, Void, String>() {
            @Override
            protected String doInBackground(Context... contexts) {
                List<DepartureVisionData> data = getDepartureData(getContext());
                final Object c = adapter.createDataContext();
                String prevHeader = "";
                for (DepartureVisionData dv : data) {
                    String currentHeader = dv.station_code;
                    if (!prevHeader.equals(currentHeader)) {
                        RecycleDepartureVisionHeaderData hdr = new RecycleDepartureVisionHeaderData(RecycleDepartureVisionHeaderData.HEADER_TYPE, R.layout.njts_header_item_1);
                        String name = (systemService == null) ? dv.station_code : systemService.getStationNameFromCode(dv.station_code);
                        name = name == null ? dv.station_code : name;
                        hdr.setTitle(name + " Departures ");
                        hdr.setSubtitle(Utils.formatPrintableTime(dv.getHackCreateTime(), "MMM, dd yyyy hh:mm a"));
                        prevHeader = currentHeader;
                        adapter.addHeader(c, hdr);
                    }
                    RecycleDepartureVisionData item = new RecycleDepartureVisionData(dv);
                    adapter.addItem(c, item);
                }
                new Handler(getContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (swiping) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        adapter.updateDataContext(c);
                        adapter.notifyDataSetChanged();
                    }
                });
                return "";
            }
        }.execute(getContext());
    }

    private List<DepartureVisionData> getDepartureData(Context context) {
        List<DepartureVisionData> data = new ArrayList<>();

        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        try {
            String currentStationCode = ConfigUtils.getConfig(config, Config.DV_STATION, ConfigDefault.DV_STATION);

            JSONObject json = new JSONObject(Utils.getConfig(config, Config.DEPARTURE_VISION + "." + currentStationCode, ConfigDefault.DEPARTURE_VISION));
            DepartureVisionWrapper wrapper = JSONObjectSerializer.unmarshall(DepartureVisionWrapper.class, json);
            //long time = wrapper.time.getTime();
            for (DepartureVisionData v : wrapper.entries) {
                //Date dt = new Date(time);
                if (!currentStationCode.isEmpty() && !v.station_code.equals(currentStationCode)) {
                    continue;
                }
                //v.createTime = Utils.makeDate(Utils.getTodayYYYYMMDD(dt), v.tableTime, "yyyyMMdd HH:mm a");
                data.add(v);
            }
            //data.sort((d0, d1) -> Integer.compare(d0.index, d1.index)); API 24
            Collections.sort(data, (d0, d1) -> Integer.compare(d0.index, d1.index));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    void refresh() {
        SystemService systemService = mainActivity.systemService;
        if (systemService != null) {
            String departureVisionCode = ConfigUtils.getConfig(config, Config.DV_STATION, ConfigDefault.DV_STATION);
            systemService.schdeuleDepartureVision(departureVisionCode, 30000);
            //Snackbar.make(view, "Refreshing Departure Vision for " + departureVisionCode, Snackbar.LENGTH_SHORT).show();
            //adapter.notifyDataSetChanged();
        } else {
//            new Handler(getContext().getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                   refresh();
//                }
//            }, 2000);
        }

    }

    private int getScreenWidthDp() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        systemService = ((MainActivity) getActivity()).systemService;
        mainActivity = ((MainActivity) getActivity());
        config = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());

        initView(view);
        refresh();
        updateBackground(false);
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
        fbExpand.addFloatingActionButton(fab_toggle);
        fbExpand.addFloatingActionButton(fab_recycler_refresh);

//        fab.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
//        fab_toggle.animate().translationY(-getResources().getDimension(R.dimen.standard_105)); // outside of the screen.
//        fab_recycler_refresh.animate().translationY(-getResources().getDimension(R.dimen.standard_155)); // outside of the screen.


        fbExpand.show(false);
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

        adapter = new RecyclerDepartureWithHeaderViewAdapter(activity_recycler_view.getContext(), this);
        mRecyclerView.setAdapter(adapter);
        //adapter.setItems(data);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fbExpand.toggle();
            }
        });
        fab_recycler_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbExpand.show(false);
                String departureVisionCode = ConfigUtils.getConfig(config, Config.DV_STATION, ConfigDefault.DV_STATION);
                SystemService systemService = mainActivity.systemService;
                if (systemService != null) {
                    systemService.schdeuleDepartureVision(departureVisionCode, 30000);
                    Snackbar.make(view, "Refreshing Departure Vision for " + departureVisionCode, Snackbar.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }
            }
        });

        fab_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbExpand.show(false);
                toggleDestinations();
                Snackbar.make(view, "Switching Start/Stop Stations", Snackbar.LENGTH_SHORT).show();
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
                        if (systemService != null) {
                            SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
                            String departureVisionCode = ConfigUtils.getConfig(config, Config.DV_STATION, ConfigDefault.DV_STATION);
                            systemService.schdeuleDepartureVision(departureVisionCode, 5000);
                        }
                        updateBackground(true);
                        return "";
                    }
                }.execute("");

            }
        });
    }

//    void showFabExpanded(boolean expand) {
//        if(expand) {
//            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_outline_close_24px));
//
//            fab_toggle.animate().translationZ(0);
//            fab_recycler_refresh.animate().translationZ(0);
//
//            fab_toggle.animate().translationY(-getResources().getDimension(R.dimen.standard_55)).setInterpolator(new FastOutSlowInInterpolator());
//            fab_recycler_refresh.animate().translationY(-getResources().getDimension(R.dimen.standard_55)).setInterpolator(new FastOutSlowInInterpolator());
//
//        } else {
//            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_outline_more_vert_24px));
//            fab_toggle.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
//            fab_toggle.animate().translationZ(-100);
//
//            fab_recycler_refresh.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
//            fab_recycler_refresh.animate().translationZ(-100);
//
//        }
//    }

    public void toggleDestinations() {
        String start = ConfigUtils.getConfig(config, Config.START_STATION, ConfigDefault.START_STATION);
        String stop = ConfigUtils.getConfig(config, Config.STOP_STATION, ConfigDefault.STOP_STATION);
        ConfigUtils.setConfig(config, Config.START_STATION, stop);
        ConfigUtils.setConfig(config, Config.STOP_STATION, start);
        SystemService systemService = mainActivity.systemService;
        if (systemService != null) {
            String station_code = systemService.getStationCode(stop);// since we are swaping ..
            Utils.setConfig(config, Config.DV_STATION, station_code);

            //for(Fragment f:getSupportFragmentManager().getFragments())
            if (systemService != null) {
                systemService.updateActiveDepartureVisionStation(station_code);
                systemService.schdeuleDepartureVision(station_code, 10000);
            }
        }
    }

    @Override
    public void onTimerEvent(SystemService systemService) {

    }

    @Override
    public void onDepartureVisionUpdated(SystemService systemService) {
        updateBackground(false);

        //adapter.setItems(data); // notify called internally.
        //adapter.notifyDataSetChanged();
    }

    @Override
    public void onAlertsUpdated(SystemService systemService) {

    }

    @Override
    public void onSystemServiceConnected(SystemService systemService) {
        this.systemService = systemService;
        refresh();
    }

    @Override
    public void configChanged(SystemService systemService) {

    }
}
