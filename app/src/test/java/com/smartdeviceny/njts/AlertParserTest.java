package com.smartdeviceny.njts;

import com.smartdeviceny.njts.utils.Feed;
import com.smartdeviceny.njts.utils.FeedMessage;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AlertParserTest {
    @Test
    public  void maieeeen() {
        //Jul 23, 2018 09:12:30 AM -- MMM d, yyyy HH:mm:ss a
        String format = "MMM d, yyyy HH:mm:ss a";
        DateFormat dateTimeFormat = new SimpleDateFormat(format);
        //ArrayList<Record> alerts = new ArrayList<Record>();
        HashMap<String, String> parent = new HashMap<>();
        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream("RailAdvisories_feed.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(stream);
            NodeList nodes = document.getChildNodes();
            Node node = nodes.item(0).getChildNodes().item(0);
            nodes = node.getChildNodes();
            Date now = new Date();
            Calendar date = new GregorianCalendar();
// reset hour, minutes, seconds and millis
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            now = date.getTime();
            List<FeedMessage> array = new ArrayList<>();

            for(int i=0;i < nodes.getLength(); i ++ ) {
                //System.out.println(nodes.item(i).getNodeName() + "=" + nodes.item(i).getFirstChild().getNodeValue());
                String name = nodes.item(i).getNodeName();
                String value = nodes.item(i).getFirstChild().getNodeValue();
                if(name !=null && value!=null) {
                    parent.put(name, value);
                }
                if ( nodes.item(i).getNodeName().equals("item")) {
                    NodeList items = nodes.item(i).getChildNodes();
                    String descrition = null;
                    String title = null;
                    Date  dt =null;
                    HashMap<String, String> item = new HashMap<>();
                    for(int j=0; j < items.getLength();j ++ ) {
                        String nm = items.item(j).getNodeName();
                        String v = items.item(j).getFirstChild().getNodeValue();
                        //if(nm !=null && v !=null) {
                            item.put(nm, v);
                        //}
                        //System.out.println("\t\t" + nm + "=" + v );
                        if(items.item(j).getNodeName().equals("title")) {
                            title = items.item(j).getFirstChild().getNodeValue();
                            dt = dateTimeFormat.parse(title);

                        } else  if(items.item(j).getNodeName().equals("description")) {
                            descrition = items.item(j).getFirstChild().getNodeValue();
                           // System.out.println("\t\t" + items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                    FeedMessage f = new FeedMessage();
                    f.title = item.get("title");
                    f.description = item.get("description");
                    f.link = item.get("link");
                    f.author = item.get("author");
                    f.guid = item.get("guid"); //pubDate
                    f.pubDate = item.get("pubDate"); //

                    //array.put(item);
                    //System.out.println(f);
                    array.add(f);
                    if (dt.after(now)) {
                        //System.out.println(title + " " + descrition);
                        //alerts.add( new Record(dt, descrition));
                    }
                }
            }

            Feed feed = new Feed(parent.get("title"), parent.get("link"), parent.get("description"),
                    parent.get("language"), parent.get("copyright"), parent.get("pubDate"));
            feed.setMessages(array);
            System.out.println(feed);
            DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss a");
            //Date now = new Date();
            for(FeedMessage msg:feed.getMessages()) {
                Date dt = dateFormat.parse(msg.title);
                if (dt.getTime() > (now.getTime() - TimeUnit.DAYS.toMillis(5))) {
                    System.out.println(msg);
                }
            }
//
//            if(array.length()>0) {
//                parent.put("items", array);
//            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(parent.toString());
    }
}
