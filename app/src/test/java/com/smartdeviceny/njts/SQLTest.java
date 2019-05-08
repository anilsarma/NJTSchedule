package com.smartdeviceny.njts;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.smartdeviceny.njts.utils.SQLiteLocalDatabase;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

public class SQLTest {

    @Test
    public void test() {
        Context context = Mockito.mock(Context.class);
        ApplicationInfo appInfo = Mockito.mock(ApplicationInfo.class);

        appInfo.dataDir = null;
        Mockito.when(context.getApplicationInfo()).thenReturn(appInfo);
        //File file = new File(getClass().getClassLoader().getResource("rail_data.db").getFile());

        //SQLiteLocalDatabase sql = new SQLiteLocalDatabase(context, file.getName(), file.getParent());
    }
}
