package com.jet.im.kit.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.res.ResourcesCompat;

import com.sendbird.android.user.User;
import com.jet.im.kit.activities.adapter.MutableBaseAdapter;
import com.jet.im.kit.interfaces.OnMentionEventListener;
import com.jet.im.kit.internal.ui.widgets.ListPopupDialog;
import com.jet.im.kit.internal.ui.widgets.MentionWatcher;
import com.jet.im.kit.internal.ui.widgets.ThemeableSnackbar;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.MentionSpan;
import com.jet.im.kit.model.TextUIConfig;
import com.jet.im.kit.model.UserMentionConfig;
import com.jet.im.kit.utils.UserUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * EditText with Mention feature. This EditText detects the trigger keyword and show a list of suggested users for mention.
 * Plus, If the suggested user is selected, the selected user will be markup as mentioned-text and created as a mention data.
 *
 * since 3.0.0
 */
public class MentionEditText extends AppCompatEditText {
    final private int FLAG_NO_SPELLING_SUGGESTION = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
    @NonNull
    private final AtomicBoolean isDelKeyEventAlreadyHandled = new AtomicBoolean(false);
    @NonNull
    private final ListPopupDialog<User> suggestionDialog;

    @NonNull
    private final ThemeableSnackbar snackbar;
    @Nullable
    private UserMentionConfig mentionConfig;
    @Nullable
    private MentionWatcher mentionWatcher;

    private int originalInputType;

    public MentionEditText(@NonNull Context context) {
        this(context, null);
    }

