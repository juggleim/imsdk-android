package com.juggle.im;

import java.util.List;

public class JIMConst {
    public enum ConnectionStatus {
        IDLE(0),
        CONNECTED(1),
        DISCONNECTED(2),
        CONNECTING(3),
        FAILURE(4);
        private final int status;

        ConnectionStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return this.status;
        }

        public static ConnectionStatus setStatus(int status) {
            for (ConnectionStatus s : ConnectionStatus.values()) {
                if (status == s.status) {
                    return s;
                }
            }
            return IDLE;
        }
    }
    public enum PullDirection {
        NEWER, OLDER
    }

    public enum TopConversationsOrderType {
        ORDER_BY_TOP_TIME,
        ORDER_BY_MESSAGE_TIME
    }

    public interface IResultCallback<T> {
        void onSuccess(T data);
        void onError(int errorCode);
    }

    public interface IResultListCallback<T> {
        void onSuccess(List<T> data, boolean isFinish);
        void onError(int errorCode);
    }
}
