package com.example.lwy.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lwy.paginationlib.PaginationRecycleView;
import com.lwy.paginationlib.ViewHolder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PaginationRecycleView.Adapter.OnItemClickListener {


    private PaginationRecycleView mPaginationRcv;
    private CustomAdapter mAdapter;
    private int[] perPageCountChoices = {10, 20, 30, 50};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPaginationRcv = findViewById(R.id.pagination_rcv);
        mAdapter = new CustomAdapter(this, 99);
        mPaginationRcv.setAdapter(mAdapter);
        mPaginationRcv.setPerPageCountChoices(perPageCountChoices);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mPaginationRcv.setLayoutManager(layoutManager);

        mPaginationRcv.setmListener(new PaginationRecycleView.Listener() {
            @Override
            public void loadMore(int currentPagePosition, int nextPagePosition, int perPageCount, int dataTotalCount) {
                mAdapter.setDatas(nextPagePosition, geneDatas(nextPagePosition, perPageCount));
                mPaginationRcv.setState(PaginationRecycleView.SUCCESS);
            }

            @Override
            public void onPerPageCountChanged(int perPageCount) {

            }
        });
        mAdapter.setOnItemClickListener(this);

    }

    public List<JSONObject> geneDatas(int currentPagePosition, int perPageCount) {
        int from = (currentPagePosition - 1) * perPageCount;
        List<JSONObject> datas = new ArrayList<>();
        try {
            for (int i = 0; i < perPageCount; i++) {
                JSONObject json = new JSONObject();

                json.put("name", "name<" + (from++) + ">");

                datas.add(json);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "error:" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return datas;
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        JSONObject item = mAdapter.getCurrentPageItem(position);
        Toast.makeText(this, item.optString("name"), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }

    class CustomAdapter extends PaginationRecycleView.Adapter<JSONObject, ViewHolder> {


        private Context mContext;

        public CustomAdapter(Context context, int dataTotalCount) {
            super(dataTotalCount);
            mContext = context;
        }


        @Override
        public void bindViewHolder(ViewHolder viewholder, JSONObject data) {
            viewholder.setText(R.id.text, data.optString("name"));
        }

        @Override
        public ViewHolder createViewHolder(@NonNull ViewGroup parent, int viewTypea) {
            return ViewHolder.createViewHolder(mContext, parent, R.layout.item_list);
        }
    }


}
