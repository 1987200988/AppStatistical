/*
 * Copyright (C) 2021 Baidu, Inc. All Rights Reserved.
 */
package com.sky.appstatistical.utils;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

public class HttpUtils {


    private HttpUtils(){}


    static class Holder{
        static HttpUtils httpUtils = new HttpUtils();
    }


    public static HttpUtils getInstance(){
        return Holder.httpUtils;
    }



    /**
     * get请求获取数据
     *
     * @param url
     */
    private void getByOkGo(String url) {
        OkGo.<String>get("url")//
                .tag(this)
                .params("adKind","1")//传入请求参数
                .cacheKey("cachekey")//作为缓存的key
                .cacheMode(CacheMode.NO_CACHE)//设置缓存模式

                //StringCallback只返回成功
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {

                    }
                    @Override//适配器模式,可以不实现该方法
                    public void onError(Response<String> response) {

                    }
                });
    }
    /**
     * post请求
     * @param url
     */
    private void postByOkGo(String url){
        OkGo.<String>post(url)
                .tag(this)
                .params("key", "v")
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {

                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return null;
                    }
                });




    }
}
