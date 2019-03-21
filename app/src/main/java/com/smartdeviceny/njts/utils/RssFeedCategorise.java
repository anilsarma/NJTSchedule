package com.smartdeviceny.njts.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RssFeedCategorise {
    static DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");

    public class Category {
        public ArrayList<RailAlertDetails> train = new ArrayList<>();
        public ArrayList<RailAlertDetails> construction = new ArrayList<>();
        public ArrayList<RailAlertDetails> service = new ArrayList<>();

        public void sort() {
            train.sort(Comparator.comparingLong(RailAlertDetails::getTime));
            construction.sort(Comparator.comparingLong(RailAlertDetails::getTime));
            service.sort(Comparator.comparingLong(RailAlertDetails::getTime));
        }
    }

    public static String getLongName(String short_code) {
        switch (short_code) {
            case "NEC":
                return "Northeast Corridor";
            case "NJCL":
                return "North Jersey Coast";
            case "RARV":
                return "Raritan Valley";
            case "MNE":
                return "Morristown Line";
            case "MNBN":
                return "Main/Bergen-Port Jervis Line";
            case "BNTN":
                return "Montclair-Boonton";
            case "PASC":
                return "Pascack Valley";
            case "ATLC":
                return "Atlantic City";
            case "HBLR":
                return "Hudson-Bergen Light Rail";
            case "NLR":
                return "Newark Light Rail";
            case "RVR":
                return "River Line";
        }
        return short_code;
    }
    public static String getShortName(String long_name) {
        switch (long_name) {
            case "Northeast Corridor":
                return "NEC";
            case "North Jersey Coast":
                return "NJCL";
            case "Raritan Valley":
                return "RARV";
            case "Morristown Line":
                return "MNE";
            case "Main/Bergen-Port Jervis Line":
                return "MNBN";
            case "Montclair-Boonton":
                return "BNTN";
            case "Pascack Valley":
                return "PASC";
            case "Atlantic City":
                return "ATLC";
            case "Hudson-Bergen Light Rail":
                return "HBLR";
            case "Newark Light Rail":
                return "NLR";
            case "River Line":
                return "RVR";
        }
        return long_name;
    }
    public Category categorize(Feed feed, long cutoffTime) {
        Category category = new Category();

        for (FeedMessage msg : feed.getMessages()) {
            //TravelAlertsTo&rel=Rail&selLine=MNE#RailTab
            long msgTime = 0;
            try {
                msgTime = dateFormat.parse(msg.pubDate).getTime();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (msg.link.contains("TravelAlertsTo")) {
                Matcher matcher = Pattern.compile("=Rail&selLine=(.+?)#RailTab").matcher(msg.link);
                if (matcher.find()) {
                    String long_name = getLongName(matcher.group(1));
                    String short_code = matcher.group(1);
                    RailAlertDetails alert = new RailAlertDetails(long_name, short_code, true, msg.description);
                    alert.setTime(msgTime);
                    category.train.add(alert);
                }
            } else if (msg.link.contains("ConstructionAdvisoryTo")) {
                RailAlertDetails alert = new RailAlertDetails("", "", true, msg.description);
                alert.setTime(msgTime);
                category.construction.add(alert);
            } else if (msg.link.contains("ServiceAdjustmentTo")) {
                RailAlertDetails alert = new RailAlertDetails("", "", true, msg.description);
                alert.setTime(msgTime);
                category.service.add(alert);
            } else {
                System.out.println(msg.link);
            }
        }
        System.out.println(category.train);
        return category;
    }
}
