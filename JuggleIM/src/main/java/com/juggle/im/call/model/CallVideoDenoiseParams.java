package com.juggle.im.call.model;

public class CallVideoDenoiseParams {
    public enum CallVideoDenoiseMode {
        OFF(0),
        ON(1),
        AUTO(2);

        private final int value;

        CallVideoDenoiseMode(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }

        public static CallVideoDenoiseMode getCallVideoDenoiseMode(int value) {
            try {
                if (OFF.value == value) {
                    return OFF;
                } else if (ON.value == value) {
                    return ON;
                } else {
                    return AUTO.value == value ? AUTO : null;
                }
            } catch (Exception var2) {
                throw new RuntimeException("The enumeration cannot be found");
            }
        }
    }

    public enum CallVideoDenoiseStrength {
        LIGHT(1),
        MEDIUM(2),
        HEAVY(3);

        private final int value;

        CallVideoDenoiseStrength(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }

        public static CallVideoDenoiseStrength getCallVideoDenoiseStrength(int value) {
            try {
                if (LIGHT.value == value) {
                    return LIGHT;
                } else if (MEDIUM.value == value) {
                    return MEDIUM;
                } else {
                    return HEAVY.value == value ? HEAVY : null;
                }
            } catch (Exception var2) {
                throw new RuntimeException("The enumeration cannot be found");
            }
        }
    }

    public CallVideoDenoiseMode mode;
    public CallVideoDenoiseStrength strength;

    public CallVideoDenoiseParams() {
        this.mode = CallVideoDenoiseMode.OFF;
        this.strength = CallVideoDenoiseStrength.LIGHT;
    }
}
