package com.smartdeviceny.njts.parser;

import com.smartdeviceny.njts.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DepartureVisionParser {
    public HashMap<String, DepartureVisionData> parseDepartureVision(String station_code, String long_name, Document doc) {
        //Log.d("DV", "parsing Departure vision Doc");
        HashMap<String, DepartureVisionData> result = new HashMap<>();
        try {
            Element table = doc.getElementById("GridView1");
            Node node = table;
            Elements child =table.getElementsByTag("tr");
            String header_string = "";
            if (child.size() > 0) {
                // discard the frist 3
                //Log.d("DV", "child ===================== Size:" + child.size());
                Node header = child.get(1);
                Elements header_elements = child.get(1).getElementsByTag("td");
                Element h = (Element) header_elements.get(0);
                //header_string = h.child(0).html().toString() + " " + h.child(1).html().toString().replace("&nbsp; &nbsp; Select a train to view station stops", "");
                //System.out.println(header_string);
            }
            String tableTime = "0:00 AM";
            int startIndex = 2;
            {
                //Node tr = child.get(1);
                //List<Node> td = tr.childNodes().get(0).childNodes();
                Elements e = child.get(0).getElementsByTag("div");
                if( e.size()<2){
                    startIndex ++;
                    e = child.get(1).getElementsByTag("div");
                }
                String time = e.get(1).html().toString();
                if( time.contains("&nbsp")) {
                    time = time.split("&nbsp")[0];
                }
                tableTime = time;
                System.out.println("Creating time:" + Utils.makeDate(Utils.getTodayYYYYMMDD(null), tableTime, "yyyyMMdd HH:mm a"));
            }
            // discard the frist 3
            //Log.d("DV", "child ===================== Size:" + child.size());
            int index = 0;
            for (int i = startIndex; i < child.size(); i++) {
                Node tr = child.get(i);
                Elements td = child.get(i).getElementsByTag("td");
                //Log.d("DV", "childNodes(td) ===================== Size:" + td.size());

                if (td.size() < 4) {
                    continue;
                }
                HashMap<String, Object> data = new HashMap<>();
                HashMap<String, String> stylemap = new HashMap<>();

                try {
                    String style[] = ((Element) tr).attr("style").split(";");
                    for (String s : style) {
                        String nvp[] = s.split(":");
                        stylemap.put(nvp[0], nvp[1]);
                    }
                } catch (Exception e) {

                }

                String time = ((Element) td.get(0)).html().toString();
                String to = Jsoup.parse(td.get(1).toString()).text();
                String track = ((Element) td.get(2)).html().toString();
                String line = ((Element) td.get(3)).html().toString();
                String train = ((Element) td.get(4)).html().toString();
                String status = Jsoup.parse(td.get(5).toString()).text();
                String background = stylemap.get("background-color");
                String foreground = stylemap.get("color");

                line = line.replace("Corrdr", "Corridor").replace("-", " ").replace("No J", "North J");
                data.put("time", time);
                data.put("to", to);
                data.put("track", track);
                data.put("line", line);
                data.put("status", status);
                data.put("train", train);
                data.put("station", station_code);
                data.put("station_long_name", long_name);
                data.put("background", background);
                data.put("foreground", foreground);
                data.put("index", index++);
                //Log.d("DV", "details time:" + time +  " to:" + to + " track:" + track + " line:" + line + " status:" + status + " train:" + train + " station:" + station );
                DepartureVisionData dv = new DepartureVisionData(data);
                dv.tableTime = tableTime;
                dv.createTime  = new Date();

                try {
                    Date tm = Utils.makeDate(Utils.getTodayYYYYMMDD(null), dv.time, "yyyyMMdd HH:mm");
                    long diff = dv.createTime.getTime() - tm.getTime();
                    if (TimeUnit.MILLISECONDS.toHours(Math.abs(diff)) > 7) {
                        // need adjustment
                        if (tm.getTime() < dv.createTime.getTime()) {
                            tm = new Date(tm.getTime() + TimeUnit.HOURS.toMillis(12));
                        } else {
                            // rea
                            tm = new Date(tm.getTime() - TimeUnit.HOURS.toMillis(12));
                        }
                    }
                    dv.adjusted_time = tm;
                }catch (Exception e) {
                    e.printStackTrace();;
                }
                result.put(dv.block_id, dv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.d("DV", "result=" + result.size());
        return result;
    }
}
