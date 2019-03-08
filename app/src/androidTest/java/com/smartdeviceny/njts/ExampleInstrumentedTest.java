package com.smartdeviceny.njts;

import android.content.Context;
import android.database.sqlite.SQLiteQuery;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.smartdeviceny.njts.utils.SQLiteLocalDatabase;
import com.smartdeviceny.njts.utils.SqlUtils;
import com.smartdeviceny.njts.utils.UtilsDBVerCheck;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.smartdeviceny.njts", appContext.getPackageName());
        File f = new File(appContext.getApplicationInfo().dataDir + File.separator + "rails_db.sql");
        SQLiteLocalDatabase sql = UtilsDBVerCheck.getSQLDatabase(appContext, f);
        assertNotEquals(null, sql);
        assertEquals("NY", SqlUtils.getStationCode(sql.getReadableDatabase(), "New york Penn Station"));
    }



}
