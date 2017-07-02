package com.qsmaxmin.qsbase.mvp.fragment;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.qsmaxmin.qsbase.R;
import com.qsmaxmin.qsbase.common.aspect.ThreadPoint;
import com.qsmaxmin.qsbase.common.aspect.ThreadType;
import com.qsmaxmin.qsbase.common.log.L;
import com.qsmaxmin.qsbase.common.widget.listview.LoadingFooter;
import com.qsmaxmin.qsbase.common.widget.recyclerview.HeaderFooterRecyclerView;
import com.qsmaxmin.qsbase.mvp.adapter.MyRecycleViewHolder;
import com.qsmaxmin.qsbase.mvp.adapter.QsRecycleAdapterItem;
import com.qsmaxmin.qsbase.mvp.presenter.QsPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * @CreateBy qsmaxmin
 * @Date 16/8/5
 * @Description RecyclerView视图
 */
public abstract class QsRecyclerFragment<T extends QsPresenter, D> extends QsFragment<T> implements QsIRecyclerFragment<D>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final int     TYPE_LIST          = 1 << 2;
    public static final int     TYPE_GRID          = 2 << 2;
    public static final int     TYPE_STAGGEREDGRID = 3 << 2;
    private final       List<D> mList              = new ArrayList<>();

    private   HeaderFooterRecyclerView   mRecyclerView;
    protected MyRecycleAdapter           mRecyclerViewAdapter;
    protected LoadingFooter              mLoadingFooter;
    protected StaggeredGridLayoutManager staggeredGridLayoutManager;

    @Override public int layoutId() {
        return R.layout.qs_fragment_recycleview;
    }

    @Override public int getHeaderLayout() {
        return 0;
    }

    @Override public int getFooterLayout() {
        return 0;
    }

    @Override public MyRecycleAdapter onCreateAdapter() {
        return null;
    }

    @Override public HeaderFooterRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override protected View initView(LayoutInflater inflater) {
        View view = super.initView(inflater);
        initRecycleView(inflater, view);
        return view;
    }

    /**
     * 初始化RecycleView
     */
    protected void initRecycleView(LayoutInflater inflater, View view) {
        if (view instanceof HeaderFooterRecyclerView) {
            mRecyclerView = (HeaderFooterRecyclerView) view;
        } else {
            mRecyclerView = (HeaderFooterRecyclerView) view.findViewById(android.R.id.list);
        }
        if (mRecyclerView == null) throw new RuntimeException("HeaderFooterRecyclerView is not exit or its id not 'android.R.id.list' in current layout!!");
        if (getHeaderLayout() > 0) {
            View headerView = inflater.inflate(getHeaderLayout(), null);
            if (headerView != null) {
                mRecyclerView.addHeaderView(headerView);
                L.i(initTag(), ".........  buttonKnife 注解：" + headerView);
                ButterKnife.bind(this, headerView);
            }
        }
        if (getFooterLayout() > 0) {
            View footerView = inflater.inflate(getFooterLayout(), null);
            if (footerView != null) {
                if (footerView instanceof LoadingFooter) {
                    mLoadingFooter = (LoadingFooter) footerView;
                } else {
                    mLoadingFooter = (LoadingFooter) footerView.findViewById(R.id.loading_footer);
                }
                mRecyclerView.addFooterView(footerView);
            }
        }

        mRecyclerViewAdapter = onCreateAdapter();
        if (mRecyclerViewAdapter == null) {
            mRecyclerViewAdapter = new MyRecycleAdapter(inflater);
        }
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        switch (getRecyclerViewType()) {
            case TYPE_LIST:
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                break;

            case TYPE_GRID:
                final GridLayoutManager manager = new GridLayoutManager(getContext(), getSpanCount());
                manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override public int getSpanSize(int position) {
                        /*当有footer或者header时特殊处理，让它占满整个一条*/
                        L.i(initTag(), "getSpanSize   position:" + position);
                        if (getHeaderLayout() > 0 && position == 0) {
                            return getSpanCount();
                        } else if (getHeaderLayout() > 0 && getFooterLayout() > 0 && position == mList.size() + 1) {
                            return getSpanCount();
                        } else if (getHeaderLayout() == 0 && getFooterLayout() > 0 && position == mList.size()) {
                            return getSpanCount();
                        } else {
                            return 1;
                        }
                    }
                });
                mRecyclerView.setLayoutManager(manager);
                break;

            case TYPE_STAGGEREDGRID:
                staggeredGridLayoutManager = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
                /*顶部不留白*/
                staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
                mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
                break;
        }

    }

    @Override public MyRecycleAdapter getAdapter() {
        return mRecyclerViewAdapter;
    }

    @Override @ThreadPoint(ThreadType.MAIN) public void setData(List<D> list) {
        synchronized (mList) {
            mList.clear();
            if (list != null && !list.isEmpty()) mList.addAll(list);
            updateAdapter();
        }
    }

    @Override @ThreadPoint(ThreadType.MAIN) public void addData(List<D> list) {
        if (list != null && !list.isEmpty()) {
            synchronized (mList) {
                mList.addAll(list);
                updateAdapter();
            }
        }
    }

    @Override @ThreadPoint(ThreadType.MAIN) public void addData(List<D> list, int position) {
        if (list != null && !list.isEmpty() && position >= 0) {
            synchronized (mList) {
                position = (position < mList.size()) ? position : mList.size();
                if (mRecyclerViewAdapter != null) mRecyclerViewAdapter.notifyItemRangeInserted(position, list.size());
                mList.addAll(position, list);
                updateAdapter();
            }
        }
    }

    @Override @ThreadPoint(ThreadType.MAIN) public void delete(int position) {
        synchronized (mList) {
            if (position >= 0 && position < mList.size()) {
                if (mRecyclerViewAdapter != null) mRecyclerViewAdapter.notifyItemRemoved(position);
                mList.remove(position);
                updateAdapter();
            }
        }
    }

    @Override @ThreadPoint(ThreadType.MAIN) public void deleteAll() {
        synchronized (mList) {
            mList.clear();
            updateAdapter();
        }
    }

    @Override public final List<D> getData() {
        return mList;
    }

    @Override @ThreadPoint(ThreadType.MAIN) public void updateAdapter() {
        if (mRecyclerViewAdapter != null) {
            if (mViewAnimator != null) {
                if (mList.isEmpty()) {
                    showEmptyView();
                } else {
                    showContentView();
                }
            }
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }


    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }


    /**
     * 适配器
     */
    public class MyRecycleAdapter extends RecyclerView.Adapter {
        private final LayoutInflater mInflater;

        MyRecycleAdapter(LayoutInflater inflater) {
            this.mInflater = inflater;
        }

        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            QsRecycleAdapterItem recycleAdapterItem = getRecycleAdapterItem(mInflater, parent, viewType);
            recycleAdapterItem.getViewHolder().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    QsRecyclerFragment.this.onItemClick(parent, view, position, id);
                }
            });
            recycleAdapterItem.getViewHolder().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    return QsRecyclerFragment.this.onItemLongClick(parent, view, position, id);
                }
            });
            return recycleAdapterItem.getViewHolder();
        }

        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyRecycleViewHolder<Object> j2WRecycleViewHolder = (MyRecycleViewHolder) holder;
            j2WRecycleViewHolder.setPosition(position, getItemCount());
            j2WRecycleViewHolder.onBindData(mList.get(position), position, mList.size());
        }

        @Override public int getItemViewType(int position) {
            return getItemViewType(position);
        }

        @Override public int getItemCount() {
            return mList.size();
        }
    }

    protected int getSpanCount() {
        return 2;
    }

    protected int getRecyclerViewType() {
        return TYPE_LIST;
    }

    @Override public int getItemViewType(int position) {
        return 0;
    }
}