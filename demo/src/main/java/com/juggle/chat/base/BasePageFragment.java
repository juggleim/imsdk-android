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
 * Fragment base class
 *
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
     * Create the ViewModel.
     *
     * @return the ViewModel
     */
    @NonNull
    protected abstract VM onCreateViewModel(@NonNull Bundle bundle);

    protected abstract View createView(@NonNull LayoutInflater inflater,
                                       @Nullable ViewGroup container,
                                       @Nullable Bundle savedInstanceState);

    /**
     * Called after the view is created.
     *
     * @param viewModel VM
     */
    protected abstract void onViewReady(@NonNull VM viewModel);

    @NonNull
    protected VM getViewModel() {
        return viewModel;
    }

}
