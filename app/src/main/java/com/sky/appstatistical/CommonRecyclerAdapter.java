package com.sky.appstatistical;

import java.util.List;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

/**
 * Created by ZengCS on 2019/5/30.
 * E-mail:zengcs@vip.qq.com
 * Add:成都市天府软件园E3
 */
public abstract class CommonRecyclerAdapter<T> extends BaseQuickAdapter<T, BaseViewHolder> {
    public CommonRecyclerAdapter(@LayoutRes int resId, @Nullable List<T> data) {
        super(resId, data);
    }
}