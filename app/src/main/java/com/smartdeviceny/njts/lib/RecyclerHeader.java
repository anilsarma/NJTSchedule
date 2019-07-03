package com.smartdeviceny.njts.lib;

import androidx.annotation.LayoutRes;

public interface RecyclerHeader extends RecyclerMainItemData {
    @LayoutRes
    int getHeaderLayout();
    int getHeaderType();
}
