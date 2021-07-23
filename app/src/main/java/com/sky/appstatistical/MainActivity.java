package com.sky.appstatistical;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.tabs.TabLayout;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.GetRequest;
import com.sky.appstatistical.utils.AppUsageUtil;
import com.sky.appstatistical.utils.JDateKit;
import com.sky.appstatistical.utils.JListKit;

import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AppUsageActivity";
    private List<AppUsageBean> mItems;
    private CommonRecyclerAdapter<AppUsageBean> mAdapter;
    private static final String[] TAB_NAMES = {"今日数据", "昨日数据", "本周数据", "本月数据", "年度数据"};
    private boolean isGoToGrand = false;// 是否去过授权页面
    private TextView tvTimeRange;
    private RecyclerView rvAppUsage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定UI
        setContentView(R.layout.activity_main);
        TabLayout tabCondition = findViewById(R.id.tab_condition);
        tvTimeRange = findViewById(R.id.tv_time_range);
        rvAppUsage = findViewById(R.id.rv_app_usage);



        // 初始化Tab
        int c = 0;
        TabLayout tabLayout = tabCondition;
        for (String name : TAB_NAMES) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setTag(c);
            tab.view.setOnClickListener(v -> onTabClick((int) tab.getTag()));
            tabLayout.addTab(tab.setText(name));
            c++;
        }

        // 授权|加载数据
        initData();
    }

    private void initData() {
        if (AppUsageUtil.hasAppUsagePermission(this)) {
            // 默认加载今天的数据
            isGoToGrand = false;
            onTabClick(0);
        } else {
            isGoToGrand = true;
            // TODO 这里有点强制开启的意思，实际应用中最好弹出一个对话框让用户知道，并可以选择【授权】或【退出】
            AppUsageUtil.requestAppUsagePermission(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGoToGrand) {// 如果从应用跳转到了授权，那么返回应用的时候 需要重新执行一次
            initData();
        }
    }

    public void onTabClick(int position) {
        Log.d(TAG, "onTabClick() called with: position = [" + position + "]");

        setTitle(TAB_NAMES[position]);
        long currTime = System.currentTimeMillis();

        switch (position) {
            case 0:// 今天的数据  00:00 到 现在
                getAppUsage(getTodayTime0(), currTime);
                break;
            case 1:// 昨天的数据  昨天00:00 - 今天00:00
                long todayTime0 = getTodayTime0();
                getAppUsage(todayTime0 - DateUtils.DAY_IN_MILLIS, todayTime0);
                break;
            case 2:// 最近7天数据
                getAppUsage(currTime - DateUtils.WEEK_IN_MILLIS, currTime);
                break;
            case 3:// 最近30天数据
                getAppUsage(currTime - DateUtils.DAY_IN_MILLIS * 30, currTime);
                break;
            case 4:// 最近一年的数据
                getAppUsage(currTime - DateUtils.DAY_IN_MILLIS * 365, currTime);
                break;
        }
    }

    /**
     * @return 今日零点的时间
     */
    private long getTodayTime0() {
        // 获取今天凌晨0点0分0秒的time

        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                10, 0, 0);

        Log.d(TAG,
                "getTodayTime0: year = "+Calendar.YEAR+"month = "+Calendar.MONTH+"day = "+Calendar.DAY_OF_MONTH);

        Log.d(TAG, "getTodayTime0: "+calendar.getTimeInMillis());


        return calendar.getTimeInMillis();
    }

    private LoadAppUsageTask mLoadAppUsageTask;

    private void getAppUsage(long beginTime, long endTime) {
        String fmt = "yyyy-MM-dd HH:mm:ss";
        tvTimeRange.setText(String.format("(%s - %s)",
                JDateKit.timeToDate(fmt, beginTime),
                JDateKit.timeToDate(fmt, endTime)));
        // setTitle("数据分析中...");
//        showLoading("数据分析中...");

        Log.d(TAG, "getAppUsage: beginTime = "+JDateKit.timeToDate(fmt, beginTime));


        // a task can be executed only once,init is required every time
        mLoadAppUsageTask = new LoadAppUsageTask(beginTime, endTime, list -> {
            mItems = list;
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {






            }


            initAdapter();
        });
        mLoadAppUsageTask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadAppUsageTask != null) {
            mLoadAppUsageTask.cancel(true);
            mLoadAppUsageTask = null;
        }
    }

    private long maxTime;// 当前列表中 使用最久的APP时间 用于计算进度条百分比

    private void initAdapter() {
        if (JListKit.isNotEmpty(mItems)) {
            Collections.sort(mItems);// 按使用时长排序
            maxTime = mItems.get(0).getTotalTimeInForeground();
        } else {
            maxTime = 1;
        }
        setTitle(String.format("%s (共%s条)", getTitle(), mItems.size()));
        if (mAdapter == null) {
            String fmt = "yyyy-MM-dd HH:mm:ss";
            mAdapter = new CommonRecyclerAdapter<AppUsageBean>(R.layout.item_app_usage, mItems) {
                @Override
                protected void convert(@NonNull BaseViewHolder helper, AppUsageBean item) {
                    helper.setText(R.id.id_tv_app_name, String.format("%s(%s)", item.getAppName(), item.getPackageName()));
                    Drawable appIcon = item.getAppIcon();
                    if (appIcon != null) {
                        helper.setImageDrawable(R.id.id_iv_app_icon, appIcon);
                    } else {
                        helper.setImageResource(R.id.id_iv_app_icon, R.mipmap.ic_launcher);
                    }
                    long totalTimeInForeground = item.getTotalTimeInForeground();
                    helper.setText(R.id.id_tv_time_in_foreground, String.format("使用时长:%s (%sms)", JDateKit.timeToStringChineChinese(totalTimeInForeground), totalTimeInForeground));
                    helper.setText(R.id.id_tv_last_usage, String.format("上次使用:%s", JDateKit.timeToDate(fmt, item.getLastTimeUsed())));
                    // 计算进度条百分比
                    float percent = (float) item.getTotalTimeInForeground() / maxTime;
                    Guideline guideline = helper.getView(R.id.guideline);
                    guideline.setGuidelinePercent(percent);
                }
            };
            rvAppUsage.setAdapter(mAdapter);
            rvAppUsage.setLayoutManager(new LinearLayoutManager(this));
        } else {
            mAdapter.setNewInstance(mItems);
        }
    }
}