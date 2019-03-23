package com.smartdeviceny.njts.utils;

import com.smartdeviceny.njts.annotations.Persist;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
 * Stores an RSS feed
 */
public class Feed {

    @Persist
    String title;
    @Persist
    String link;
    @Persist
    String description;
    @Persist
    String language;
    @Persist
    String copyright;
    @Persist
    String pubDate;
    @Persist
    final ArrayList<FeedMessage> entries = new ArrayList<FeedMessage>();
        public  Feed() {

        }
    public Feed(String title, String link, String description, String language, String copyright, String pubDate) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.language = language;
        this.copyright = copyright;
        this.pubDate = pubDate;
    }

    public ArrayList<FeedMessage> getMessages() {
        return entries;
    }

    public void setMessages(List<FeedMessage> msgs) {
        entries.clear();
        for (FeedMessage msg : msgs) {
            entries.add(msg);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public String getLanguage() {
        return language;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getPubDate() {
        return pubDate;
    }

    @Override
    public String toString() {
        return "Feed [copyright=" + copyright + ", description=" + description + ", language=" + language + ", link=" + link + ", pubDate=" + pubDate + ", title=" + title + "]";
    }

    public void marshall(JSONObject packet) {
        try {
            packet.put("title", title);
            packet.put("link", link);
            packet.put("description", description);
            packet.put("language", language);
            packet.put("copyright", copyright);
            packet.put("pubDate", pubDate);
            JSONArray messages = new JSONArray();
            for (FeedMessage msg : entries) {
                JSONObject item = new JSONObject();
                msg.marshall(item);
                messages.put(messages.length(), item);
            }
            packet.put("messages", messages);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void unmarshall(JSONObject packet) {
        try {

            if (packet.has("title")) {
                title = packet.getString("title");
            }
            if (packet.has("link")) {
                link = packet.getString("link");
            }
            if (packet.has("description")) {
                description = packet.getString("description");
            }
            if (packet.has("language")) {
                language = packet.getString("language");
            }
            if (packet.has("copyright")) {
                copyright = packet.getString("copyright");
            }
            if (packet.has("pubDate")) {
                pubDate = packet.getString("pubDate");
            }
            if (packet.has("messages")) {
                JSONArray messages = packet.getJSONArray("messages");
                for(int i=0; i < messages.length(); i ++) {
                    JSONObject obj = messages.getJSONObject(i);
                    FeedMessage item = new FeedMessage();
                    item.unmarshall(obj);
                    entries.add(item);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}