    public MentionEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.editTextStyle);
    }

    public MentionEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.suggestionDialog = new ListPopupDialog<>(context);
        this.snackbar = new ThemeableSnackbar(context);
        this.originalInputType = getInputType();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.snackbar.init((View) getParent());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (suggestionDialog.isShowing()) {
            suggestionDialog.dismiss();
        }

        snackbar.dismiss();
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (this.mentionConfig != null) {
            post(() -> {
                updateInputType(selStart, selEnd);
                updateSpan(selStart, selEnd);
                lookupMention();
            });
        }
    }

    private boolean hasNoSuggestionFlag() {
        return (getInputType() & FLAG_NO_SPELLING_SUGGESTION) == FLAG_NO_SPELLING_SUGGESTION;
    }

    private void lookupMention() {
        final Editable text = getText();
        if (this.mentionWatcher != null && text != null) {
            this.mentionWatcher.findMention(text);
        }
    }

    private void updateInputType(int selStart, int selEnd) {
        if (selStart == selEnd) {
            final Editable text = getText();
            int inputType = getInputType();
            final Typeface typeFace = getTypeface();

            int offset = selStart;
            if (text != null) {
                offset = text.toString().lastIndexOf(" ", selStart - 1);
                if (offset < 0) {
                    offset = selStart;
                } else {
                    offset = Math.min(offset + 1, text.length());
                }
            }
            final MentionSpan startMentionSpan = getMentionSpanAtOffset(offset);
            if (startMentionSpan != null) {
                if (!hasNoSuggestionFlag()) {
                    originalInputType = inputType;
                    setInputType(inputType | FLAG_NO_SPELLING_SUGGESTION);
                    setTypeface(typeFace);
                }
            } else {
                if (hasNoSuggestionFlag()) {
                    setInputType(originalInputType);
                    setTypeface(typeFace);
                }
            }
        }
    }

    private void updateSpan(int selStart, int selEnd) {
        Logger.d("++ update span : selStart=%d, selEnd=%d", selStart, selEnd);
        final Editable text = getText();
        if (text == null) return;

        boolean selChanged = false;
        int start = selStart;
        int end = selEnd;

        final MentionSpan startMentionSpan = getMentionSpanAtOffset(selStart);
        final int startMentionStartPosition = text.getSpanStart(startMentionSpan);
        final int startMentionEndPosition = text.getSpanEnd(startMentionSpan);
        if (startMentionStartPosition < selStart && selStart < startMentionEndPosition) {
            start = startMentionStartPosition;
            selChanged = true;
        }

        boolean isAlreadySelected = selStart != selEnd;
        if (isAlreadySelected) {
            final MentionSpan endMentionSpan = getMentionSpanAtOffset(selEnd);
            final int endMentionStartPosition = text.getSpanStart(endMentionSpan);
            final int endMentionEndPosition = text.getSpanEnd(endMentionSpan);
            if (endMentionStartPosition < selEnd && selEnd < endMentionEndPosition) {
                end = endMentionEndPosition;
                selChanged = true;
            }
        }
        if (selChanged) {
            setSelection(start, end);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (!isDelKeyEventAlreadyHandled.getAndSet(false) && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
            Logger.d("__ onKeyDown keycode = %s", event.getKeyCode());
            boolean handled = onBackspacePressed();
            if (handled) {
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Nullable
    @Override
    public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
        final InputConnection connection = super.onCreateInputConnection(outAttrs);
        if (connection == null) return null;
        return new InputConnectionWrapper(connection, true) {
            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    Logger.d("__ keycode del = %s", event.getKeyCode());
                    isDelKeyEventAlreadyHandled.set(true);
                    boolean handled = onBackspacePressed();
                    if (handled) {
                        return true;
                    }
                }
                return super.sendKeyEvent(event);
            }

            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                Logger.d("__ deleteSurroundingText beforeLength = %s, afterLength=%s", beforeLength, afterLength);
                if (beforeLength == 1 && afterLength == 0) {
                    return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                }

                return super.deleteSurroundingText(beforeLength, afterLength);
            }
        };
    }

    /**
     * Return the MentionSpan to which the character at index belongs
     *
     * @param index Offset of mention span to get
     * @return Then markup object with mention information
     * since 3.0.0
     */
    @Nullable
    public MentionSpan getMentionSpanAtOffset(int index) {
        final Editable text = getText();
        if (text == null) return null;

        final MentionSpan[] spans = text.getSpans(index, index, MentionSpan.class);
        return (spans != null && spans.length > 0) ? spans[0] : null;
    }

    private boolean onBackspacePressed() {
        final int cursorStart = getSelectionStart();
        final int cursorEnd = getSelectionEnd();
        if (cursorStart == cursorEnd) {
            final Editable buffer = getText();
            if (buffer != null && buffer.length() > 0) {
                MentionSpan[] span = buffer.getSpans(cursorStart, cursorEnd, MentionSpan.class);
                if (span.length > 0) {
                    int start = buffer.getSpanStart(span[0]);
                    int end = buffer.getSpanEnd(span[0]);
                    buffer.replace(start, end, "");
                    buffer.removeSpan(span[0]);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Notifies change of a list of suggested user for mention.
     *
     * @param suggestedMentionList The updated suggested user list for mention
     * since 3.0.0
     */
    public void notifySuggestedMentionDataChanged(@NonNull List<User> suggestedMentionList) {
        if (suggestedMentionList.isEmpty()) {
            if (suggestionDialog.isShowing()) {
                suggestionDialog.dismiss();
            }

            snackbar.dismiss();
            return;
        }

        if (getText() == null || mentionConfig == null) return;
        final MentionSpan[] spans = getText().getSpans(0, getText().length(), MentionSpan.class);
        if (spans.length >= mentionConfig.getMaxMentionCount()) {
            snackbar.show();
            return;
        }
        snackbar.dismiss();
        suggestionDialog.update((View) getParent(), suggestedMentionList);
        suggestionDialog.setScrollPosition(0);
    }

    /**
     * Sets the adapter to be used to a suggested mention popup dialog when the trigger is detected.
     *
     * @param adapter The adapter for a list of suggested users for mention
     * since 3.0.0
     */
    public void setSuggestedMentionListAdapter(@NonNull MutableBaseAdapter<User> adapter) {
        suggestionDialog.setAdapter(adapter);
    }

    /**
     * Sets whether to use divider for a suggested mention popup dialog when the trigger is detected.
     *
     * @param useDivider If <code>true</code> divider will be shown in a dialog, <code>false</code> other wise.
     * since 3.0.0
     */
    public void setUseSuggestedMentionListDivider(boolean useDivider) {
        suggestionDialog.setUseDivider(useDivider);
    }

    /**
     * Binds the configuration for mention and the callback for mention to this EditText.
     *
     * @param config The configuration for mention to be applied for this class
     * @param handler The callback that will run when a mentioned text is detected
     * since 3.0.0
     */
    public void bindUserMention(@NonNull UserMentionConfig config, @NonNull TextUIConfig mentionUIConfig, @NonNull OnMentionEventListener handler) {
        this.snackbar.setMaxMentionCount(config.getMaxMentionCount());
        this.mentionConfig = config;
        this.mentionWatcher = new MentionWatcher(this, config, (isDetected, detectedKeyword) -> {
            Logger.d(">> onMentionTextDetectStateChanged(), isDetected=%s, text=%s", isDetected, detectedKeyword);
            if (!isDetected) {
                Logger.d("++ dismiss suggestion dialog if you needed!!");
                if (suggestionDialog.isShowing()) {
                    suggestionDialog.dismiss();
                }
                snackbar.dismiss();
            }
            Logger.d(" onMentionedTextDetected, keyword=%s", detectedKeyword);
            handler.onMentionedTextDetected(detectedKeyword);
        });

        this.suggestionDialog.setOnItemClickListener((view, position, user) -> {
            final String nickname = UserUtils.getDisplayName(getContext(), user);
            Logger.d("++ position=%s, nickname=%s, id=%s", position, nickname, user.getUserId());

            final int startCursorPosition = getSelectionStart();
            final int endCursorPosition = getSelectionEnd();
            final Editable text = getText();
            final String token = config.getTrigger();
            if (text == null) return;

            int index = MentionWatcher.findTriggerIndex(this, config.getTrigger(), config.getDelimiter(), startCursorPosition);
            if (index >= 0) {
                MentionSpan mentionSpan = new MentionSpan(getContext(), token, nickname, user, mentionUIConfig);
                final SpannableString mentionText = new SpannableString(mentionSpan.getDisplayText());
                mentionText.setSpan(mentionSpan, 0, mentionText.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.replace(index, endCursorPosition, TextUtils.concat(mentionText, config.getDelimiter()));
                setSelection(index + mentionSpan.getLength() + 1);
            }
        });
    }

    /**
     * Returns the mentioned-template text on this EditText.
     *
     * @return The text indicating that mentions are included
     * since 3.0.0
     */
    @NonNull
    public CharSequence getMentionedTemplate() {
        CharSequence result = "";
        final Editable mentionedTemplateText = Editable.Factory.getInstance().newEditable(getText());
        final MentionSpan[] spans = mentionedTemplateText.getSpans(0, mentionedTemplateText.length(), MentionSpan.class);
        if (spans.length > 0) {
            for (MentionSpan span : spans) {
                int start = mentionedTemplateText.getSpanStart(span);
                int end = mentionedTemplateText.getSpanEnd(span);
                mentionedTemplateText.replace(start, end, span.getTemplateText());
            }
            result = mentionedTemplateText;
        }
        return result;
    }

    /**
     * Returns the list of mentioned-users on this EditText.
     *
     * @return The list of mentioned users
     * since 3.0.0
     */
    @NonNull
    public List<User> getMentionedUsers() {
        if (getText() == null) return Collections.emptyList();
        final MentionSpan[] spans = getText().getSpans(0, getText().length(), MentionSpan.class);
        final List<User> mentionedUsers = new ArrayList<>();
        for (MentionSpan span : spans) {
            mentionedUsers.add(span.getMentionedUser());
        }
        return mentionedUsers;
    }

    public void applyTextUIConfig(@NonNull TextUIConfig textUIConfig) {
        if (textUIConfig.getTextColor() != TextUIConfig.UNDEFINED_RESOURCE_ID) {
            getPaint().setColor(textUIConfig.getTextColor());
        }
        if (textUIConfig.getTextStyle() != TextUIConfig.UNDEFINED_RESOURCE_ID) {
            getPaint().setTypeface(textUIConfig.generateTypeface());
        }
        if (textUIConfig.getTextSize() != TextUIConfig.UNDEFINED_RESOURCE_ID) {
            getPaint().setTextSize(textUIConfig.getTextSize());
        }
        if (textUIConfig.getTextBackgroundColor() != TextUIConfig.UNDEFINED_RESOURCE_ID) {
            getPaint().bgColor = textUIConfig.getTextBackgroundColor();
        }

        if (textUIConfig.getCustomFontRes() != TextUIConfig.UNDEFINED_RESOURCE_ID) {
            try {
                final Typeface font = ResourcesCompat.getFont(getContext(), textUIConfig.getCustomFontRes());
                if (font != null) {
                    getPaint().setUnderlineText(false);
                    getPaint().setTypeface(font);
                }
            } catch (Resources.NotFoundException ignore) {
            }
        }
    }
}
