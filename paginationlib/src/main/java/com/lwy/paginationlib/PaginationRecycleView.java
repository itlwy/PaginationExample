package com.lwy.paginationlib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaginationRecycleView extends LinearLayout implements PaginationController.OnChangedListener {

    public static final int SUCCESS = 0;
    public static final int FAILED = 1;
    public static final int EMPTY = 2;
    private RecyclerView mRecycleView;
    private PaginationController mPaginationControllerView;
    private Adapter mAdapter;
    private Listener mListener;
    private ProgressBar mProgressBar;

    public void setmListener(Listener mListener) {
        this.mListener = mListener;
    }

    public PaginationRecycleView(Context context) {
        super(context);
        init();
    }

    public PaginationRecycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaginationRecycleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View.inflate(getContext(), R.layout.pagination_layout, this);
        mRecycleView = findViewById(R.id.rcv);
        mPaginationControllerView = findViewById(R.id.controller);
        mProgressBar = findViewById(R.id.progress);
    }

    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
        mRecycleView.setAdapter(mAdapter.getmInnerAdapter());
        mAdapter.mPaginationRecycleView = this;

        mPaginationControllerView.setmListener(this);
        setTotal(mAdapter.mDataTotal);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mRecycleView.setLayoutManager(layoutManager);
    }

    public void setPerPageCountChoices(int[] perPageCountChoices) {
        mPaginationControllerView.setPerPageCountChoices(perPageCountChoices);
    }

    public void setState(int flag) {
        switch (flag) {
            case SUCCESS:

                break;
            case EMPTY:
            case FAILED:
                if (mAdapter.mLastPagePos > 0) {
                    mPaginationControllerView.skip2Pos(mAdapter.mLastPagePos);
                    mAdapter.mCurrentPagePos = mAdapter.mLastPagePos;
                }
                break;
        }
        setLoading(false);
    }


    private void setLoading(boolean flag) {
        if (flag) {
            mProgressBar.setVisibility(VISIBLE);
        } else {
            mProgressBar.setVisibility(GONE);
        }
    }

    private void setTotal(int total) {
        mPaginationControllerView.setTotalCount(total);
    }

    @Override
    public void onPageSelectedChanged(int currentPapePos, int lastPagePos, int totalPageCount, int total) {
        if (currentPapePos > lastPagePos) {
            mAdapter.nextSkip(currentPapePos, lastPagePos);
        } else {
            mAdapter.lastSkip(currentPapePos, lastPagePos);
        }
    }

    @Override
    public void onPerPageCountChanged(int perPageCount) {
        mAdapter.onPerPageCountChanged(perPageCount);
        if (mListener != null)
            mListener.onPerPageCountChanged(perPageCount);
    }

    public interface Listener {
        void loadMore(int currentPagePosition, int nextPagePosition, int perPageCount, int dataTotalCount);

        void onPerPageCountChanged(int perPageCount);
    }

    public static abstract class Adapter<T, VH extends ViewHolder> {
        private PaginationRecycleView mPaginationRecycleView;
        private Map<Integer, List<T>> mDataMap;
        private List<T> mShowList;
        private int mCurrentPagePos;
        private int mPerPageCount;
        private int mDataTotal;
        private InnerAdapter mInnerAdapter;
        private int mLastPagePos;
        private OnItemClickListener mOnItemClickListener;

        public List<T> getDataByPage(int pagePos) {
            return mDataMap.get(pagePos);
        }

        public int getCurrentPagePos() {
            return mCurrentPagePos;
        }

        public int getPerPageCount() {
            return mPerPageCount;
        }

        public Adapter(int dataTotalCount) {
            mInnerAdapter = new InnerAdapter();
            mDataMap = new HashMap<>();
            setDataTotalCount(dataTotalCount);
        }

        public InnerAdapter getmInnerAdapter() {
            return mInnerAdapter;
        }


        public void setDataTotalCount(int total) {
            mDataTotal = total;
            if (mPaginationRecycleView != null)
                mPaginationRecycleView.setTotal(total);
            clear();
        }

        public void setDatas(int pagePos, List<T> datas) {
            mDataMap.put(pagePos, datas);
            notifyDataSetChanged();
        }

        public void clear() {
            mDataMap.clear();
            notifyDataSetChanged();
        }

        public T getCurrentPageItem(int position) {
            return mShowList != null ? mShowList.get(position) : null;
        }

        public void nextSkip(int nextPost, int lastPos) {
            mCurrentPagePos = nextPost;
            mLastPagePos = lastPos;
            if (checkIfNeedLoadMore()) {
                mPaginationRecycleView.setLoading(true);
                if (mPaginationRecycleView.mListener != null) {
                    mPaginationRecycleView.mListener.loadMore(mCurrentPagePos - 1, mCurrentPagePos, mPerPageCount, mDataTotal);
                }
            } else {
                notifyDataSetChanged();
            }
        }

        public void lastSkip(int nextPost, int lastPos) {
            mCurrentPagePos = nextPost;
            mLastPagePos = lastPos;
            notifyDataSetChanged();
        }

        private boolean checkIfNeedLoadMore() {
            return !mDataMap.containsKey(mCurrentPagePos);
        }

        public void notifyDataSetChanged() {
            mShowList = mDataMap.get(mCurrentPagePos);
            mInnerAdapter.notifyDataSetChanged();
        }

        public void onBindViewHolder(@NonNull VH viewholder, int position) {
            bindViewHolder(viewholder, mShowList.get(position));
        }

        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            VH holder = createViewHolder(parent, viewType);
            setListener(parent, holder, viewType);
            return holder;
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public int getItemCount() {
            return mShowList == null ? 0 : mShowList.size();
        }

        protected void setListener(final ViewGroup parent, final ViewHolder viewHolder, int viewType) {
            if (!isEnabled(viewType)) return;
            viewHolder.getConvertView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        int position = viewHolder.getAdapterPosition();
                        mOnItemClickListener.onItemClick(v, viewHolder, position);
                    }
                }
            });

            viewHolder.getConvertView().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnItemClickListener != null) {
                        int position = viewHolder.getAdapterPosition();
                        return mOnItemClickListener.onItemLongClick(v, viewHolder, position);
                    }
                    return false;
                }
            });
        }

        protected boolean isEnabled(int viewType) {
            return true;
        }

        public abstract void bindViewHolder(VH viewholder, T data);

        public abstract VH createViewHolder(@NonNull ViewGroup parent, int viewTypea);

        /**
         * 每页多少条选项被改变时触发
         *
         * @param perPageCount
         */
        void onPerPageCountChanged(int perPageCount) {
            mCurrentPagePos = 1;
            mLastPagePos = 0;
            mPerPageCount = perPageCount;
            mDataMap.clear();
            notifyDataSetChanged();
        }

        public interface OnItemClickListener {
            void onItemClick(View view, RecyclerView.ViewHolder holder, int position);

            boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position);
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        class InnerAdapter extends RecyclerView.Adapter<VH> {

            @NonNull
            @Override
            public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return Adapter.this.onCreateViewHolder(viewGroup, i);
            }

            @Override
            public void onBindViewHolder(@NonNull VH vh, int i) {
                Adapter.this.onBindViewHolder(vh, i);
            }

            @Override
            public int getItemCount() {
                return Adapter.this.getItemCount();
            }

            @Override
            public int getItemViewType(int position) {
                return Adapter.this.getItemViewType(position);
            }
        }
    }

}
