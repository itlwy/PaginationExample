package com.lwy.paginationlib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PaginationController extends FrameLayout implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private LinearLayout mControllerView;
    private TextView mLastBtn;
    private TextView mNextBtn;
    private Spinner mPerPageCountSpinner;
    private ArrayAdapter<String> mPerPageCountAdapter;

    private OnChangedListener mListener;

    private int[] mPerPageCountChoices = {10, 20, 30, 50};
    private int mCurrentPagePos = 1;
    private int mLastPagePos = 0;
    private int mTotalPageCount;
    private int mTotalCount;
    private int mPerPageCount = 10;
    private int mNumberTipShowCount = 5;  // 设为奇数: 数字指示器的数量

    private LinearLayout mNumberLlt;
    private TextView[] mNumberTipTextViewArray;

    private int mWidth;
    public static int sColor_selected;
    public static int sColor_unselected;
    public static float sTextSize = 16;

    public void setPerPageCountChoices(int[] perPageCountChoices) {
        this.mPerPageCountChoices = perPageCountChoices;
        initSpinner();
    }

    public void setmListener(OnChangedListener mListener) {
        this.mListener = mListener;
    }

    public PaginationController(Context context) {
        super(context);
        init();
    }

    public PaginationController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaginationController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mWidth = dp2px(getContext(), 32);
        sColor_selected = getContext().getResources().getColor(R.color.indicator_rect_selected);
        sColor_unselected = getContext().getResources().getColor(R.color.indicator_rect_unselected);

        mControllerView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pagination_indicator, null);
        mLastBtn = mControllerView.findViewById(R.id.last_btn);
        mNextBtn = mControllerView.findViewById(R.id.next_btn);
        mNumberLlt = mControllerView.findViewById(R.id.number_llt);
        mPerPageCountSpinner = (Spinner) mControllerView.findViewById(R.id.per_page_count_spinner);

        mLastBtn.setText("<");
        mNextBtn.setText(">");
        mLastBtn.setTextSize(sTextSize);
        mNextBtn.setTextSize(sTextSize);
        mLastBtn.getLayoutParams().width = mWidth;
        mLastBtn.getLayoutParams().height = mWidth;
        mNextBtn.getLayoutParams().width = mWidth;
        mNextBtn.getLayoutParams().height = mWidth;
        initSpinner();
        mLastBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(mControllerView, layoutParams);
    }

    private void initSpinner() {
        if (mPerPageCountAdapter == null) {
            mPerPageCountSpinner.getLayoutParams().height = mWidth;
            mPerPageCountAdapter = new CustomArrayAdapter(getContext());
            mPerPageCountSpinner.setAdapter(mPerPageCountAdapter);
            mPerPageCountSpinner.setOnItemSelectedListener(this);
        } else {
            mPerPageCountAdapter.clear();
        }
        for (int perPageCountChoice : mPerPageCountChoices) {
            mPerPageCountAdapter.add(perPageCountChoice + "条/页");
        }
        mPerPageCountSpinner.setSelection(0);
    }

    public void setTotalCount(int totalCount) {
        this.mTotalCount = totalCount;
        notifyChange();
    }

    private void notifyChange() {
        initIndicator();
        updateNumberLlt();
    }

    private void initIndicator() {
        mCurrentPagePos = 1;
        mLastPagePos = 0;
        if (mTotalCount == 0) {
            mLastBtn.setSelected(false);
            mLastBtn.setTextColor(sColor_unselected);
            mNextBtn.setSelected(false);
            mNextBtn.setTextColor(sColor_unselected);
            return;
        }
        mLastBtn.setSelected(false);
        mLastBtn.setTextColor(sColor_unselected);
        mNextBtn.setSelected(true);
        mNextBtn.setTextColor(sColor_selected);

        mTotalPageCount = mTotalCount % mPerPageCount > 0 ? mTotalCount / mPerPageCount + 1 : mTotalCount / mPerPageCount;
        if (mListener != null) {
            mListener.onPerPageCountChanged(mPerPageCount);
            mListener.onPageSelectedChanged(mCurrentPagePos, mLastPagePos, mTotalPageCount, mTotalCount);
        }
    }

    public void next() {
        int lastPos = mCurrentPagePos;
        if (mCurrentPagePos == mTotalPageCount)
            return;
        mCurrentPagePos++;
        updateState(lastPos);
    }

    public void last() {
        int lastPos = mCurrentPagePos;
        if (mCurrentPagePos == 1)
            return;
        mCurrentPagePos--;
        updateState(lastPos);
    }

    private void updateState(int lastPos) {
        if (mCurrentPagePos == mTotalPageCount) {
            mNextBtn.setSelected(false);
            mNextBtn.setTextColor(sColor_unselected);
        } else {
            mNextBtn.setSelected(true);
            mNextBtn.setTextColor(sColor_selected);
        }
        if (mCurrentPagePos == 1) {
            mLastBtn.setSelected(false);
            mLastBtn.setTextColor(sColor_unselected);
        } else {
            mLastBtn.setSelected(true);
            mLastBtn.setTextColor(sColor_selected);
        }

        if (mListener != null) {
            mListener.onPageSelectedChanged(mCurrentPagePos, lastPos, mTotalPageCount, mTotalCount);
        }
        updateNumberLlt();
    }

    private void updateNumberLlt() {
        if (mTotalCount == 0) {
            return;
        }
        geneNumberTextView();
        if (mTotalPageCount > mNumberTipShowCount) {
            int start, end;
            int half = mNumberTipShowCount / 2;
            start = mCurrentPagePos - half;
            end = mCurrentPagePos + half;
            if (start <= 0) {
                // 越过"数字1"的位置了  把超出部分补偿给end
                end = end + Math.abs(start) + 1;
                start = 1;
            } else if (end > mTotalPageCount) {
                // 越过"总页数数字"的位置了  把超出部分补偿给start
                start = start - Math.abs(mTotalPageCount - end);
                end = mTotalPageCount;
            }
            updateNumberText(start, end);
        } else {
            // 总页数小于数字指示器数量，则直接以总页数的大小来刷新
            updateNumberText(1, mNumberTipTextViewArray.length);
        }
    }

    private void updateNumberText(int start, int end) {
        for (int i = 0; i < end - start + 1; i++) {
            TextView textView = mNumberTipTextViewArray[i];
            textView.setText((start + i) + "");
            if (start + i == mCurrentPagePos) {
                textView.setSelected(true);
                textView.setTextColor(sColor_selected);
            } else {
                textView.setSelected(false);
                textView.setTextColor(sColor_unselected);
            }
        }
    }

    private void geneNumberTextView() {
        int count = mNumberTipShowCount < mTotalPageCount ? mNumberTipShowCount : mTotalPageCount;
        if (mNumberTipTextViewArray == null) {
            mNumberTipTextViewArray = new TextView[count];
            mNumberLlt.removeAllViews();
        } else if (mNumberTipTextViewArray.length != count) {
            mNumberTipTextViewArray = new TextView[count];
            mNumberLlt.removeAllViews();
        } else {
            return;
        }
        for (int i = 0; i < mNumberTipTextViewArray.length; i++) {
            TextView textView = new TextView(getContext());
            textView.setBackgroundResource(R.drawable.selector_rect);
            mNumberTipTextViewArray[i] = textView;
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(sTextSize);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mWidth, mWidth);
            if (i > 0 && i < mNumberTipTextViewArray.length)
                params.leftMargin = 2;
            textView.setOnClickListener(this);
            mNumberLlt.addView(textView, params);
        }
    }

    @Override
    public void onClick(View v) {
        int lastPos = mCurrentPagePos;

        if (v.getId() == R.id.next_btn) {
            if (mCurrentPagePos == mTotalPageCount)  // 已经是最后一页了
                return;
            mLastPagePos = mCurrentPagePos;
            mCurrentPagePos++;
        } else if (v.getId() == R.id.last_btn) {
            if (mCurrentPagePos == 1)  // 已经是第一页了
                return;
            mLastPagePos = mCurrentPagePos;
            mCurrentPagePos--;
        } else {
            // 点击了中间的数字指示器
            int clickNumber = Integer.parseInt(((TextView) v).getText().toString());
            if (clickNumber == mCurrentPagePos) {
                return;
            }
            mLastPagePos = mCurrentPagePos;
            mCurrentPagePos = clickNumber;
        }
        updateState(lastPos);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mPerPageCount = mPerPageCountChoices[position];
        if (this.mListener != null) {
            mListener.onPerPageCountChanged(mPerPageCount);
        }
        notifyChange();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void skip2Pos(int position) {
        mLastPagePos = mCurrentPagePos;
        mCurrentPagePos = position;
        updateState(mLastPagePos);
    }


    public interface OnChangedListener {
        void onPageSelectedChanged(int currentPapePos, int lastPagePos, int totalPageCount, int total);

        void onPerPageCountChanged(int perPageCount);
    }

    // 为了自定义Spinner字体颜色等
    static class CustomArrayAdapter extends ArrayAdapter<String> {
        private Context mContext;
        private List<String> mStringArray = new ArrayList<>();

        public CustomArrayAdapter(Context context) {
            super(context, android.R.layout.simple_spinner_item);
            mContext = context;
        }

        public CustomArrayAdapter(Context context, List<String> stringArray) {
            super(context, android.R.layout.simple_spinner_item, stringArray);
            mContext = context;
            mStringArray = stringArray;
        }

        @Override
        public void add(String object) {
            super.add(object);
            mStringArray.add(object);
        }

        @Override
        public void addAll(String... items) {
            super.addAll(items);
            mStringArray.addAll(Arrays.asList(items));
        }

        @Override
        public void addAll(Collection<? extends String> collection) {
            super.addAll(collection);
            mStringArray.addAll(collection);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            //修改Spinner展开后的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            //此处text1是Spinner默认的用来显示文字的TextView
            TextView tv = convertView.findViewById(android.R.id.text1);
            tv.setText(mStringArray.get(position));
            tv.setTextSize(sTextSize);
            tv.setTextColor(sColor_selected);

            return convertView;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 修改Spinner选择后结果的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            //此处text1是Spinner默认的用来显示文字的TextView
            TextView tv = convertView.findViewById(android.R.id.text1);
            tv.setText(mStringArray.get(position));
            tv.setTextSize(sTextSize);
            tv.setTextColor(sColor_selected);
            return convertView;
        }

    }

    /**
     * dp转换成px
     */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转换成dp
     */
    public static int px2dp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
