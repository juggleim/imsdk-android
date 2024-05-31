package com.jet.im.internal.logger.action;

/**
 * @author Ye_Guli
 * @create 2024-05-23 9:40
 */
class RemoveExpiredAction implements IAction {
    long mDeleteTime;

    public RemoveExpiredAction(Builder builder) {
        this.mDeleteTime = builder.mDeleteTime;
    }

    @Override
    public boolean isValid() {
        if (mDeleteTime <= 0) return false;
        return true;
    }

    @Override
    @ActionTypeEnum.ActionType
    public int getType() {
        return ActionTypeEnum.TYPE_REMOVE_EXPIRED;
    }

    static class Builder {
        long mDeleteTime;

        public Builder setDeleteTime(long mDeleteTime) {
            this.mDeleteTime = mDeleteTime;
            return this;
        }

        public RemoveExpiredAction build() {
            return new RemoveExpiredAction(this);
        }
    }
}
