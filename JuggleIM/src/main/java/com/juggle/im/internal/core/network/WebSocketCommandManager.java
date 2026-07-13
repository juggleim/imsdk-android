package com.juggle.im.internal.core.network;

import com.juggle.im.internal.core.network.wscallback.IWebSocketCallback;
import com.juggle.im.internal.util.JSimpleTimer;
import com.juggle.im.internal.util.JLogger;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ye_Guli
 * @create 2024-05-11 16:33
 * <p>
 * Two parts of logic: command timeout and message resend
 * Command timeout (including message sending), timeout is 5 seconds
 * WebSocketCommandManager (MessageTimeoutManager in the diagram) stores mCmdCallbackMap with timestamps. It exposes put and remove APIs for JWebSocket and exposes a CommandListnener for JWebSocket to set so onTimeOut can be called back
 * WebSocketCommandManager keeps a resident Timer that checks every 5 seconds whether mCmdCallbackMap contains timed-out indexes. If one times out, it removes the index and calls back JWebSocket onTimeOut; JWebSocket then calls upper-layer onError(OPERATION_TIMEOUT)
 * Add one more flow: when ConnectionManager calls stopHeartbeat, meaning the persistent connection moves from connected to disconnected, call JWebSocket.pushRemainCmdAndCallbackError to take all remaining cmds from mCmdCallbackMap and callback onError(CONNECTION_UNAVAILABLE)
 * <p>
 * Resend can be handled in the business layer
 */
public class WebSocketCommandManager {
    private final static String TAG = "WS-Command";
    private final static int COMMAND_TIME_OUT = 8 * 1000;
    private final static int COMMAND_DETECTION_INTERVAL = 8 * 1000;

    private final CommandTimeoutListener mCommandListener;
    private final ConcurrentHashMap<Integer, Long> mCmdCallbackTimestampMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, IWebSocketCallback> mCmdCallbackMap = new ConcurrentHashMap<>();

    private JSimpleTimer mCommandDetectionTimer = null;
    private boolean mIsInit = false;
    private boolean mIsRunning = false;
    private final AtomicInteger mTimeoutCount = new AtomicInteger(0);

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
            JLogger.e(TAG, "putCommand failed, mCmdIndex= " + mCmdIndex + ", callback= " + callback);
            return;
        }
        if (mCmdCallbackMap.get(mCmdIndex) != null) {
            JLogger.e(TAG, "putCommand failed, the mCmdIndex is already added, mCmdIndex= " + mCmdIndex + ", callback= " + callback);
            return;
        }
        JLogger.v(TAG, "putCommand success, mCmdIndex= " + mCmdIndex + ", callback= " + callback);
        mCmdCallbackMap.put(mCmdIndex, callback);
        mCmdCallbackTimestampMap.put(mCmdIndex, System.currentTimeMillis());
    }

    public IWebSocketCallback removeCommand(Integer mCmdIndex) {
        if (mCmdIndex == null) {
            return null;
        }
        mTimeoutCount.set(0);
        mCmdCallbackTimestampMap.remove(mCmdIndex);
        IWebSocketCallback removedCallback = mCmdCallbackMap.remove(mCmdIndex);
        JLogger.v(TAG, "removeCommand success, mCmdIndex= " + mCmdIndex + ", removedCallback= " + removedCallback);
        return removedCallback;
    }

    public synchronized ArrayList<IWebSocketCallback> clearCommand() {
        mTimeoutCount.set(0);
        ArrayList<IWebSocketCallback> commandList = new ArrayList<>(mCmdCallbackMap.values());
        this.mCmdCallbackMap.clear();
        this.mCmdCallbackTimestampMap.clear();
        JLogger.v(TAG, "clearCommand success, the commandList.size= " + commandList.size());
        return commandList;
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
                if (!realTimeoutMessages.isEmpty()) {
                    afterCommandDetection(realTimeoutMessages);
                    mTimeoutCount.addAndGet(realTimeoutMessages.size());
                    JLogger.i("CMD-Detect", "timeOutCount is " + mTimeoutCount);
                    if (mTimeoutCount.get() > 3) {
                        mCommandListener.onTimeoutCountExceed();
                        mTimeoutCount.set(0);
                    }
                }
            }
        };
        mCommandDetectionTimer.init();

        mIsInit = true;
    }

    private ArrayList<IWebSocketCallback> doCommandDetection() {
        JLogger.v(TAG, "command detection executing, the cmdCallbackMap.size= " + mCmdCallbackMap.size());
        ArrayList<IWebSocketCallback> timeoutMessages = new ArrayList<>();
        try {
            for (Integer key : mCmdCallbackMap.keySet()) {
                final IWebSocketCallback callback = mCmdCallbackMap.get(key);
                if (callback == null) {
                    JLogger.e(TAG, "command detection executing, removeCommand because the callback is null, mCmdIndex= " + key);
                    removeCommand(key);
                } else {
                    Long sendMessageTimestamp = mCmdCallbackTimestampMap.get(key);
                    long delta = System.currentTimeMillis() - (sendMessageTimestamp == null ? 0 : sendMessageTimestamp);
                    if (delta > COMMAND_TIME_OUT) {
                        JLogger.e(TAG, "command detection executing, removeCommand because the command is timeout, mCmdIndex= " + key + ", callback= " + callback);
                        timeoutMessages.add(callback);
                        removeCommand(key);
                    }
                }
            }
        } catch (Exception e) {
            JLogger.e(TAG, "command detection error, exception= " + e.getMessage());
        }
        return timeoutMessages;
    }

    private void afterCommandDetection(ArrayList<IWebSocketCallback> timeoutMessages) {
        if (timeoutMessages != null && !timeoutMessages.isEmpty()) {
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
        void onTimeoutCountExceed();
    }
}