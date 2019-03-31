package com.smartdeviceny.njts.adapters;


import android.support.annotation.LayoutRes;

import com.smartdeviceny.njts.lib.RecyclerHeader;
import com.smartdeviceny.njts.lib.RecyclerMainItemData;
import com.smartdeviceny.njts.parser.DepartureVisionData;

public class RecycleDepartureVisionData implements RecyclerMainItemData {
    public static final int HEADER_TYPE_1 = 1;
    public DepartureVisionData stop;


    public RecycleDepartureVisionData(DepartureVisionData dv) {
       this.stop = dv;

    }


}