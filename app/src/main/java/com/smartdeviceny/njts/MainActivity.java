package com.smartdeviceny.njts;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.smartdeviceny.njts.adapters.FragmentPagerMainPageAdaptor;
import com.smartdeviceny.njts.adapters.ServiceConnected;
import com.smartdeviceny.njts.utils.ConfigUtils;
import com.smartdeviceny.njts.utils.JobID;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;
import com.smartdeviceny.njts.values.NotificationValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    boolean mIsBound = false;
    public SystemService systemService;
    public ProgressDialog progressDialog = null;
    int tabSelected = -1;
    SharedPreferences config;

    String skus[]= { "donation_dollar_4", "njts_subscription_monthly", "njts_yearly_subscription"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //setupConfigDefaults(config, getString(R.string.CONFIG_START_STATION), getString(R.string.CONFIG_DEFAULT_START_STATION));
        //setupConfigDefaults(config, getString(R.string.CONFIG_STOP_STATION), getString(R.string.CONFIG_DEFAULT_STOP_STATION));
        //setupConfigDefaults(config, getString(R.string.CONFIG_DEFAULT_ROUTE), getString(R.string.CONFIG_DEFAULT_ROUTE));

        startService(new Intent(this, PowerStartService.class));
        startService(new Intent(this, SystemService.class));
        setContentView(R.layout.activity_main_new);
        initToolbar();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);
        FragmentPagerMainPageAdaptor adapter = new FragmentPagerMainPageAdaptor(getSupportFragmentManager(), getApplicationContext());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabSelected = tab.getPosition();
                Log.d("MAIN", "Tab selected " + tabSelected);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tabSelected = -1;
                Log.d("MAIN", "Tab un-selected " + tabSelected);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d("MAIN", "Tab selected" + tabSelected);
                onTabSelected(tab);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(NotificationValues.BROADCAT_DATABASE_READY);
            filter.addAction(NotificationValues.BROADCAT_DATABASE_CHECK_COMPLETE);
            filter.addAction(NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED);
            filter.addAction(NotificationValues.BROADCAT_PERIODIC_TIMER);
            filter.addAction(NotificationValues.BROADCAT_NOTIFY_CONFIG_CHANGED);
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // bug in noughat... crap
        Utils.scheduleJob(this.getApplicationContext(), JobID.DepartureVisionJobService, DepartureVisionJobService.class, 15 * 1000, false, null);
    }

    public void doCheckIsDatabaseReady(Context context) {
        if (systemService != null) {
            //systemService.checkForUpdate();
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!systemService.isDatabaseReady()) {
                showUpdateProgressDialog(context, "System downloading a tiny NJ Transit database file");
            }

        } else {
            Log.d("MAIN", "system service not init " + systemService);
        }
    }

    public void doCheckForUpdate(Context context) {
        if (systemService != null) {
            systemService.checkForUpdate(false);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (systemService.isUpdateRunning()) {
                showUpdateProgressDialog(context, "Checking for Latest NJ Transit Train Schedules");
            }
        } else {
            Log.d("MAIN", "system service not init " + systemService);
        }
    }

    public void doForceCheckUpgrade(Context context) {
        if (systemService != null) {
            systemService.doForceCheckUpgrade();
            //doCheckForUpdate(context);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPostResume() {
        if (progressDialog != null && progressDialog.isShowing()) {
            if (systemService != null && !systemService.isUpdateRunning()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
        doCheckIsDatabaseReady(this);
        super.onPostResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private Toolbar initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("NJ Transit Schedule");
        return toolbar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.menu_main, menu);
        //Log.d("MA", "menu created");
        // RecyclerView rv;
        //rv.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_Refresh:
                if (systemService != null) {
                    //SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String departureVisionCode = ConfigUtils.getConfig(config, Config.DV_STATION, ConfigDefault.DV_STATION);
                    systemService.schdeuleDepartureVision(departureVisionCode, 5000);
                }
                return true;
            case R.id.menu_reverse: {
                // swap the routes
                if (tabSelected == 0 || tabSelected == 1 || tabSelected == 3) {
                    //SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String start = ConfigUtils.getConfig(config, Config.START_STATION, ConfigDefault.START_STATION);
                    String stop = ConfigUtils.getConfig(config, Config.STOP_STATION, ConfigDefault.STOP_STATION);
                    ConfigUtils.setConfig(config, Config.START_STATION, stop);
                    ConfigUtils.setConfig(config, Config.STOP_STATION, start);
                    String station_code = systemService.getStationCode(stop);// since we are swaping ..
                    Utils.setConfig(config, Config.DV_STATION, station_code);

                    //for(Fragment f:getSupportFragmentManager().getFragments())
                    if (systemService != null) {
                        systemService.updateActiveDepartureVisionStation(station_code);
                        systemService.schdeuleDepartureVision(station_code, 10000);
                    }
                    sendNotifyConfigChanged();
                }
            }
            break;
            case R.id.menu_Settings: {

            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateFavorite(boolean status, String block_id) {
        if (status) {
            systemService.addFavorite(block_id);
        } else {
            systemService.removeFavorite(block_id);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            systemService = ((RemoteBinder) service).getService();
            doCheckIsDatabaseReady(MainActivity.this);

            Log.d("MAIN", "SystemService connected, calling onSystemServiceConnected on fragments");
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                ServiceConnected frag = (ServiceConnected) f;
                if (frag != null) {
                    frag.onSystemServiceConnected(systemService);
                }
            }

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

    public void showUpdateProgressDialog(Context context, @Nullable String msg) {
        progressDialog = new ProgressDialog(context);
        if (msg == null) {
            msg = "Checking for NJ Transit schedule updates";
        }
        progressDialog.setMessage(msg);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void doConfigChanged() {
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            ServiceConnected frag = (ServiceConnected) f;
            if (frag != null) {
                if (systemService != null) {
                    frag.configChanged(systemService);
                }
            }
        }
        Utils.scheduleJob(getApplicationContext(), JobID.UpdateCheckerJobService, UpdateCheckerJobService.class, (int) TimeUnit.SECONDS.toMillis(10), false, null);
    }

    public void sendNotifyConfigChanged() {
        Intent intent = new Intent(NotificationValues.BROADCAT_NOTIFY_CONFIG_CHANGED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("MAIN", "sending BROADCAT_NOTIFY_CONFIG_CHANGED");

    }

    public String getStationCode() {
        return ConfigUtils.getConfig(config, Config.DV_STATION, ConfigDefault.DV_STATION);
    }

    public class LocalBcstReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            //Log.d("MAIN", "onReceive " + intent.getAction());
            if (intent.getAction().equals(NotificationValues.BROADCAT_DATABASE_READY)) {
                //Log.d("MAIN", "Database is ready we can do all the good stuff");
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                doConfigChanged();
            } else if (intent.getAction().equals(NotificationValues.BROADCAT_DATABASE_CHECK_COMPLETE)) {
                //Log.d("MAIN", NotificationValues.BROADCAT_DATABASE_CHECK_COMPLETE);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } else if (intent.getAction().equals(NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED)) {
                //Log.d("MAIN", NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED);
                for (Fragment f : getSupportFragmentManager().getFragments()) {
                    if (!ServiceConnected.class.isAssignableFrom(f.getClass())) {
                        continue;
                    }
                    ServiceConnected frag = (ServiceConnected) f;
                    if (systemService != null) {
                        frag.onDepartureVisionUpdated(systemService);
                    }


                }
            } else if (intent.getAction().equals(NotificationValues.BROADCAT_ALERT_UPDATED)) {
                //Log.d("MAIN", NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED);
                for (Fragment f : getSupportFragmentManager().getFragments()) {
                    if (!ServiceConnected.class.isAssignableFrom(f.getClass())) {
                        continue;
                    }
                    ServiceConnected frag = (ServiceConnected) f;
                    if (systemService != null) {
                        frag.onAlertsUpdated(systemService);
                    }


                }
            } else if (intent.getAction().equals(NotificationValues.BROADCAT_PERIODIC_TIMER)) {
                //Log.d("MAIN", NotificationValues.BROADCAT_PERIODIC_TIMER);
                boolean hasfrag = false;
                for (Fragment f : getSupportFragmentManager().getFragments()) {
                    ServiceConnected frag = (ServiceConnected) f;
                    if (!ServiceConnected.class.isAssignableFrom(f.getClass())) {
                        continue;
                    }
                    if (systemService != null) {
                        frag.onTimerEvent(systemService);
                    }
                }
            } else if (intent.getAction().equals(NotificationValues.BROADCAT_NOTIFY_CONFIG_CHANGED)) {
                //Log.d("MAIN", NotificationValues.BROADCAT_NOTIFY_CONFIG_CHANGED);
                for (Fragment f : getSupportFragmentManager().getFragments()) {
                    ServiceConnected frag = (ServiceConnected) f;
                    if (!ServiceConnected.class.isAssignableFrom(f.getClass())) {
                        continue;
                    }
                    if (systemService != null) {
                        frag.configChanged(systemService);
                    }
                }
            } else {
                Log.d("MAIN", "got something not sure what " + intent.getAction());
            }
        }
    }

    // Our handler for received Intents. This will be called whenever an Intent
    private BroadcastReceiver mMessageReceiver = new LocalBcstReceiver();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Toast.makeText(this, "Selected something " + menuItem.getTitle(), Toast.LENGTH_LONG).show();
        switch (menuItem.getItemId()) {
            case R.id.nav_license:
                directToWeb("http://www.google.com/");
                break;
            case R.id.nave_rate:
                openPlayStore();
                break;
            case R.id.nav_donate:
                donate();
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //IabHelper mHelper;
    private BillingClient billingClient;
Map<String, SkuDetails> details = new HashMap<>();
    void donate() {
//        if (mHelper == null) {
//            mHelper = new IabHelper(this, "");
//            try {
//                mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
//                    public void onIabSetupFinished(IabResult result) {
//                        if (!result.isSuccess()) {
//                            String msg = "Problem setting up In-app Billing: " + result;
//                            Log.e("BILLING", msg);
//                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//                        } else {
//                            try {
//                                loadProducts();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        Toast.makeText(MainActivity.this, "PBilling complete" + result, Toast.LENGTH_SHORT).show();
//                    }
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            loadProducts();
//        }

        try {

            if (billingClient == null) {
                billingClient = BillingClient.newBuilder(MainActivity.this).setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
                        System.out.println("CONN onPurchasesUpdated SKU: " + billingResult.getDebugMessage());
                        if(purchases!=null) {
                            for(Purchase p:purchases) {
                                System.out.println("CONN Purchase getOrderId:" + p.getOrderId());
                                System.out.println("CONN Purchase getSku:" + p.getSku());
                                System.out.println("CONN Purchase getDeveloperPayload:" + p.getDeveloperPayload());
                                System.out.println("CONN Purchase getPurchaseToken:" + p.getPurchaseToken());
                                System.out.println("CONN Purchase getPurchaseTime:" + p.getPurchaseTime());

                            }
                        }
                    }
                }).enablePendingPurchases().build();
                billingClient.startConnection(new BillingClientStateListener() {
                    @Override
                    public void onBillingSetupFinished(BillingResult billingResult) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            System.out.println("CONN onBillingSetupFinished OK: " + billingResult.getDebugMessage());
                        } else {
                            System.out.println("CONN onBillingSetupFinished error: " + billingResult.getDebugMessage());
                            billingClient=null;
                        }
                    }

                    @Override
                    public void onBillingServiceDisconnected() {
                        System.out.println("CONN onBillingSetupFinished SKU: " );
                        billingClient = null;
                    }
                });
            } else {
                ArrayList<String> r = new ArrayList<>();
                for(String s:skus) {
                    r.add(s);
                }
                SkuDetailsParams param = SkuDetailsParams.newBuilder()
                        .setSkusList(r).setType(BillingClient.SkuType.INAPP).build();
                billingClient.querySkuDetailsAsync(param, new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<com.android.billingclient.api.SkuDetails> skuDetailsList) {
                        System.out.println("querySkuDetailsAsync INAPP: " + billingResult.getDebugMessage() + " " + skuDetailsList.size() );
                        for (com.android.billingclient.api.SkuDetails sku:skuDetailsList) {

                            Log.d("INAPP SKU", "descr: " + sku.getDescription());
                            Log.d("INAPP SKU", "price: " + sku.getPrice());
                            Log.d("INAPP SKU", "getTitle: " + sku.getTitle());
                            Log.d("INAPP SKU", "getSku: " + sku.getSku());
                            Log.d("INAPP SKU", "getSubscriptionPeriod: " + sku.getSubscriptionPeriod());
                            details.put(sku.getSku(), sku);
                        }
                    }
                });
                SkuDetailsParams param2= SkuDetailsParams.newBuilder()
                        .setSkusList(r).setType(BillingClient.SkuType.SUBS).build();
                billingClient.querySkuDetailsAsync(param2, new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<com.android.billingclient.api.SkuDetails> skuDetailsList) {
                        System.out.println("querySkuDetailsAsync SUb : " + billingResult.getDebugMessage() + " " + skuDetailsList.size() );
                        for (com.android.billingclient.api.SkuDetails sku:skuDetailsList) {

                            Log.d("SUB SKU", "descr: " + sku.getDescription());
                            Log.d("SUB SKU", "price: " + sku.getPrice());
                            Log.d("SUB SKU", "getTitle: " + sku.getTitle());
                            Log.d("SUB SKU", "getSku: " + sku.getSku());
                            Log.d("SUB SKU", "getSubscriptionPeriod: " + sku.getSubscriptionPeriod());
                            details.put(sku.getSku(), sku);
                        }
                    }
                });
            }
            Purchase.PurchasesResult result =  billingClient.queryPurchases(BillingClient.SkuType.INAPP);

            if(result==null || result.getPurchasesList()==null||result.getPurchasesList().size()==0) {
                System.out.println("PURCHASE " + result);

                SkuDetails sku = details.get("donation_dollar_4");
                if(sku!=null) {
                    BillingFlowParams params = BillingFlowParams.newBuilder().setSkuDetails(sku).build();
                   BillingResult r =  billingClient.launchBillingFlow(MainActivity.this, params);
                    Toast.makeText(MainActivity.this, "SKU " + r.getDebugMessage() + " " + r.getResponseCode(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "No SKU ", Toast.LENGTH_LONG).show();
                }

                sku = details.get(skus[1]);
                if(sku!=null) {
                    BillingFlowParams params = BillingFlowParams.newBuilder().setSkuDetails(sku).build();
                    BillingResult r =  billingClient.launchBillingFlow(MainActivity.this, params);
                    Toast.makeText(MainActivity.this, "SKU " + r.getDebugMessage() + " " + r.getResponseCode(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "No SKU ", Toast.LENGTH_LONG).show();
                }

            } else {
                for (Purchase purchase : result.getPurchasesList()) {
                    System.out.println("queryPurchases PURCHASE:" + purchase.getOrderId());
                    System.out.println("queryPurchases PURCHASE:" + purchase.getSku());
                    System.out.println("queryPurchases PURCHASE:" + purchase.getPurchaseState());
                    System.out.println("queryPurchases PURCHASE:" + purchase.getPurchaseTime());
                    System.out.println("queryPurchases Purchase getPurchaseToken:" + purchase.getPurchaseToken());

                    ConsumeParams cparam = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                    billingClient.consumeAsync(cparam, new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                            System.out.println("CONN Purchase onConsumeResponse :" + purchaseToken + " " + billingResult.getResponseCode() + " " + billingResult.getDebugMessage());
                        }
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadProducts() {
        try {
            List<String> moreItemSkus = Arrays.asList("donation_dollar_4");
            List<String> moreSubItemSkus = Arrays.asList();

//            mHelper.launchPurchaseFlow(MainActivity.this, "donation_dollar_4", 10001, new IabHelper.OnIabPurchaseFinishedListener() {
//                @Override
//                public void onIabPurchaseFinished(IabResult result, com.smartdeviceny.njts.billing.Purchase info) {
//                    System.out.println("RESULT BIL " + result.getMessage());
//                    if (result.getResponse() == 7) { // already owned
//                        // cons
//                        //mHelper.consumeAsync();
//                    }
//                    if (info != null) {
//                        System.out.println("PURCHASE:" + info.getOrderId());
//                        System.out.println("PURCHASE:" + info.getSku());
//                        System.out.println("PURCHASE:" + info.getPurchaseState());
//                        System.out.println("PURCHASE:" + info.getPurchaseTime());
//                    }
//                }
//            });
//
//            mHelper.queryInventoryAsync(true, moreItemSkus, moreSubItemSkus, new IabHelper.QueryInventoryFinishedListener() {
//                @Override
//                public void onQueryInventoryFinished(IabResult result, Inventory inv) {
//                    try {
//                        //Field method = inv.getClass().getDeclaredField("mSkuMap");
//                        //method.setAccessible(true);
//                        System.out.println("BILLING getTitle" + inv);
//                        for (SkuDetails sku : Arrays.asList(inv.getSkuDetails("donation_dollar_4"))) {
//                            //SkuDetails sku = inv.getSkuDetails("DONATE");
//
//                            System.out.println("BILLING getTitle" + sku.getTitle());
//                            System.out.println("BILLING getDescription" + sku.getDescription());
//                            System.out.println("BILLING getPrice" + sku.getPrice());
//                            System.out.println("BILLING getSku" + sku.getSku());
//                            System.out.println("BILLING getType" + sku.getType());
//                            com.smartdeviceny.njts.billing.Purchase purchase = inv.getPurchase(sku.getSku());
//                            System.out.println("BILLING Purchase" + purchase);
//                            if (purchase != null) {
//                                mHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
//                                    @Override
//                                    public void onConsumeFinished(com.smartdeviceny.njts.billing.Purchase purchase, IabResult result) {
//                                        System.out.println("BILLING Purchase" + purchase + " consumed");
//                                    }
//                                });
//                            } else {
//
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void directToWeb(String site) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(site));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.smartdeviceny.njts")));
        } catch (android.content.ActivityNotFoundException anfe) {
            directToWeb("https://play.google.com/store/apps/details?id=com.smartdeviceny.njts");
        }
    }
}
