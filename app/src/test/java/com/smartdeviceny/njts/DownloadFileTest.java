package com.smartdeviceny.njts;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

import com.smartdeviceny.njts.utils.DownloadFile;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Uri.class)
public class DownloadFileTest {
   private BroadcastReceiver reciver = null;

    @Test
    public void downloadTest() throws IOException {
        DownloadManager manager = Mockito.mock(DownloadManager.class);
        Context context = Mockito.mock(Context.class);
        DownloadFile.Callback callback = Mockito.mock(DownloadFile.Callback.class);
        Mockito.when(callback.downloadComplete(Mockito.any(), Mockito.anyLong(), Mockito.any(), Mockito.any())).thenReturn(true);


        Mockito.when(context.registerReceiver(Mockito.any(), Mockito.any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                BroadcastReceiver recv = invocation.getArgument(0);
                IntentFilter intent = invocation.getArgument(1);

                reciver = recv;
                System.out.println("call the reciver now");
                return null;
            }
        });
        Mockito.when(context.getApplicationContext()).thenReturn(context);
        Mockito.when(context.getSystemService(Context.DOWNLOAD_SERVICE)).thenReturn(manager);
        String url = "http://dv.njtransit.com/mobile/tid-mobile.aspx?sid="+ "NY";


        DownloadFile downloadFile = new DownloadFile(context, callback);
        final int requestid = 12;
        Mockito.when(manager.enqueue(Mockito.any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return requestid;
            }
        });
        downloadFile.downloadFile(url, "title", "description",
                DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI,
                "text/html");

        // called enqued.
        Mockito.verify(manager, Mockito.times(1)).enqueue(Mockito.any());
        Mockito.verify(context, Mockito.times(1)).registerReceiver(Mockito.any(), Mockito.any());

        // the download has been requested
        // call the callback
        Intent intent = Mockito.mock(Intent.class);
        Cursor cursor = Mockito.mock(Cursor.class);
        Mockito.when(manager.query(Mockito.any())).thenReturn(cursor);

        final DownloadDataTable queryData = new DownloadDataTable();
        queryData.add( DownloadManager.STATUS_SUCCESSFUL, requestid);


        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("NJT NYP Departures.html");

        File f = File.createTempFile("download_manager", ".testing.njts.tmp");
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        FileOutputStream outputStream = new FileOutputStream(f);
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
        f.deleteOnExit();

        Mockito.when(cursor.getColumnIndex(DownloadManager.COLUMN_ID)).thenAnswer((invocation) -> 0);
        Mockito.when(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)).thenAnswer((invocation) -> 1);
        Mockito.when(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)).thenAnswer((invocation) -> 2);
        Mockito.when(cursor.getColumnIndex(DownloadManager.COLUMN_URI)).thenAnswer((invocation) -> 3);


        Mockito.when(cursor.getInt(0)).thenAnswer((i) -> queryData.getID());
        Mockito.when(cursor.getInt(1)).thenAnswer((i) -> queryData.getStatus());
        Mockito.when(cursor.getString(2)).thenAnswer((i) -> f.toURI().toASCIIString()); // local file if successful.
        Mockito.when(cursor.getString(3)).thenAnswer((i) -> url);

        PowerMockito.mockStatic(Uri.class);
        Uri uri = Mockito.mock(Uri.class);
        Mockito.when(uri.getPath()).thenReturn(f.toString());
        PowerMockito.when(Uri.parse(f.toURI().toASCIIString())).thenReturn(uri);

        Mockito.when(cursor.moveToNext()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
               queryData.setIndex(queryData.getIndex() +1);

               return cursor.isAfterLast();
            }
        });
        Mockito.when(cursor.isAfterLast()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (queryData.getIndex()>= queryData.size()) {
                    return true;
                }
                return false;
            }
        });

        reciver.onReceive(context, intent);

    }


    @Test
    public void downloadTesFail() throws IOException {
        DownloadManager manager = Mockito.mock(DownloadManager.class);
        Context context = Mockito.mock(Context.class);
        DownloadFile.Callback callback = Mockito.mock(DownloadFile.Callback.class);
        Mockito.when(callback.downloadComplete(Mockito.any(), Mockito.anyLong(), Mockito.any(), Mockito.any())).thenReturn(true);


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
        final int requestid = 12;
        Mockito.when(manager.enqueue(Mockito.any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return requestid;
            }
        });
        downloadFile.downloadFile(url, "title", "description",
                DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI,
                "text/html");

        // called enqued.
        Mockito.verify(manager, Mockito.times(1)).enqueue(Mockito.any());
        Mockito.verify(context, Mockito.times(1)).registerReceiver(Mockito.any(), Mockito.any());

        // the download has been requested
        // call the callback
        Intent intent = Mockito.mock(Intent.class);
        Cursor cursor = Mockito.mock(Cursor.class);
        Mockito.when(manager.query(Mockito.any())).thenReturn(cursor);

        final DownloadDataTable queryData = new DownloadDataTable();
        queryData.add( DownloadManager.STATUS_FAILED, requestid);


        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("NJT NYP Departures.html");

//        File f = File.createTempFile("download_manager", ".testing.njts.tmp");
//        byte[] buffer = new byte[8 * 1024];
//        int bytesRead;
//        FileOutputStream outputStream = new FileOutputStream(f);
//        while ((bytesRead = inputStream.read(buffer)) != -1) {
//            outputStream.write(buffer, 0, bytesRead);
//        }
//        outputStream.close();
//        inputStream.close();
//        f.deleteOnExit();

        Mockito.when(cursor.getColumnIndex(DownloadManager.COLUMN_ID)).thenAnswer((invocation) -> 0);
        Mockito.when(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)).thenAnswer((invocation) -> 1);
        Mockito.when(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)).thenAnswer((invocation) -> 2);
        Mockito.when(cursor.getColumnIndex(DownloadManager.COLUMN_URI)).thenAnswer((invocation) -> 3);


        Mockito.when(cursor.getInt(0)).thenAnswer((i) -> queryData.getID());
        Mockito.when(cursor.getInt(1)).thenAnswer((i) -> queryData.getStatus());
        //Mockito.when(cursor.getString(2)).thenAnswer((i) -> f.toURI().toASCIIString()); // local file if successful.
        Mockito.when(cursor.getString(3)).thenAnswer((i) -> url);

        //PowerMockito.mockStatic(Uri.class);
        //Uri uri = Mockito.mock(Uri.class);
        //Mockito.when(uri.getPath()).thenReturn(f.toString());
        //PowerMockito.when(Uri.parse(f.toURI().toASCIIString())).thenReturn(uri);

        Mockito.when(cursor.moveToNext()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                queryData.setIndex(queryData.getIndex() +1);

                return cursor.isAfterLast();
            }
        });
        Mockito.when(cursor.isAfterLast()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (queryData.getIndex()>= queryData.size()) {
                    return true;
                }
                return false;
            }
        });

        reciver.onReceive(context, intent);

    }
}
