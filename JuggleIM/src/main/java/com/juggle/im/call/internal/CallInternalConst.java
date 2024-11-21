package com.juggle.im.call.internal;

public class CallInternalConst {
    public enum CallEngineType {
        ZEGO(0),
        AGORA(1);

        CallEngineType(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        private final int mValue;
    }
}
