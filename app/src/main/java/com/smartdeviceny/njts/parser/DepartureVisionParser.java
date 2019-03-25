package com.smartdeviceny.njts.parser;

import com.smartdeviceny.njts.utils.Utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.HashMap;
import java.util.List;

public class DepartureVisionParser {
    public HashMap<String, DepartureVisionData> parseDepartureVision(String station_code, Document doc) {
        //Log.d("DV", "parsing Departure vision Doc");
        HashMap<String, DepartureVisionData> result = new HashMap<>();
        try {
            Element table = doc.getElementById("GridView1");
            Node node = table;
            List<Node> child = node.childNodes().get(1).childNodes();
            String header_string = "";
            if (child.size() > 0) {
                // discard the frist 3
                //Log.d("DV", "child ===================== Size:" + child.size());
                Node header = child.get(1);
                List<Node> header_elements = header.childNodes();
                Element h = (Element) header_elements.get(0);
                header_string = h.child(0).html().toString() + " " + h.child(1).html().toString().replace("&nbsp; &nbsp; Select a train to view station stops", "");
                //System.out.println(header_string);
            }
            String tableTime = "0:00 AM";
            {
                Node tr = child.get(1);
                List<Node> td = tr.childNodes().get(0).childNodes();
                String time = ((Element) td.get(1)).html().toString();
                if( time.contains("&nbsp")) {
                    time = time.split("&nbsp")[0];
                }
                tableTime = time;
                System.out.println("Creating time:" + Utils.makeDate(Utils.getTodayYYYYMMDD(null), tableTime, "yyyyMMdd HH:mm a"));
            }
            // discard the frist 3
            //Log.d("DV", "child ===================== Size:" + child.size());
            for (int i = 3; i < child.size(); i++) {
                Node tr = child.get(i);
                List<Node> td = tr.childNodes();
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

                String time = ((Element) td.get(1)).html().toString();
                String to = ((Element) td.get(3)).html().toString();
                String track = ((Element) td.get(5)).html().toString();
                String line = ((Element) td.get(7)).html().toString();
                String train = ((Element) td.get(9)).html().toString();
                String status = ((Element) td.get(11)).html().toString();
                String background = stylemap.get("background-color");
                String foreground = stylemap.get("color");

                data.put("time", time);
                data.put("to", to);
                data.put("track", track);
                data.put("line", line);
                data.put("status", status);
                data.put("train", train);
                data.put("station", station_code);
                data.put("background", background);
                data.put("foreground", foreground);
                //Log.d("DV", "details time:" + time +  " to:" + to + " track:" + track + " line:" + line + " status:" + status + " train:" + train + " station:" + station );
                DepartureVisionData dv = new DepartureVisionData(data);
                dv.tableTime = tableTime;
                result.put(dv.block_id, dv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.d("DV", "result=" + result.size());
        return result;
    }
}
