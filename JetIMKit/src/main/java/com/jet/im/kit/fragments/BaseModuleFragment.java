package com.jet.im.kit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.modules.BaseModule;
import com.jet.im.kit.vm.BaseViewModel;

/**
 * Fragments provided to use UIKit's modules.
 * Each Fragment has a single module structured by UI components and ViewModel.
 * And below Callback functions are called on the {@link #onCreate(Bundle)} method.
 * <p>The functions below are called in order.</p>
 * <ol>
 * <li>{@link #onCreateModule(Bundle)}</a>
 * <li>{@link #onConfigureParams(BaseModule, Bundle)}</a>
 * <li>{@link #onCreateModule(Bundle)}</a>
 * <li>{@link #onCreateViewModel()}</a>
 * </ol>
 *
 * After above methods are all called, the created view model tries to {@link BaseViewModel#authenticate(AuthenticateHandler)}.
 * User authentication completed means the fragment is ready to use. When authentication is complete,
 * the {@link #onBeforeReady(ReadyStatus, BaseModule, BaseViewModel)} and {@link #onReady(ReadyStatus, BaseModule, BaseViewModel)} methods are called.
 * The ReadyState let you know to determine the result of the authentication.
 * If the {@link ReadyStatus} is {@link ReadyStatus#READY}, the current user instance and the channel instance ars available.
 *
 * @param <MT> The specific module type that the fragment that inherits this class wants to use
 * @param <VM> The specific view model type that the fragment that inherits this class wants to use
 * @see ReadyStatus
 * since 3.0.0
 */
public abstract class BaseModuleFragment<MT extends BaseModule, VM extends BaseViewModel> extends PermissionFragment {
    private MT module;
    private VM viewModel;

    /**
     * Create a module and a view model, and proceed with the authentication process in the view model.
     * When authentication is complete, {@link #onBeforeReady(ReadyStatus, BaseModule, BaseViewModel)}, {@link #onReady(ReadyStatus, BaseModule, BaseViewModel)} are called.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     * since 3.0.0
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.viewModel = onCreateViewModel();
        this.module = onCreateModule(getArguments() == null ? new Bundle() : getArguments());
        onConfigureParams(this.module, getArguments() == null ? new Bundle() : getArguments());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // It can be a problem if `authenticate` ends first before `onCreateView` is called.
        shouldAuthenticate();
        return module.onCreateView(requireActivity(), inflater, getArguments());
    }

    /**
     * Called to do initial creation of a module.
     * This is called in {@link #onCreate(Bundle)} and before {@link #onConfigureParams(BaseModule, Bundle)}.
     *
     * @param args If the fragment is being re-created from a previous saved state, this is the state.
     * @return The module that the fragment wants to use
     * since 3.0.0
     */
    @NonNull
    protected abstract MT onCreateModule(@NonNull Bundle args);

    /**
     * Called to initialize the module's params or components' params.
     * This is called after {@link #onCreateModule(Bundle)}.
     * <p>
     * The instance of components and Params of each component will be created automatically when the module is created
     * and you can set the properties of Params in this function. Each param can get via the component of module and it can't create directly.
     * </p>
     * <pre class="prettyprint">
     * public class MyFragment extends BaseModuleFragment {
     *     protected void onConfigureParams(BaseModuleFragment module, Bundle args) {
     *         super.onConfigureParams(module, args);
     *
     *         Params params = module.getComponent().getParams();
     *         params.setTitle("title");
     *     }
     * }
     * </pre>
     *
     * <h3>Notes</h3>
     * The properties set in Params are used to create View in the onCreateView of the module, and params set after the module's onCreateView is called may not be reflected.
     *
     * @param module The module to be the target of configuring parameters
     * @param args   If the fragment is being re-created from a previous saved state, this is the state.
     * since 3.0.0
     */
    protected abstract void onConfigureParams(@NonNull MT module, @NonNull Bundle args);

    /**
     * Called to do initial creation of a view model.
     * This is called after {@link #onConfigureParams(BaseModule, Bundle)}.
     *
     * @return The view model that the fragment wants to use
     * @see BaseViewModel
     * since 3.0.0
     */
    @NonNull
    protected abstract VM onCreateViewModel();

    /**
     * After {@link BaseViewModel#authenticate(AuthenticateHandler)} ()} is finished,
     * {@link #onReady(ReadyStatus, BaseModule, BaseViewModel)} will be called with the result of authentication
     * and all preparations will be ready to use.
     * This function called before {@link #onReady(ReadyStatus, BaseModule, BaseViewModel)}.
     *
     * @param status    The status of the result of the view model's authentication
     * @param module    Module to be setup
     * @param viewModel A view model that provides the data needed for the fragment
     * @see ReadyStatus
     * since 3.0.0
     */
    protected abstract void onBeforeReady(@NonNull ReadyStatus status, @NonNull MT module, @NonNull VM viewModel);

    /**
     * Called to start the operation of the fragment after authentication and module setup.
     * <p>
     * Called when authentication has been completed,
     * and the result of authentication delivers through {@link ReadyStatus}.</p>
     * If the {@link ReadyStatus} has {@link ReadyStatus#READY}, all authentications are finished normally
     * {@link ReadyStatus#READY} meant all authentication has been completed normally,
     * and in case of an {@link ReadyStatus#ERROR}, authentication can be attempted again through the {@link #shouldAuthenticate()} in this fragment.
     *
     * @param status    The status of the result of the view model's authentication.
     * @param module    Module to be used in fragment
     * @param viewModel A view model that provides the data needed for the fragment
     * @see ReadyStatus
     * since 3.0.0
     */
    protected abstract void onReady(@NonNull ReadyStatus status, @NonNull MT module, @NonNull VM viewModel);

    // It shouldn't exist other than a function that is always called.
    void onAuthenticateComplete(@NonNull ReadyStatus status, @NonNull MT module) {
        if (!isFragmentAlive()) return;
        onBeforeReady(status, module, viewModel);
        onReady(status, module, viewModel);
    }

    /**
     * Returns the module to be used in fragment.
     *
     * @return Module that has view information in fragment
     * since 3.0.0
     */
    @NonNull
    protected MT getModule() {
        return module;
    }

    /**
     * Returns the view model to be used in fragment.
     *
     * @return The view model that provides the data needed for the fragment
     * since 3.0.0
     */
    @NonNull
    protected VM getViewModel() {
        return viewModel;
    }

    /**
     * Requests authentication with Sendbird Chat SDK.
     * The results will be delivered {@link #onBeforeReady(ReadyStatus, BaseModule, BaseViewModel)} and {@link #onReady(ReadyStatus, BaseModule, BaseViewModel)}.
     *
     * @see #onBeforeReady(ReadyStatus, BaseModule, BaseViewModel)
     * @see #onReady(ReadyStatus, BaseModule, BaseViewModel)
     * since 3.0.0
     */
    protected void shouldAuthenticate() {
        this.viewModel.authenticate(new AuthenticateHandler() {
            @Override
            public void onAuthenticated() {
                if (!isFragmentAlive()) return;
                onAuthenticateComplete(ReadyStatus.READY, module);
            }

            @Override
            public void onAuthenticationFailed() {
                if (!isFragmentAlive()) return;
                onAuthenticateComplete(ReadyStatus.ERROR, module);
            }
        });
    }
}
