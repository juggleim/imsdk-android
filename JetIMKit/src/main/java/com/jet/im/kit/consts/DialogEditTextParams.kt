package com.jet.im.kit.consts

import android.text.TextUtils.TruncateAt

open class DialogEditTextParams private constructor(
    var hintText: String? = null,
    var enableSingleLine: Boolean = false,
    var ellipsis: TruncateAt? = null,
    var selection: Int = 0,
    var text: String? = null,
) {
    constructor(hintText: String) : this(hintText = hintText, false, null, 0, null)
}
