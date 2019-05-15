package com.lwy.paginationlib;

import android.content.Context;
import android.content.res.TypedArray;
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

import static com.lwy.paginationlib.PaginationIndicator.dp2px;
import static com.lwy.paginationlib.PaginationIndicator.sp2px;

public class PaginationRecycleView extends LinearLayout implements PaginationIndicator.OnChangedListener {

    public static final int SUCCESS = 0;
    public static final int FAILED = 1;
    public static final int EMPTY = 2;
    private RecyclerView mRecycleView;
    private PaginationIndicator mPaginationIndicatorView;
    private Adapter mAdapter;
    private Listener mListener;
    private ProgressBar mProgressBar;

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public PaginationRecycleView(Context context) {
        this(context, null);
    }

    public PaginationRecycleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaginationRecycleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PaginationIndicator);
        int color_selected = a.getColor(R.styleable.PaginationIndicator_selected_color, getContext().getResources().getColor(R.color.indicator_rect_selected));
        int color_unselected = a.getColor(R.styleable.PaginationIndicator_unselected_color, getContext().getResources().getColor(R.color.indicator_rect_unselected));
        int numberTipShowCount = a.getInteger(R.styleable.PaginationIndicator_number_tip_count, 0);
        int textSize = a.getDimensionPixelSize(R.styleable.PaginationIndicator_text_size, sp2px(getContext(), 16));
        int width = a.getDimensionPixelSize(R.styleable.PaginationIndicator_rect_size, 0);

        if (width == 0) {
            width = dp2px(getContext(), 32);
        }
        a.recycle();

        View.inflate(getContext(), R.layout.pagination_layout, this);
        mRecycleView = findViewById(R.id.rcv);
        mPaginationIndicatorView = findViewById(R.id.indicator);
        mProgressBar = findViewById(R.id.progress);

        if (numberTipShowCount != 0)
            mPaginationIndicatorView.setNumberTipShowCount(numberTipShowCount);
        PaginationIndicator.sTextSize = textSize;
        PaginationIndicator.sColor_selected = color_selected;
        PaginationIndicator.sColor_unselected = color_unselected;
        PaginationIndicator.sWidth = width;
        mPaginationIndicatorView.refreshView();
    }

    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
        mRecycleView.setAdapter(mAdapter.getInnerAdapter());
        mAdapter.mPaginationRecycleView = this;

        mPaginationIndicatorView.setListener(this);
        setTotal(mAdapter.mDataTotal);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mRecycleView.setLayoutManager(layoutManager);
    }

    /**
     * 设置"x条/页"的spinner的选项源
     *
     * @param perPageCountChoices
     */
    public void setPerPageCountChoices(int[] perPageCountChoices) {
        mPaginationIndicatorView.setPerPageCountChoices(perPageCountChoices);
    }

    /**
     * 当Listener.loadMore回调执行后，外部加载完数据后应该设置相应状态
     *
     * @param flag SUCCESS : 加载数据成功  EMPTY : 空数据  FAILED : 加载失败
     */
    public void setState(int flag) {
        switch (flag) {
            case SUCCESS:

                break;
            case EMPTY:
            case FAILED:
                if (mAdapter.mLastPagePos > 0) {
                    mPaginationIndicatorView.skip2Pos(mAdapter.mLastPagePos);
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
        mPaginationIndicatorView.setTotalCount(total);
    }

    /**
     * 设置分页控件中间的数字显示个数
     *
     * @param numberTipShowCount
     */
    public void setNumberTipShowCount(int numberTipShowCount) {
        mPaginationIndicatorView.setNumberTipShowCount(numberTipShowCount);
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
        /**
         * 当当前页无数据在分页控制器里时，会触发此回调告知外部去获取数据，获取成功通过adapter.setDatas(int, List<T>)设置数据
         *
         * @param currentPagePosition 当前页码
         * @param nextPagePosition    需要加载的下一页的页码(实际要加载的正式此参数)
         * @param perPageCount        每页显示的条数
         * @param dataTotalCount      数据源总数量
         */
        void loadMore(int currentPagePosition, int nextPagePosition, int perPageCount, int dataTotalCount);

        /**
         * 当"x条/每页"选择改变时触发的回调
         *
         * @param perPageCount
         */
        void onPerPageCountChanged(int perPageCount);
    }

    /**
     * 对内部RecycleView的Adapter的装饰增强Adapter
     *
     * @param <T>  数据的类型
     * @param <VH> RecycleView体系里的ViewHolder增强
     */
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

        /**
         * 根据页码获取相应的页的数据列表
         *
         * @param pagePos
         * @return
         */
        public List<T> getDataByPage(int pagePos) {
            return mDataMap.get(pagePos);
        }

        /**
         * 获取当前页码
         *
         * @return
         */
        public int getCurrentPagePos() {
            return mCurrentPagePos;
        }

        /**
         * 获取每页条数
         *
         * @return
         */
        public int getPerPageCount() {
            return mPerPageCount;
        }

        /**
         * @param dataTotalCount 数据源总量(需要传入进行分页器初始化)
         */
        public Adapter(int dataTotalCount) {
            mInnerAdapter = new InnerAdapter();
            mDataMap = new HashMap<>();
            setDataTotalCount(dataTotalCount);
        }

        protected InnerAdapter getInnerAdapter() {
            return mInnerAdapter;
        }

        /**
         * 设置数据源总量  会触发更新
         *
         * @param total
         */
        public void setDataTotalCount(int total) {
            mDataTotal = total;
            if (mPaginationRecycleView != null)
                mPaginationRecycleView.setTotal(total);
            clear();
        }

        /**
         * 设置特定页码的数据列表
         *
         * @param pagePos
         * @param datas
         */
        public void setDatas(int pagePos, List<T> datas) {
            mDataMap.put(pagePos, datas);
            notifyDataSetChanged();
        }

        /**
         * 清空数据
         */
        public void clear() {
            mDataMap.clear();
            notifyDataSetChanged();
        }

        /**
         * 获取当前页显示的列表中的position项并返回
         *
         * @param position
         * @return
         */
        public T getCurrentPageItem(int position) {
            return mShowList != null ? mShowList.get(position) : null;
        }

        /**
         * 往正方向(页码大的方向)翻页
         *
         * @param nextPos 需要转入的页
         * @param lastPos 记录当前即将被离开的页
         */
        public void nextSkip(int nextPos, int lastPos) {
            mCurrentPagePos = nextPos;
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

        /**
         * 往负方向(页码小的方向)翻页
         *
         * @param nextPos 需要转入的页
         * @param lastPos 记录当前即将被离开的页
         */
        public void lastSkip(int nextPos, int lastPos) {
            mCurrentPagePos = nextPos;
            mLastPagePos = lastPos;
            notifyDataSetChanged();
        }

        /**
         * 检测即将跳入的页是否已有数据在缓存里，无则需要向外部加载数据
         *
         * @return true: 需要回调loadmore  false: 有缓存数据 直接使用
         */
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
            /**
             * @param view
             * @param holder
             * @param position 当前页显示的列表的选中位置(即recycleview当前显示的列表选中项)
             */
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
