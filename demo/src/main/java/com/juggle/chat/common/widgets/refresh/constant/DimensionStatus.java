package com.juggle.chat.common.widgets.refresh.constant;

/** state，  */
@SuppressWarnings("WeakerAccess")
public class DimensionStatus {

    public static final DimensionStatus DefaultUnNotify =
            new DimensionStatus(0, false); // Default，Notification
    public static final DimensionStatus Default = new DimensionStatus(1, true); // Default
    public static final DimensionStatus XmlWrapUnNotify =
            new DimensionStatus(2, false); // Xml，Notification
    public static final DimensionStatus XmlWrap = new DimensionStatus(3, true); // Xml
    public static final DimensionStatus XmlExactUnNotify =
            new DimensionStatus(4, false); // Xml view ，Notification
    public static final DimensionStatus XmlExact = new DimensionStatus(5, true); // Xml view
    public static final DimensionStatus XmlLayoutUnNotify =
            new DimensionStatus(6, false); // Xml layout ，Notification
    public static final DimensionStatus XmlLayout = new DimensionStatus(7, true); // Xml layout
    public static final DimensionStatus CodeExactUnNotify =
            new DimensionStatus(8, false); // ，Notification
    public static final DimensionStatus CodeExact = new DimensionStatus(9, true); //
    public static final DimensionStatus DeadLockUnNotify =
            new DimensionStatus(10, false); // ，Notification
    public static final DimensionStatus DeadLock = new DimensionStatus(10, true); //

    public final int ordinal;
    public final boolean notified;

    public static final DimensionStatus[] values =
            new DimensionStatus[] {
                DefaultUnNotify,
                Default,
                XmlWrapUnNotify,
                XmlWrap,
                XmlExactUnNotify,
                XmlExact,
                XmlLayoutUnNotify,
                XmlLayout,
                CodeExactUnNotify,
                CodeExact,
                DeadLockUnNotify,
                DeadLock
            };

    private DimensionStatus(int ordinal, boolean notified) {
        this.ordinal = ordinal;
        this.notified = notified;
    }

    /**
     * Notificationstate
     *
     * @return Notificationstate
     */
    public DimensionStatus unNotify() {
        if (notified) {
            DimensionStatus prev = values[ordinal - 1];
            if (!prev.notified) {
                return prev;
            }
            return DefaultUnNotify;
        }
        return this;
    }

    /**
     * Notificationstate
     *
     * @return Notificationstate
     */
    public DimensionStatus notified() {
        if (!notified) {
            return values[ordinal + 1];
        }
        return this;
    }

    /**
     * state
     *
     * @param status
     * @return
     */
    public boolean canReplaceWith(DimensionStatus status) {
        return ordinal < status.ordinal
                || ((!notified || CodeExact == this) && ordinal == status.ordinal);
    }

    //    /**
    //     * state
    //     * @param status
    //     * @return  gte
    //     */
    //    public boolean gteStatusWith(DimensionStatus status) {
    //        return ordinal() >= status.ordinal();
    //    }
}
