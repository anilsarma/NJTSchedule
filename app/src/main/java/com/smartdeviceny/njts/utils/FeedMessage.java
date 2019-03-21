package com.smartdeviceny.njts.utils;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FeedMessage {
    final static String format = "MMM d, yyyy HH:mm:ss a";
    final static DateFormat dateTimeFormat = new SimpleDateFormat(format);
    public String title;
    public String description;
    public String link;
    public String author;
    public String guid;
    public String pubDate;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public String toString() {
        return "FeedMessage [title=" + title + ", description=" + description + ", link=" + link + ", author=" + author + ", guid=" + guid + "]";
    }

    public void marshall(JSONObject packet) {
        try {
            packet.put("title", title);
            packet.put("description", description);
            packet.put("link", link);
            packet.put("author", author);
            packet.put("guid", guid);
            packet.put("pubDate", pubDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Date getPubDate() {
        try {
            return dateTimeFormat.parse(pubDate);
        } catch (Exception e) {
            e.printStackTrace();
            return new Date();// return now.
        }
    }
    public void unmarshall(JSONObject packet) {
        try {
            if( packet.has("title")) {
                title = packet.getString("title");
            }
            if( packet.has("description")) {
            description = packet.getString("description");
            }
            if( packet.has("link")) {
            link = packet.getString("link");
            }
            if( packet.has("author")) {
            author = packet.getString("author");
            }
            if( packet.has("guid")) {
            guid = packet.getString("guid");
            }
            if( packet.has("pubDate")) {
                pubDate = packet.getString("pubDate");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}