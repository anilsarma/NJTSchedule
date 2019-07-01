package com.smartdeviceny.njts;

import android.content.Context;
import android.util.Log;

import com.smartdeviceny.njts.parser.DepartureVisionData;
import com.smartdeviceny.njts.parser.DepartureVisionParser;
import com.smartdeviceny.njts.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class TestDepartureVisionParser {

    @Mock
    Context mContext;

    SystemService systemService = new SystemService();
    DepartureVisionParser parser = new DepartureVisionParser();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this.getClass());
    }



    @Test
    public void checkDepartureVision() {
        String code = "NY";
        try {

             InputStream is = this.getClass().getClassLoader().getResourceAsStream("ny_depature_vision_ny_cancel.html");
            Document doc = Jsoup.parse(is, null, "http://dv.njtransit.com");

            HashMap<String, DepartureVisionData> result = parser.parseDepartureVision(code, "New York Penn Station", doc);
            for(String key:result.keySet()) {
                DepartureVisionData data = result.get(key);
                System.out.println(key +  "=[" + data + "]");
            }
            DepartureVisionData data = result.get("3373"); // check first entry.
            assertNotNull(data);
            assertEquals(data.status, "BOARDING");
            assertEquals(data.track, "7");

            data = result.get("6667");
            assertNotEquals(data, null);
            assertEquals(data.status, "CANCELLED");
            assertEquals(data.track, "");


            systemService.updateDepartureVision(code, result);
            systemService.updateActiveDepartureVisionStation("NY");
            Thread.sleep(1000);
            HashMap<String, DepartureVisionData>dv = systemService.getCachedDepartureVisionStatus_byTrip();
            for(String key:dv.keySet()) {
                DepartureVisionData dvd = dv.get(key);
                System.out.println(key +  "=[" + dvd + "]");
            }
            //assertNotEquals(dv.size(),0);

        } catch (Exception e) {
            e.printStackTrace();
            assertEquals(e, null);
        }

    }
}
