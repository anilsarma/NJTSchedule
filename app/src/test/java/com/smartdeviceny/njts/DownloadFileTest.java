package com.smartdeviceny.njts;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

import com.smartdeviceny.njts.utils.DownloadFile;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DownloadFileTest {
   private BroadcastReceiver reciver = null;

    @Test
    public void downloadTest() {
        DownloadManager manager = Mockito.mock(DownloadManager.class);
        Context context = Mockito.mock(Context.class);
        DownloadFile.Callback callback = Mockito.mock(DownloadFile.Callback.class);


        Mockito.when(context.registerReceiver(Mockito.any(), Mockito.any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                BroadcastReceiver recv = invocation.getArgument(0);
                IntentFilter intent = invocation.getArgument(1);

                reciver = recv;
                System.out.println("call the reiver now");
                return null;
            }
        });
        Mockito.when(context.getApplicationContext()).thenReturn(context);
        Mockito.when(context.getSystemService(Context.DOWNLOAD_SERVICE)).thenReturn(manager);
        String url = "http://dv.njtransit.com/mobile/tid-mobile.aspx?sid="+ "NY";


        DownloadFile downloadFile = new DownloadFile(context, callback);
        Mockito.when(manager.enqueue(Mockito.any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        });
        downloadFile.downloadFile(url, "title", "description", DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI,
                "text/html");

        // called enqued.
        Mockito.verify(manager, Mockito.times(1)).enqueue(Mockito.any());
        Mockito.verify(context, Mockito.times(1)).registerReceiver(Mockito.any(), Mockito.any());

        // the download has been requested
        // call the callback
        Intent intent = Mockito.mock(Intent.class);
        Cursor cursor = Mockito.mock(Cursor.class);
        Mockito.when(manager.query(Mockito.any())).thenReturn(cursor);
        Mockito.when(cursor.isAfterLast()).thenAnswer(new Answer<Object>() {
            int count = 0;
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                count ++;
                if (count == 0) {
                    return false;
                }
                return true;
            }
        });

        reciver.onReceive(context, intent);

    }
}
