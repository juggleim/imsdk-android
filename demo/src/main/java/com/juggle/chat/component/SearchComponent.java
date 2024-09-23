package com.juggle.chat.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.juggle.chat.R;

public class SearchComponent extends BaseComponent {

    private LinearLayout searchLayout;
    private ImageView clearButton;
    private EditText searchEditText;
    private OnSearchQueryListener onSearchQueryListener;
    private OnClickListener onSearchClickListener;
    private boolean isSearchComponentClickable;

    public SearchComponent(@NonNull Context context) {
        super(context);
    }

    public SearchComponent(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchComponent(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View onCreateView(
            Context context,
            LayoutInflater inflater,
            @NonNull ViewGroup parent,
            AttributeSet attrs) {
        View view = inflater.inflate(R.layout.rc_search_component, parent, false);
        searchLayout = view.findViewById(R.id.layout_search);
        searchEditText = view.findViewById(R.id.et_search);
        clearButton = view.findViewById(R.id.iv_clear);

        // 读取自定义属性
        if (attrs != null) {
            TypedArray a =
                    context.getTheme()
                            .obtainStyledAttributes(attrs, R.styleable.SearchComponent, 0, 0);
            try {
                isSearchComponentClickable =
                        a.getBoolean(R.styleable.SearchComponent_search_component_clickable, false);
            } finally {
                a.recycle();
            }
        }

        FrameLayout flSearch = view.findViewById(R.id.fl_search);
        flSearch.setOnClickListener(
                v -> {
                    if (isSearchComponentClickable) {
                        if (onSearchClickListener != null) {
                            onSearchClickListener.onClick(v);
                        }
                    } else {
                        searchEditText.requestFocus();
                        showKeyboard(searchEditText);
                    }
                });

        if (!isSearchComponentClickable) {
            clearButton.setOnClickListener(v -> searchEditText.setText(""));
            searchEditText.setOnEditorActionListener(
                    new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                                if (onSearchQueryListener != null) {
                                    onSearchQueryListener.onClickSearch(v.getText().toString());
                                }
                                return true;
                            }
                            return false;
                        }
                    });
            searchEditText.setOnFocusChangeListener(
                    (v, hasFocus) -> {
                        updateSearchLayoutPosition(hasFocus);
                    });

            searchEditText.addTextChangedListener(
                    new TextWatcher() {
                        @Override
                        public void beforeTextChanged(
                                CharSequence s, int start, int count, int after) {
                            // Do nothing
                        }

                        @Override
                        public void onTextChanged(
                                CharSequence s, int start, int before, int count) {
                            if (onSearchQueryListener != null) {
                                onSearchQueryListener.onSearch(s.toString());
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            boolean hasText = s.length() > 0;
                            clearButton.setVisibility(hasText ? VISIBLE : GONE);
                        }
                    });
        } else {
            searchEditText.setFocusable(false);
            searchEditText.setCursorVisible(false);
        }

        return view;
    }

    /**
     * 设置搜索框点击事件监听器
     */
    public void setSearchClickListener(@NonNull OnClickListener onSearchClickListener) {
        this.onSearchClickListener = onSearchClickListener;
    }

    /**
     * 设置搜索监听器
     *
     * @param listener 搜索监听器
     */
    public void setSearchQueryListener(OnSearchQueryListener listener) {
        this.onSearchQueryListener = listener;
    }

    public interface OnSearchQueryListener {
        /**
         * 搜索回调
         *
         * @param query 搜索关键字
         */
        void onSearch(String query);

        default void onClickSearch(String query) {
        }
    }

    /**
     * 根据是否输入文本或焦点来更新搜索布局的位置
     *
     * @param alignLeft 如果为 true，布局左对齐；否则居中
     */
    private void updateSearchLayoutPosition(boolean alignLeft) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) searchLayout.getLayoutParams();
        params.gravity = alignLeft ? Gravity.START | Gravity.CENTER_VERTICAL : Gravity.CENTER;
        searchLayout.setLayoutParams(params);
    }

    /**
     * 强制显示键盘
     *
     * @param editText 获取焦点的 EditText
     */
    private void showKeyboard(EditText editText) {
        if (editText == null) {
            return;
        }
        InputMethodManager imm =
                (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
