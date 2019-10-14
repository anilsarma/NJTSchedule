package com.smartdeviceny.njts.adapters;

import com.smartdeviceny.njts.lib.RecyclerMainItemData;
import com.smartdeviceny.njts.parser.Route;

public class NJTScheduleData implements RecyclerMainItemData {
    Route rt;
    public NJTScheduleData(Route rt) {
        this.rt = rt;
    }
    public String getTitle() {
        return rt.block_id;
    }
}