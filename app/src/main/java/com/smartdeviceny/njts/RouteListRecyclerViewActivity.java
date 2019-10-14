package com.smartdeviceny.njts;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartdeviceny.njts.utils.StopDetails;
import com.smartdeviceny.njts.utils.StopsParser;

import java.util.ArrayList;
import java.util.List;

public class RouteListRecyclerViewActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;
    private RouteListViewAdapter adapter;
    private int color = 0;
    private List<String> data;
    private String insertData;
    private boolean loading;
    private int loadTimes;
    boolean mIsBound = false;
    SystemService systemService;
    String trip_id;
    String block_id;
    String from;
    String route_name;
    String to;
    String header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        trip_id = bundle.getString("trip_id");
        block_id = bundle.getString("block_id");
        from = bundle.getString("from");
        to = bundle.getString("to");
        route_name = bundle.getString("route_name");

        header = from + " \u279F " + to;
        doBindService();
        //systemService = ((MainActivity)getApplicationContext()).systemService;

        setContentView(R.layout.activity_routelist_recycler_view);

        Toolbar toolbar = findViewById(R.id.toolbar_routelist_recycler_view);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //getSupportActionBar().setHomeButtonEnabled(true);
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (toolbar != null) {
            //toolbar.setTitle(route_name);
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_close_white_24dp));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //What to do on back clicked
                    //super.onBackPressed();
                    RouteListRecyclerViewActivity.this.onBackPressed();
                }
            });
        }
        initData();
        initView();

//        RouteListRecyclerRouteDecoration sectionItemDecoration =new RouteListRecyclerRouteDecoration(
//                getResources().getDimensionPixelSize(R.dimen.recycler_section_header_height),true, getSectionCallback());
//        //getResources().getDimensionPixelSize(R.dimen.recycler_section_header_height),true, getSectionCallback());
//        mRecyclerView.addItemDecoration(sectionItemDecoration);
    }


    private void initData() {
        data = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            data.add(i + "");
        }

        insertData = "0";
        loadTimes = 0;
    }

    private void initView() {
        fab = findViewById(R.id.routelist_fab_recycler_view);
        fab.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_outline_close_24px));
        mRecyclerView = findViewById(R.id.routelist_content_recycler_view);

        if (getScreenWidthDp() >= 1200) {
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else if (getScreenWidthDp() >= 800) {
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else {
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(linearLayoutManager);
        }

        adapter = new RouteListViewAdapter(this);
        mRecyclerView.setAdapter(adapter);
        //adapter.setItems(data);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RouteListRecyclerViewActivity.this.finish();
                //LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                //adapter.addItem(linearLayoutManager.findFirstVisibleItemPosition() + 1, insertData);
            }
        });

        //ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        //ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        //mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_routelist_recycler_view);
        swipeRefreshLayout.setColorSchemeResources(R.color.google_blue, R.color.white, R.color.google_red, R.color.google_yellow);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (color > 4) {
                            color = 0;
                        }
                        adapter.setColor(++color);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);

            }
        });

        mRecyclerView.addOnScrollListener(scrollListener);
    }

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (!loading && linearLayoutManager.getItemCount() == (linearLayoutManager.findLastVisibleItemPosition() + 1)) {

//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (loadTimes <= 5) {
//                            adapter.removeFooter();
//                            loading = false;
//                            adapter.addItems(data);
//                            adapter.addFooter();
//                            loadTimes++;
//                        } else {
//                            adapter.removeFooter();
////                            Snackbar.make(mRecyclerView, getString(R.string.no_more_data), Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
////                                @Override
////                                public void onDismissed(Snackbar transientBottomBar, int event) {
////                                    super.onDismissed(transientBottomBar, event);
////                                    loading = false;
////                                    adapter.addFooter();
////                                }
////                            }).show();
//                        }
//                    }
//                }, 1500);

                //loading = true;
            }
        }
    };


    private int getScreenWidthDp() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            systemService = ((RemoteBinder) service).getService();
            // list of name value pairs from the trips table containing
            // arrival_time, arrival_time,
            ArrayList<StopDetails>  stops = StopsParser.parse(systemService.getTripStops(trip_id));
            for(StopDetails st:stops) {
                st.block_id = block_id;
                st.from = from;
                st.route_name = route_name;
                st.to = to;

            }
            adapter.addHeader(header, route_name, block_id);
            adapter.setItems(stops);

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            systemService = null;
            Log.d("MAIN", "SystemService disconnected");
        }
    };

    void doBindService() {
        if (!mIsBound) {
            Log.d("SVCON", "SystemService binding.");
            bindService(new Intent(this, SystemService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            Log.d("SVCON", "SystemService doUnbindService.");
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            unbindService(mConnection);
            mIsBound = false;
            //textStatus.setText("Unbinding.");
        }
    }
}
