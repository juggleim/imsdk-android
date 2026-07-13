package com.juggle.im.interfaces;

import java.util.List;
import java.util.Map;

public interface IChatroomManager {

    /**
     * Joins a chatroom.
     *
     * @param chatroomId Chatroom ID.
     */
    void joinChatroom(String chatroomId);

    /**
     * Joins a chatroom.
     *
     * @param chatroomId Chatroom ID.
     * @param prevMessageCount Number of historical messages to fetch when joining the chatroom.
     */
    void joinChatroom(String chatroomId, int prevMessageCount);

    /**
     * Joins a chatroom.
     *
     * @param chatroomId Chatroom ID.
     * @param prevMessageCount Number of historical messages to fetch when joining the chatroom.
     * @param isAutoCreate Whether to automatically create the chatroom when it does not exist. Not created by default.
     */
    void joinChatroom(String chatroomId, int prevMessageCount, boolean isAutoCreate);

    /**
     * Quits a chatroom.
     *
     * @param chatroomId Chatroom ID.
     */
    void quitChatroom(String chatroomId);

    /**
     * Sets chatroom attributes.
     *
     * @param chatroomId Chatroom ID.
     * @param attributes Chatroom attributes. Both keys and values are strings. Up to 100 different attributes are supported.
     *                   Keys not set by the current user cannot be operated on by the client and return JErrorCode.CHATROOM_KEY_UNAUTHORIZED.
     * @param callback Completion callback.
     *                 When code returns JErrorCode.NONE, all attributes were set successfully.
     *                 Other codes indicate that some keys failed to be set. All failed keys are returned in the callback with the corresponding error codes, which can be found in the JErrorCode definitions.
     */
    void setAttributes(String chatroomId, Map<String, String> attributes, IChatroomAttributesUpdateCallback callback);

    /**
     * Removes chatroom attributes.
     *
     * @param chatroomId Chatroom ID.
     * @param keys List of attribute keys to delete. Keys not set by the current user cannot be deleted.
     * @param callback Completion callback.
     *                 When code returns JErrorCode.NONE, all attributes were deleted successfully.
     *                 Other codes indicate that some keys failed to be deleted. All failed keys are returned in the callback with the corresponding error codes, which can be found in the JErrorCode definitions.
     */
    void removeAttributes(String chatroomId, List<String> keys, IChatroomAttributesUpdateCallback callback);

    /**
     * Gets all chatroom attributes.
     *
     * @param chatroomId Chatroom ID.
     * @param callback Completion callback.
     */
    void getAllAttributes(String chatroomId, IChatroomAttributesCallback callback);

    void addListener(String key, IChatroomListener listener);

    void removeListener(String key);

    void addAttributesListener(String key, IChatroomAttributesListener listener);

    void removeAttributesListener(String key);

    interface IChatroomAttributesUpdateCallback {
        /**
         * Completion callback.
         *
         * @param errorCode Returns JErrorCode.NONE when all attributes are set successfully.
         *             Other codes indicate that some keys failed to be set. All failed keys are returned in failedKeys.
         * @param failedKeys The key indicates the chatroomId, and the value indicates the corresponding error code.
         */
        void onComplete(int errorCode, Map<String, Integer> failedKeys);
    }

    interface IChatroomAttributesCallback {
        /**
         * Completion callback.
         *
         * @param errorCode Returns JErrorCode.NONE when the fetch succeeds.
         * @param attributes Fetched attribute list.
         */
        void onComplete(int errorCode, Map<String, String> attributes);
    }

    interface IChatroomListener {
        /// Current user joined the chatroom.
        void onChatroomJoin(String chatroomId);
        /// Current user quit the chatroom.
        void onChatroomQuit(String chatroomId);
        /// Failed to join the chatroom.
        void onChatroomJoinFail(String chatroomId, int errorCode);
        /// Failed to quit the chatroom.
        void onChatroomQuitFail(String chatroomId, int errorCode);
        /// Current user was kicked out of the chatroom.
        void onChatroomKick(String chatroomId);
        /// The chatroom was destroyed.
        void onChatroomDestroy(String chatroomId);
    }

    interface IChatroomAttributesListener {
        /// Chatroom attributes were updated, either added or with changed values.
        void onAttributesUpdate(String chatroomId, Map<String, String> attributes);
        /// Chatroom attributes were deleted.
        void onAttributesDelete(String chatroomId, Map<String, String> attributes);
    }
}
