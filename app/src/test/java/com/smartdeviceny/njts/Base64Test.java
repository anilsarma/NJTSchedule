package com.smartdeviceny.njts;

import android.os.Build;

import com.smartdeviceny.njts.utils.Utils;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 24)
public class Base64Test {

    @Test
    public void encodeDecodeTest() throws IOException{
       System.out.println("out:" + Build.VERSION.SDK_INT);
        String str = "This is a string";
        System.out.println(Utils.encodeToString(str));
        Assert.assertEquals(str, Utils.decodeToString(Utils.encodeToString(str)));
    }
}
