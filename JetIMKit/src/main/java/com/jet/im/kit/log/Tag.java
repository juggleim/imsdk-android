package com.jet.im.kit.log;

import androidx.annotation.NonNull;

enum Tag {
    DEFAULT("SBUIKIT");

    @NonNull
    private final String tag;

    Tag(@NonNull String tag) {
        this.tag = tag;
    }

    @NonNull
    public String tag() {
        return tag;
    }
}
