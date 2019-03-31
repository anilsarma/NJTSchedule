package com.smartdeviceny.njts.lib;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class StickHeaderRecyclerView<D extends RecyclerMainItemData, H extends RecyclerHeader> extends RecyclerView.Adapter implements StickyHeaderInterface {
    private List<RecyclerMainItemData> mData = new ArrayList<>();

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        StickHeaderItemDecoration stickHeaderDecoration = new StickHeaderItemDecoration(recyclerView.getContext(), this);
        recyclerView.addItemDecoration(stickHeaderDecoration);
    }

    @Override
    public final int getItemViewType(int position) {
        if (mData.get(position) instanceof RecyclerHeader) {
            return ((RecyclerHeader) mData.get(position)).getHeaderType();
        }
        return getViewType(position);
    }

    @Override
    public boolean isHeader(int itemPosition) {
        return mData.get(itemPosition) instanceof RecyclerHeader;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getHeaderLayout(int headerPosition) {
        return ((RecyclerHeader) mData.get(headerPosition)).getHeaderLayout();
    }

    @Override
    public int getHeaderPositionForItem(int itemPosition) {
        int headerPosition = 0;
        do {
            if (this.isHeader(itemPosition)) {
                headerPosition = itemPosition;
                break;
            }
            itemPosition -= 1;
        } while (itemPosition >= 0);
        return headerPosition;
    }

    public void setHeaderAndData(@NonNull List<D> datas, @Nullable RecyclerHeader header) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        if (header != null) {
            mData.add(header);
        }
        mData.addAll(datas);
    }

    public void clear() {
        mData.clear();
    }
    public Object createDataContext() {
        return new ArrayList<RecyclerMainItemData>();
    }
    public void addHeader(Object dataContext, RecyclerHeader header) {
        ArrayList<RecyclerMainItemData> data = (ArrayList<RecyclerMainItemData>)dataContext;
        if (data == null) {
            data = new ArrayList<>();
        }

        data.add(header);
    }
    public void addItem(Object dataContext, D item) {
        ArrayList<RecyclerMainItemData> data = (ArrayList<RecyclerMainItemData>)dataContext;
        if (data == null) {
            data = new ArrayList<>();
        }
        data.add(item);
    }
    public void updateDataContext(Object dataContext) {
        ArrayList<RecyclerMainItemData> data = (ArrayList<RecyclerMainItemData>)dataContext;
        mData = data; //
    }
    public void addHeader(RecyclerHeader header) {
        if (mData == null) {
            mData = new ArrayList<>();
        }

        mData.add(header);
    }
    public void addItem(D data) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        mData.add(data);
    }
    protected int getViewType(int pos){
        return 0;
    }

    public D getDataInPosition(int position) {
        return (D) mData.get(position);
    }

    public H getHeaderDataInPosition(int position) {
        return (H) mData.get(position);
    }
}