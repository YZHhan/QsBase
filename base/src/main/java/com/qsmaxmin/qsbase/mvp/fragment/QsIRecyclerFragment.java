package com.qsmaxmin.qsbase.mvp.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.qsmaxmin.qsbase.common.widget.recyclerview.HeaderFooterRecyclerView;
import com.qsmaxmin.qsbase.mvp.adapter.QsRecycleAdapterItem;

import java.util.List;

/**
 * @CreateBy qsmaxmin
 * @Date 17/7/2  上午10:58
 * @Description
 */

public interface QsIRecyclerFragment<D> extends QsIFragment {

    QsRecycleAdapterItem getRecycleAdapterItem(LayoutInflater mInflater, ViewGroup parent, int type);

    int getHeaderLayout();

    int getFooterLayout();

    HeaderFooterRecyclerView getRecyclerView();

    int getItemViewType(int position);

    void setData(List<D> list);

    List<D> getData();

    void addData(List<D> list);

    void addData(List<D> data, int position);

    void delete(int position);

    void deleteAll();

    void updateAdapter();

    QsRecyclerFragment.MyRecycleAdapter onCreateAdapter();

    QsRecyclerFragment.MyRecycleAdapter getAdapter();
}