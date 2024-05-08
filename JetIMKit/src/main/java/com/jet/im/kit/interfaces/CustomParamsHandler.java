package com.jet.im.kit.interfaces;

import androidx.annotation.NonNull;

import com.sendbird.android.params.FileMessageCreateParams;
import com.sendbird.android.params.GroupChannelCreateParams;
import com.sendbird.android.params.GroupChannelUpdateParams;
import com.sendbird.android.params.MultipleFilesMessageCreateParams;
import com.sendbird.android.params.OpenChannelCreateParams;
import com.sendbird.android.params.OpenChannelUpdateParams;
import com.sendbird.android.params.UserMessageCreateParams;
import com.sendbird.android.params.UserMessageUpdateParams;

/**
 * Interface definition for a callback to be invoked before when each operation called.
 *
 * since 1.2.2
 */
public interface CustomParamsHandler {

    /**
     * It will be called before creating group channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link GroupChannelCreateParams}.
     * since 1.2.2
     */
    default void onBeforeCreateGroupChannel(@NonNull GroupChannelCreateParams params) {}

    /**
     * It will be called before updating group channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link GroupChannelUpdateParams}.
     * since 1.2.2
     */
    default void onBeforeUpdateGroupChannel(@NonNull GroupChannelUpdateParams params) {}

    /**
     * It will be called before sending message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of user message. Refer to {@link UserMessageCreateParams}.
     * since 1.2.2
     */
    default void onBeforeSendUserMessage(@NonNull UserMessageCreateParams params) {}

    /**
     * It will be called before sending message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of file message. Refer to {@link FileMessageCreateParams}.
     * since 1.2.2
     */
    default void onBeforeSendFileMessage(@NonNull FileMessageCreateParams params) {}

    /**
     * It will be called before sending multiple files message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of multiple files message. Refer to {@link MultipleFilesMessageCreateParams}.
     * since 3.9.0
     */
    default void onBeforeSendMultipleFilesMessage(@NonNull MultipleFilesMessageCreateParams params) {}

    /**
     * It will be called before updating message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of user message. Refer to {@link UserMessageUpdateParams}.
     * since 1.2.2
     */
    default void onBeforeUpdateUserMessage(@NonNull UserMessageUpdateParams params) {}

    /**
     * It will be called before updating open channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link OpenChannelUpdateParams}.
     * since 2.0.0
     */
    default void onBeforeUpdateOpenChannel(@NonNull OpenChannelUpdateParams params) {}

    /**
     * It will be called before creating open channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link OpenChannelCreateParams}.
     * since 3.2.0
     */
    default void onBeforeCreateOpenChannel(@NonNull OpenChannelCreateParams params) {}
}
