package com.smartdeviceny.njts.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.smartdeviceny.njts.MainActivity;
import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.SystemService;
import com.smartdeviceny.njts.adapters.NJTScheduleAdapter;
import com.smartdeviceny.njts.adapters.NJTScheduleData;
import com.smartdeviceny.njts.adapters.NJTScheduleHeader;
import com.smartdeviceny.njts.adapters.ServiceConnected;
import com.smartdeviceny.njts.annotations.JSONObjectSerializer;
import com.smartdeviceny.njts.lib.ExpandableFabButtons;
import com.smartdeviceny.njts.parser.DepartureVisionData;
import com.smartdeviceny.njts.parser.DepartureVisionWrapper;
import com.smartdeviceny.njts.parser.Route;
import com.smartdeviceny.njts.utils.ConfigUtils;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FragmentNJTScheduleViewNew extends Fragment implements ServiceConnected {
    private List<DepartureVisionData> data;
    private FloatingActionButton fab;
    private RecyclerView mRecyclerView;
    private NJTScheduleAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    SystemService systemService;
    SharedPreferences config;
    MainActivity mainActivity;


    ExpandableFabButtons fbExpand;
    private FloatingActionButton fab_toggle;
    private FloatingActionButton fab_recycler_refresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.njts_recycler_view, container, false);
        if (data == null) {
            data = new ArrayList<>();
        }
        return view;
    }

    private List<DepartureVisionData> getDepartureVisionData(Context context) {
        List<DepartureVisionData> data = new ArrayList<>();
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        try {
            String currentStationCode = ConfigUtils.getConfig(config, Config.DV_STATION, ConfigDefault.DV_STATION);

            JSONObject json = new JSONObject(Utils.getConfig(config, Config.DEPARTURE_VISION + "." + currentStationCode, ConfigDefault.DEPARTURE_VISION));
            DepartureVisionWrapper wrapper = JSONObjectSerializer.unmarshall(DepartureVisionWrapper.class, json);
            //long time = wrapper.time.getTime();
            for (DepartureVisionData v : wrapper.entries) {
                if (!currentStationCode.isEmpty() && !v.station_code.equals(currentStationCode)) {
                    continue;
                }
                data.add(v);
            }
            Collections.sort(data, (d0, d1) -> Integer.compare(d0.index, d1.index));
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
        mainActivity = ((MainActivity) getActivity());
        config = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());

        initView(view);

//        Toolbar toolbar = view.findViewById(R.id.toolbar_recycler_view);
//        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
//            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
        SystemService systemService = ((MainActivity) getActivity()).systemService;
        if (systemService != null) {
            updateBackground(false); //
        }
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView(View activity_recycler_view) {
        fab = activity_recycler_view.findViewById(R.id.njts_fab);
        fab_toggle = activity_recycler_view.findViewById(R.id.njts_fb_toggle);
        fab_recycler_refresh = activity_recycler_view.findViewById(R.id.njts_fb_refresh);

        fbExpand = new ExpandableFabButtons(getContext(), fab, R.drawable.ic_outline_close_24px, R.drawable.ic_outline_more_vert_24px);
        fbExpand.addFloatingActionButton(fab_toggle);
        fbExpand.addFloatingActionButton(fab_recycler_refresh);
        fbExpand.show(false);

        mRecyclerView = activity_recycler_view.findViewById(R.id.njts_recyclerView);


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

        adapter = new NJTScheduleAdapter(getActivity(), activity_recycler_view.getContext());
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
                updateBackground(false);
            }
        });

        fab_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbExpand.show(false);
                toggleDestinations();
                updateBackground(false);
                Snackbar.make(view, "Switching Start/Stop Stations", Snackbar.LENGTH_SHORT).show();
            }
        });
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        swipeRefreshLayout = activity_recycler_view.findViewById(R.id.njts_swipe_refresh_layout_recycler_view);
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
                        List<DepartureVisionData> dvData = FragmentNJTScheduleViewNew.this.getDepartureVisionData(FragmentNJTScheduleViewNew.this.getContext());
                        new Handler(getContext().getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                                FragmentNJTScheduleViewNew.this.data = dvData;
                                adapter.notifyDataSetChanged();
                            }
                        });

                        return "";
                    }
                }.execute("");

            }
        });
    }

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
    }


    void updateBackground(boolean swiping) {
        new AsyncTask<Context, Void, String>() {
            @Override
            protected String doInBackground(Context... contexts) {
                if (mainActivity.systemService == null) {
                    return "";
                }
                final List<Route> routes = getRoutes(mainActivity.systemService);
                final Object c = adapter.createDataContext();
                String prevHeader = "";
                for (Route rt : routes) {
                    String currentHeader = Utils.formatPrintableTime(rt.departure_time_as_date, "EEE, MMM dd, yyyy");
                    if (!prevHeader.equals(currentHeader)) {
                        NJTScheduleHeader hdr = new NJTScheduleHeader(NJTScheduleHeader.HEADER_TYPE_1, R.layout.njts_header_item_1);
                        hdr.setRouteName(currentHeader);
                        hdr.setTitle(rt.from + " \u279F " + rt.to);
                        prevHeader = currentHeader;
                        adapter.addHeader(c, hdr);
                    }
                    NJTScheduleData item = new NJTScheduleData(rt);
                    adapter.addItem(c, item);
                }
                List<DepartureVisionData> dvData = FragmentNJTScheduleViewNew.this.getDepartureVisionData(FragmentNJTScheduleViewNew.this.getContext());
                final HashMap<String, DepartureVisionData> data = systemService.getCachedDepartureVisionStatus_byTrip();
                new Handler(getContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (swiping) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        adapter.updateDataContext(c);
                        adapter.notifyDataSetChanged();

                        FragmentNJTScheduleViewNew.this.data = dvData;
                        if (data != null) {
                            adapter.updateDepartureVision(data);
                        }

                        mRecyclerView.scrollToPosition(getPosition(routes));
                    }
                });
                return "";
            }
        }.execute(getContext());
    }

    private ArrayList<Route> getRoutes(SystemService systemService) {

        String startStation = config.getString(Config.START_STATION, ConfigDefault.START_STATION);
        String stopStation = config.getString(Config.STOP_STATION, ConfigDefault.STOP_STATION);
        String departureVisionCode = ConfigUtils.getConfig(config, Config.DV_STATION, ConfigDefault.DV_STATION);

        int delta = -1;
        try {
            delta = Integer.parseInt(ConfigUtils.getConfig(config, Config.DELTA_DAYS, "" + delta));
        } catch (Exception e) {
        }
        return systemService.getRoutes(startStation, stopStation, null, delta);
    }

    int getPosition(final List<Route> routes) {
        int index = -1;
        int i = 0;
        Date now = new Date();
        Route start = null;
        try {
            for (Route rt : routes) {
                if (start == null) {
                    start = rt;
                }
                if (rt.departure_time_as_date.getTime() > now.getTime()) {
                    break; // we want this to be in the middle of the page some what.
                }
                index = i;
                i++;
            }
        } catch (Exception e) {
        }
        if (start != null) {
            long days = TimeUnit.MILLISECONDS.toDays(routes.get(index).departure_time_as_date.getTime() - start.departure_time_as_date.getTime());
            if (days > 0) {
                index += days;
            }
            if (index > routes.size()) {

                index = routes.size() - 1;
            }
        }
        index = Math.min(index, routes.size()-1);
        index = Math.max(index, 0);

        return index;
    }

    @Override
    public void onAlertsUpdated(SystemService systemService) {

    }

    @Override
    public void onSystemServiceConnected(SystemService systemService) {
        this.systemService = systemService;
        updateBackground(false);
    }

    @Override
    public void configChanged(SystemService systemService) {

    }
}
