package com.example.gallery.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gallery.base.bridge.Bye

/**
 * 针对所有相册子页面的基类
 */
abstract class BaseFragment : Fragment(), Bye {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun bye() {
        activity?.finish()
    }

}