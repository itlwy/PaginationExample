# PaginationExample
轻量级仿网页风格分页器，和RecycleView封装一起配合使用，也可单独使用，喜欢就star、fork下吧～谢谢

## 目录

* [功能介绍](#功能介绍)
* [效果图](#效果图)
* [如何引入](#如何引入)
* [简单使用](#简单使用)
* [依赖](#依赖)
* [License](#License)

## 功能介绍

- [x] 支持延迟加载分页
- [x] 支持单独分页器组件使用；同时封装了RecycleView，可以配合使用
- [x] 支持加载状态改变提示
- [x] 支持自定义数字指示器数量、选中和未选中等样式

## 效果图

<img src="https://github.com/itlwy/PaginationExample/blob/master/resources/pic1.png"
 width = "40%" height = "40%" div align=left/><img src="https://github.com/itlwy/PaginationExample/blob/master/resources/pic2.png"  width = "40%" height = "40%" div align=right/>

## Screenshots
#![Alt text](https://github.com/itlwy/PaginationExample/blob/master/resources/pagination.gif)

## 如何引入
### Gradle引入
### step 1
Add the JitPack repository to your build file

```
	allprojects {
			repositories {
				...
				maven { url 'https://jitpack.io' }
			}
		}
```

### Step 2
Add the dependency

```
dependencies {
	         implementation 'com.github.itlwy:PaginationExample:0.0.20'
	}

```



## 简单使用

### 组合RecycleView使用

此时使用的是PaginationRecycleView类

1. activity_main.xml

   ```xml
   ...
   <com.lwy.paginationlib.PaginationRecycleView
           android:id="@+id/pagination_rcv"
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:layout_marginBottom="8dp"
           app:number_tip_count="5"
           app:rect_size="30dp"
           app:selected_color="@color/indicator_rect_selected"
           app:text_size="14sp"
           app:unselected_color="@color/indicator_rect_unselected"
           />	
   ...
   ```

2. MainActivity.java

   ```java
    ...
     @Override
       protected void onCreate(Bundle savedInstanceState) {
   		mPaginationRcv = findViewById(R.id.pagination_rcv);
           mAdapter = new CustomAdapter(this, 99);
           mPaginationRcv.setAdapter(mAdapter);
   //        mPaginationRcv.setPerPageCountChoices(perPageCountChoices);
           GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
           mPaginationRcv.setLayoutManager(layoutManager);
           mPaginationRcv.setListener(new PaginationRecycleView.Listener() {
               @Override
               public void loadMore(int currentPagePosition, int nextPagePosition, int perPageCount, int dataTotalCount) {
            		// nextPagePosition为将要加载的页码，即需要加载数据的页
            		// perPageCount 每页展示的数量
                  //TODO : 此处进行异步数据加载
                  //TODO : 完成加载后通知分页控件(注意此处应该是在主线程运行)，如下
                   mAdapter.setDatas(nextPagePosition, data);
                   mPaginationRcv.setState(PaginationRecycleView.SUCCESS);
                   
               }
   
               @Override
               public void onPerPageCountChanged(int perPageCount) {
   				// "x条/每页"Spinner选中值改变时触发
               }
           });
           mAdapter.setOnItemClickListener(this);
    }
            @Override
       public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
           JSONObject item = mAdapter.getCurrentPageItem(position);  // 此处position返回的是recycleview的位置，所以取当前页显示列表的项
           Toast.makeText(this, item.optString("name"), Toast.LENGTH_LONG).show();
       }
   ...
   ```

3. CustomAdapter

   ```java
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
   ```


   布局文件中的属性说明:

   ```
   app:number_tip_count="5"   // 数字指示器显示的数量，默认是5
   app:rect_size="30dp"		// 圆角矩形的大小(正方形)
   app:selected_color="@color/indicator_rect_selected"  // 选中的颜色(包含框和字体)
   app:text_size="14sp"  // 字体大小
   app:unselected_color="@color/indicator_rect_unselected" 未选中的颜色(包含框和字体)
   ```

### 单独使用

​	此时使用的是PaginationIndicator类，布局如下：

```xml
...
<com.lwy.paginationlib.PaginationIndicator
    android:id="@+id/indicator"
    android:layout_width="match_parent"
    app:number_tip_count="5"
    app:rect_size="30dp"
    app:selected_color="@color/indicator_rect_selected"
    app:text_size="14sp"
    app:unselected_color="@color/indicator_rect_unselected"
    android:layout_height="wrap_content">
...
```

​	说明如上述

​	代码如下：

```java
... 
private int[] perPageCountChoices = {10, 20, 30, 50};
... 
mIndicatorView = (PaginationIndicator) findViewById(R.id.indicator);
        mIndicatorView.setTotalCount(99);  // 设置数据源总数量即可
        mIndicatorView.setPerPageCountChoices(perPageCountChoices); // 选填
        mIndicatorView.setListener(new PaginationIndicator.OnChangedListener() {
            @Override
            public void onPageSelectedChanged(int currentPapePos, int lastPagePos, int totalPageCount, int total) {
                Toast.makeText(MainActivity.this, "选中" + currentPapePos + "页", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPerPageCountChanged(int perPageCount) {
				// x条/页 选项改变时触发
            }
        });
...
```

​	相关说明已在代码里注释，详细可参考demo，谢谢

## 依赖

- recyclerview : com.android.support:recyclerview-v7:28.0.0


## License

   	Copyright 2019 lwy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.