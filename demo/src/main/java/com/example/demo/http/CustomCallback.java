package com.example.demo.http;

import com.example.demo.bean.HttpResult;
import com.example.demo.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class CustomCallback<T extends HttpResult<K>, K> implements Callback<T> {

    public abstract void onSuccess(K k);

    public void onError(Throwable t) {
        ToastUtils.show(t.getMessage());
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            if (response.body() != null) {
                if (response.body().getCode() == 0) {
                    onSuccess(response.body().getData());
                } else {
                    onFailure(call, new Exception(response.body().getMsg()));
                }

            } else {
                onFailure(call, new Exception("body is null"));
            }
            onSuccess(response.body().getData());
        } else {
            onFailure(call, new Exception("responseCode is" + response.code()));
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onError(t);
    }
}
