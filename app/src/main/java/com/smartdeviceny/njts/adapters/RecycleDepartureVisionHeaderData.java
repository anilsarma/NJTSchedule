package com.smartdeviceny.njts.adapters;

import android.support.annotation.LayoutRes;

import com.smartdeviceny.njts.lib.RecyclerHeader;
import com.smartdeviceny.njts.lib.RecyclerMainItemData;
import com.smartdeviceny.njts.parser.Route;

public class RecycleDepartureVisionHeaderData implements RecyclerHeader {
    public static final int HEADER_TYPE = 1;
    private int headerType;
    @LayoutRes
    private final int layoutResource;
    String title;
    String subtitle;

    public RecycleDepartureVisionHeaderData(int headerType, @LayoutRes int layoutResource) {
        this.headerType = headerType;
        this.layoutResource = layoutResource;
    }

    @Override
    public int getHeaderLayout() {
        return layoutResource;
    }

    @Override
    public int getHeaderType() {
        return headerType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}