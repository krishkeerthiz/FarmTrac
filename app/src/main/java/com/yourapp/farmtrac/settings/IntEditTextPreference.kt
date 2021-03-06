package com.yourapp.farmtrac.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference

class IntEditTextPreference : EditTextPreference {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun getPersistedString(defaultReturnValue: String?): String {
        return java.lang.String.valueOf(getPersistedInt(-1))
    }

    override fun persistString(value: String?): Boolean {
        return persistInt(Integer.valueOf(value))
    }
}