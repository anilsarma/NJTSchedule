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
import com.smartdeviceny.njts.utils.ConfigUtils;
import com.smartdeviceny.njts.utils.JobID;
import com.smartdeviceny.njts.utils.RailAlertDetails;
import com.smartdeviceny.njts.utils.RailDetailsContainer;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentAlertViewNewOne extends Fragment implements ServiceConnected {
    private List<RailAlertDetails> data;
    private FloatingActionButton fab;
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
        initData(inflater.getContext());

        //return super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    private void initData(Context context) {
        data = new ArrayList<>();
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        try {
            JSONObject json = new JSONObject(Utils.getConfig(config, Config.ALERT_JSON, "{}"));
            RailDetailsContainer cat = JSONObjectSerializer.unmarshall(RailDetailsContainer.class, json);

            Collections.reverse(cat.getTrain());
            for (RailAlertDetails v :cat.getTrain() ) {
                this.data.add(v);
            }
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
        systemService = ((MainActivity) getActivity()).systemService;

        initView(view);

//        Toolbar toolbar = view.findViewById(R.id.toolbar_recycler_view);
//        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
//            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }

        super.onViewCreated(view, savedInstanceState);
    }
    boolean isFABOpen = false;
    private void showFABMenu(){
        isFABOpen=true;
        //fab1.show();
        //fab_manage.show();
        //fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        //fab_manage.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        //fab3.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        //fab1.hide();
        //fab_manage.hide();
        //fab.animate().translationY(0);
        //fab1.animate().translationY(0);
        //fab_manage.animate().translationY(0);
    }
    private void initView(View activity_recycler_view) {
        fab = activity_recycler_view.findViewById(R.id.fab_recycler_view);
        //fab1 = activity_recycler_view.findViewById(R.id.fab1);
        //fab_manage = activity_recycler_view.findViewById(R.id.fab_manage);
        fab.animate().translationY(-getResources().getDimension(R.dimen.standard_55));

        isFABOpen = false;

        //fab_manage.hide();
        //fab1.hide();
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
        adapter.setItems(data);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                //adapter.addItem(linearLayoutManager.findFirstVisibleItemPosition() + 1, insertData);
                if(!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
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
        });
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
}
