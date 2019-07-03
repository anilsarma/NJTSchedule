package com.smartdeviceny.njts;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;

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
        //SQLiteLocalDatabase sql = UtilsDBVerCheck.getSQLDatabase(appContext, f);
        //assertNotEquals(null, sql);
        //assertEquals("NY", SqlUtils.getStationCode(sql.getReadableDatabase(), "New york Penn Station"));
    }

}
