package com.smartdeviceny.njts;

import android.os.Build;

import com.smartdeviceny.njts.utils.Utils;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;



public class Base64Test {

    @Test
    public void encodeDecodeTest() throws IOException{
       System.out.println("out:" + Build.VERSION.SDK_INT);
        String str = "This is a string";
        System.out.println(Utils.encodeToString(str));
        Assert.assertEquals(str, Utils.decodeToString(Utils.encodeToString(str)));
    }
}
