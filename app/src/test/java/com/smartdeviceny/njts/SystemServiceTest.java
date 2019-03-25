package com.smartdeviceny.njts;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;

public class SystemServiceTest {

    @Test
    public void systemServiceBasic() {
        SystemService systemService = Mockito.spy(new SystemService());
        Context context = Mockito.mock(Context.class);


        ApplicationInfo applicationInfo = Mockito.mock(ApplicationInfo.class);
        File file = new File(getClass().getClassLoader().getResource("rail_data.db").getFile());
        applicationInfo.dataDir= file.getParent();
        Mockito.when(systemService.getApplicationContext()).thenReturn(context);
        Mockito.when(context.getApplicationContext()).thenReturn(context);
        Mockito.when(context.getApplicationInfo()).thenReturn(applicationInfo);

        DownloadManager manager = Mockito.mock(DownloadManager.class);
        Mockito.when(context.getSystemService(Context.DOWNLOAD_SERVICE)).thenReturn(manager);


        //Mockito.when(systemService.checkForUpdate(Mockito.anyBoolean())).thenCallRealMethod();
        //Mockito.when(systemService._checkRemoteDBUpdate(Mockito.any())).thenCallRealMethod();
        //Mockito.when(systemService.(Mockito.anyBoolean())).thenCallRealMethod();


        Assert.assertEquals(true, systemService.checkForUpdate(false));
        Assert.assertEquals(false, systemService.checkForUpdate(false));

        Assert.assertEquals(true, systemService.isUpdateRunning());
        Assert.assertEquals(false, systemService.isDatabaseReady());
    }
}
