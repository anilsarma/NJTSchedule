package com.smartdeviceny.njts.lib;

import android.support.annotation.LayoutRes;

public interface RecyclerHeader extends RecyclerMainItemData {
    @LayoutRes
    int getHeaderLayout();
    int getHeaderType();
}
