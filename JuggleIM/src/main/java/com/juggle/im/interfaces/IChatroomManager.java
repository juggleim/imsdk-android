package com.juggle.im.interfaces;

import java.util.List;
import java.util.Map;

public interface IChatroomManager {

    /**
     * 加入聊天室
     *
     * @param chatroomId 聊天室 id
     */
    void joinChatroom(String chatroomId);

    /**
     * 加入聊天室
     *
     * @param chatroomId 聊天室 id
     * @param prevMessageCount 加入聊天室时获取的历史消息数量
     */
    void joinChatroom(String chatroomId, int prevMessageCount);

    /**
     * 加入聊天室
     *
     * @param chatroomId 聊天室 id
     * @param prevMessageCount 加入聊天室时获取的历史消息数量
     * @param isAutoCreate 当聊天室不存在时是否自动创建（默认不创建）
     */
    void joinChatroom(String chatroomId, int prevMessageCount, boolean isAutoCreate);

    /**
     * 退出聊天室
     *
     * @param chatroomId 聊天室 id
     */
    void quitChatroom(String chatroomId);

    /**
     * 设置聊天室属性
     *
     * @param chatroomId 聊天室 id
     * @param attributes 聊天室属性，key 和 value 都是字符串，最多支持设置 100 个不同的属性。
     *                   非当前用户设置的 key 在客户端不能进行操作（返回 JErrorCode.CHATROOM_KEY_UNAUTHORIZED）。
     * @param callback 完成回调
     *                 code 返回 JErrorCode.NONE 时表示所有属性都设置成功。
     *                 其它 code 表示存在设置失败的 key，所有设置失败的 key 都会回调，并返回对应的错误码，可以从 JErrorCode 的定义中找到对应的错误码。
     */
    void setAttributes(String chatroomId, Map<String, String> attributes, IChatroomAttributesUpdateCallback callback);

    /**
     * 删除聊天室属性
     *
     * @param chatroomId 聊天室 id
     * @param keys 待删除的属性 key 列表。非当前用户设置的 key 不能删除。
     * @param callback 完成回调。
     *                 code 返回 JErrorCode.NONE 时表示所有属性都删除成功。
     *                 其它 code 表示存在删除失败的 key，所有删除失败的 key 都会回调，并返回对应的错误码，可以从 JErrorCode 的定义中找到对应的错误码。
     */
    void removeAttributes(String chatroomId, List<String> keys, IChatroomAttributesUpdateCallback callback);

    /**
     * 获取聊天室所有属性
     *
     * @param chatroomId 聊天室 id
     * @param callback 完成回调
     */
    void getAllAttributes(String chatroomId, IChatroomAttributesCallback callback);

    void addListener(String key, IChatroomListener listener);

    void removeListener(String key);

    void addAttributesListener(String key, IChatroomAttributesListener listener);

    void removeAttributesListener(String key);

    interface IChatroomAttributesUpdateCallback {
        /**
         * 完成回调
         *
         * @param errorCode 返回 JErrorCode.NONE 时表示所有属性设置成功
         *             其它 code 表示存在设置失败的 key，所有失败的 key 都会在 failedKeys 中回调。
         * @param failedKeys key 表示 chatroomId，value 表示对应的错误码
         */
        void onComplete(int errorCode, Map<String, Integer> failedKeys);
    }

    interface IChatroomAttributesCallback {
        /**
         * 完成回调
         *
         * @param errorCode 返回 JErrorCode.NONE 时表示获取成功
         * @param attributes 获取回来的属性列表
         */
        void onComplete(int errorCode, Map<String, String> attributes);
    }

    interface IChatroomListener {
        /// 当前用户加入聊天室
        void onChatroomJoin(String chatroomId);
        /// 当前用户退出聊天室
        void onChatroomQuit(String chatroomId);
        /// 加入聊天室失败
        void onChatroomJoinFail(String chatroomId, int errorCode);
        /// 退出聊天室失败
        void onChatroomQuitFail(String chatroomId, int errorCode);
        /// 当前用户被踢出聊天室
        void onChatroomKick(String chatroomId);
        /// 聊天室被销毁
        void onChatroomDestroy(String chatroomId);
    }

    interface IChatroomAttributesListener {
        /// 聊天室属性更新（新增或者 value 有变化）
        void onAttributesUpdate(String chatroomId, Map<String, String> attributes);
        /// 聊天室属性删除
        void onAttributesDelete(String chatroomId, Map<String, String> attributes);
    }
}
