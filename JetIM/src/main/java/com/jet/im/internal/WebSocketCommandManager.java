package com.jet.im.internal;

import com.jet.im.internal.core.network.IWebSocketCallback;
import com.jet.im.internal.util.JSimpleTimer;
import com.jet.im.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ye_Guli
 * @create 2024-05-11 16:33
 * <p>
 * 分两部分逻辑，信令超时和消息重发
 * 信令超时（包含发消息的）（超时时间 5 s）
 * WebSocketCommandManager（对应图中的 MessageTimeoutManager），把 mCmdCallbackMap 放进去，改造一下，加上时间戳。WebSocketCommandManager 暴露 put 和 remove 接口给 JWebSocket 使用，暴露一个 CommandListnener 给 JWebSocket 设置，用来回调 onTimeOut
 * WebSocketCommandManager 维护一个常驻 Timer，5s 检测一次，检查 mCmdCallbackMap 里面是否有超时的 index，超时则 remove 对应的 index，直接回调 JWebSocket onTimeOut，JWebSocket 直接回调上层 onError(OPERATION_TIMEOUT)
 * 要补充一个逻辑：在 ConnectionManager 里调用 stopHeartbeat 的这个时机（也就是长连接从 connected 到断开的这个时机），调用 JWebSocket 的接口 pushRemainCmdAndCallbackError，把 mCmdCallbackMap 剩余所有的 cmd 取出来回调 onError(CONNECTION_UNAVAILABLE)
 * <p>
 * 重发：可以做到业务层
 */
public class WebSocketCommandManager {
    private final static String TAG = WebSocketCommandManager.class.getSimpleName();
    private final static int COMMAND_TIME_OUT = 5 * 1000;
    private final static int COMMAND_DETECTION_INTERVAL = 5 * 1000;

    private final CommandTimeoutListener mCommandListener;
    private final ConcurrentHashMap<Integer, Long> mCmdCallbackTimestampMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, IWebSocketCallback> mCmdCallbackMap = new ConcurrentHashMap<>();

    private JSimpleTimer mCommandDetectionTimer = null;
    private boolean mIsInit = false;
    private boolean mIsRunning = false;

    public WebSocketCommandManager(CommandTimeoutListener mCommandListener) {
        this.mCommandListener = mCommandListener;
        init();
    }

    public boolean isInit() {
        return mIsInit;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public void start(boolean immediately) {
        stop();
        mCommandDetectionTimer.start(immediately);
        mIsRunning = true;
    }

    public void stop() {
        mCommandDetectionTimer.stop();
        mIsRunning = false;
    }

    public void putCommand(Integer mCmdIndex, IWebSocketCallback callback) {
        if (mCmdIndex == null || callback == null) {
            LoggerUtils.d(TAG + ", putCommand failed, mCmdIndex= " + mCmdIndex + ", callback= " + callback);
            return;
        }
        if (mCmdCallbackMap.get(mCmdIndex) != null) {
            LoggerUtils.d(TAG + ", putCommand failed, the mCmdIndex is already added, mCmdIndex= " + mCmdIndex + ", callback= " + callback);
            return;
        }
        LoggerUtils.d(TAG + ", putCommand success, mCmdIndex= " + mCmdIndex + ", callback= " + callback);
        mCmdCallbackMap.put(mCmdIndex, callback);
        mCmdCallbackTimestampMap.put(mCmdIndex, System.currentTimeMillis());
    }

    public IWebSocketCallback removeCommand(Integer mCmdIndex) {
        mCmdCallbackTimestampMap.remove(mCmdIndex);
        IWebSocketCallback removedCallback = mCmdCallbackMap.remove(mCmdIndex);
        LoggerUtils.d(TAG + ", removeCommand success, mCmdIndex= " + mCmdIndex + ", removedCallback= " + removedCallback);
        return removedCallback;
    }

    public synchronized ArrayList<IWebSocketCallback> clearCommand() {
        ArrayList<IWebSocketCallback> list = new ArrayList<>(mCmdCallbackMap.values());
        this.mCmdCallbackMap.clear();
        this.mCmdCallbackTimestampMap.clear();
        return list;
    }

    public int size() {
        return mCmdCallbackMap.size();
    }

    private void init() {
        if (mIsInit) return;

        mCommandDetectionTimer = new JSimpleTimer(COMMAND_DETECTION_INTERVAL) {
            @Override
            protected void doAction() {
                ArrayList<IWebSocketCallback> realTimeoutMessages = doCommandDetection();
                afterCommandDetection(realTimeoutMessages);
            }
        };
        mCommandDetectionTimer.init();

        mIsInit = true;
    }

    private ArrayList<IWebSocketCallback> doCommandDetection() {
        LoggerUtils.d(TAG + ", command detection executing, the cmdCallbackMap.size= " + mCmdCallbackMap.size());
        ArrayList<IWebSocketCallback> timeoutMessages = new ArrayList<>();
        try {
            for (Integer key : mCmdCallbackMap.keySet()) {
                final IWebSocketCallback callback = mCmdCallbackMap.get(key);
                if (callback == null) {
                    LoggerUtils.d(TAG + ", command detection executing, removeCommand because the callback is null, mCmdIndex= " + key);
                    removeCommand(key);
                } else {
                    Long sendMessageTimestamp = mCmdCallbackTimestampMap.get(key);
                    long delta = System.currentTimeMillis() - (sendMessageTimestamp == null ? 0 : sendMessageTimestamp);
                    if (delta > COMMAND_TIME_OUT) {
                        LoggerUtils.d(TAG + ", command detection executing, removeCommand because the command is timeout, mCmdIndex= " + key + ", callback= " + callback);
                        timeoutMessages.add(callback);
                        removeCommand(key);
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtils.d(TAG + ", command detection error, exception= " + e.getMessage());
        }
        return timeoutMessages;
    }

    private void afterCommandDetection(ArrayList<IWebSocketCallback> timeoutMessages) {
        if (timeoutMessages != null && timeoutMessages.size() > 0) {
            for (int i = 0; i < timeoutMessages.size(); i++) {
                notifyCommandTimeout(timeoutMessages.get(i));
            }
        }
    }

    private void notifyCommandTimeout(IWebSocketCallback callback) {
        if (mCommandListener != null) {
            mCommandListener.onCommandTimeOut(callback);
        }
    }

    public interface CommandTimeoutListener {
        void onCommandTimeOut(IWebSocketCallback callback);
    }
}