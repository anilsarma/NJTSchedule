package com.smartdeviceny.njts.adapters;


import androidx.annotation.LayoutRes;

import com.smartdeviceny.njts.lib.RecyclerHeader;

public class NJTScheduleHeader implements RecyclerHeader {
    public static final int HEADER_TYPE_1 = 1;
   // public static final int HEADER_TYPE_2 = 2;

    private int headerType;
    @LayoutRes
    private final int layoutResource;

    String routeName;
    String title;

    public NJTScheduleHeader(int headerType, @LayoutRes int layoutResource) {
        this.layoutResource = layoutResource;
        this.headerType = headerType;

    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @LayoutRes
    @Override
    public int getHeaderLayout() {
        return layoutResource;
    }

    @Override
    public int getHeaderType() {
        return headerType;
    }
}