package com.juggle.chat.common.widgets;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.juggle.chat.R;


public class SimpleInputDialog extends DialogFragment implements View.OnClickListener {
    private EditText inputEt;
    private TextView confirmTv;
    private TextView cancelTv;

    private TextView titleTv;
    private String hintText;
    private String confirmText;
    private String cancelText;
    private String titleText;
    private InputDialogListener inputDialogListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 透明化背景
        //        if (getDialog() != null && getDialog().getWindow() != null) {
        //            Window window = getDialog().getWindow();
        //            WindowManager.LayoutParams attributes = window.getAttributes();
        //            attributes.width = ScreenUtils.dip2px(getContext(), 280);
        //            attributes.height = ScreenUtils.dip2px(getContext(), 200);
        //            window.setAttributes(attributes);
        //            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //        }
        // 背景色

    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View contentView =
                inflater.inflate(R.layout.rc_common_dialog_simple_input, container, false);
        inputEt = contentView.findViewById(R.id.common_et_dialog_input);
        confirmTv = contentView.findViewById(R.id.common_tv_dialog_confirm);
        cancelTv = contentView.findViewById(R.id.common_tv_dialog_cancel);
        titleTv = contentView.findViewById(R.id.common_tv_title);
        confirmTv.setOnClickListener(this);
        cancelTv.setOnClickListener(this);

        if (!TextUtils.isEmpty(hintText)) {
            inputEt.setHint(hintText);
        }

        if (!TextUtils.isEmpty(confirmText)) {
            confirmTv.setText(confirmText);
        }

        if (!TextUtils.isEmpty(cancelText)) {
            cancelTv.setText(cancelText);
        }

        if (!TextUtils.isEmpty(titleText)) {
            titleTv.setText(titleText);
        }
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        return contentView;
    }

    public void setInputHint(String hint) {
        hintText = hint;
    }

    public void setConfirmText(String confirmText) {
        this.confirmText = confirmText;
    }

    public void setCancelText(String cancelText) {
        this.cancelText = cancelText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public void setInputDialogListener(InputDialogListener listener) {
        this.inputDialogListener = listener;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.common_tv_dialog_confirm) {
            boolean isClose = true;
            if (inputDialogListener != null) {
                isClose = inputDialogListener.onConfirmClicked(inputEt);
            }
            if (isClose) {
                dismiss();
            }
        } else if (id == R.id.common_tv_dialog_cancel) {
            dismiss();
        }
    }

    public interface InputDialogListener {
        /**
         * 当点击确认时回调输入内容
         *
         * @return 返回 false 时，不关闭对话框，返回 true时关闭对话框
         */
        boolean onConfirmClicked(EditText input);
    }
}
