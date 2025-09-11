package com.juggle.im.call.internal;

public class CallInternalConst {
    public enum CallEngineType {
        ZEGO(0),
        LIVE_KIT(1),
        AGORA(2);

        CallEngineType(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        private final int mValue;
    }
}
