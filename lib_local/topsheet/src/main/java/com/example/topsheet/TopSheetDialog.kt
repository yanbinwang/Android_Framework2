package com.example.topsheet

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.topsheet.TopSheetBehavior.Companion.from

class TopSheetDialog @JvmOverloads constructor(context: Context, @StyleRes theme: Int = 0) : AppCompatDialog(context, getThemeResId(context, theme)) {
    private var topSheetBehavior: TopSheetBehavior<FrameLayout>? = null
    private val mTopSheetCallback by lazy { object : TopSheetBehavior.TopSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == TopSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    } }

    init {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    companion object {
        private fun getThemeResId(context: Context, themeId: Int): Int {
            return if (themeId == 0) {
                // If the provided theme is 0, then retrieve the dialogTheme from our theme
                val outValue = TypedValue()
                if (context.theme.resolveAttribute(R.attr.bottomSheetDialogTheme, outValue, true)) {
                    outValue.resourceId
                } else {
                    // bottomSheetDialogTheme is not provided; we default to our light theme
                    R.style.Theme_Design_TopSheetDialog
                }
            } else {
                themeId
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(wrapInTopSheet(layoutResID, null, null))
    }

    override fun setContentView(view: View) {
        super.setContentView(wrapInTopSheet(0, view, null))
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        super.setContentView(wrapInTopSheet(0, view, params))
    }

    private fun wrapInTopSheet(layoutResId: Int, view: View?, params: ViewGroup.LayoutParams?): View {
        val coordinator = View.inflate(context, R.layout.top_sheet_dialog, null) as CoordinatorLayout
        val topSheet = coordinator.findViewById<FrameLayout>(R.id.design_top_sheet)
        topSheetBehavior = from(topSheet)
        topSheetBehavior?.setTopSheetCallback(mTopSheetCallback)
        val contentView = if (layoutResId != 0 && view == null) {
            layoutInflater.inflate(layoutResId, coordinator, false)
        } else {
            view
        }
        if (params == null) {
            topSheet.addView(contentView)
        } else {
            topSheet.addView(contentView, params)
        }
        coordinator.findViewById<View>(R.id.top_sheet_touch_outside).setOnClickListener {
            if (isShowing) {
                cancel()
            }
        }
        return coordinator
    }

}