package com.ralf.bottomsheet

import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior

data class BottomSheetOption(
    val bottomSheetTag: String,
    val fm: FragmentManager,
    val fragment: Fragment,
    val childTag: String? = null,
    val args: Bundle? = null,
    @AnimRes
    val enter: Int = 0,
    @AnimRes
    val exit: Int = 0,
    val isShowDialog: Boolean = true,
    @IdRes
    val layoutRes: Int = 0,
    val needActivity: Boolean = true,
    var stateChangedCallback: BottomSheetBehavior.BottomSheetCallback? = null
)