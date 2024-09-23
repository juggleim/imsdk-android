package com.juggle.chat.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.jet.im.kit.fragments.BaseFragment;

/**
 * Fragment 页面基类
 *
 * @author rongcloud
 * @since 5.10.4
 */
public abstract class BasePageFragment<VM extends ViewModel>
        extends BaseFragment {
    private VM viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments() == null ? new Bundle() : getArguments();
        this.viewModel = onCreateViewModel(bundle);
    }

    @Nullable
    @Override
    public final View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = createView(inflater, container, getArguments());
        onViewReady(this.viewModel);
        return view;
    }


    /**
     * 创建 ViewModel
     *
     * @return 返回 ViewModel
     */
    @NonNull
    protected abstract VM onCreateViewModel(@NonNull Bundle bundle);

    protected abstract View createView(@NonNull LayoutInflater inflater,
                                       @Nullable ViewGroup container,
                                       @Nullable Bundle savedInstanceState);

    /**
     * View 创建之后
     *
     * @param viewModel VM
     */
    protected abstract void onViewReady(@NonNull VM viewModel);

    @NonNull
    protected VM getViewModel() {
        return viewModel;
    }

}
