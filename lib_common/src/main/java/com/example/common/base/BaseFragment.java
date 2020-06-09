package com.example.common.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.common.base.bridge.BaseImpl;
import com.example.common.base.bridge.BaseViewModel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by WangYanBin on 2020/6/4.
 */
public abstract class BaseFragment<VM extends BaseViewModel, VDB extends ViewDataBinding> extends Fragment implements BaseImpl {
    protected VM viewModel;
    protected VDB binding;

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    protected abstract int getLayoutResID();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getLayoutResID() != 0) {
            binding = DataBindingUtil.inflate(inflater, getLayoutResID(), container, false);
            binding.setLifecycleOwner(this);
            Class modelClass;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                modelClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                //如果没有指定泛型参数，则默认使用BaseViewModel
                modelClass = BaseViewModel.class;
            }
            viewModel = (VM) new ViewModelProvider(this).get(modelClass);
            viewModel.attachView(getActivity(), getContext(), binding);
            return binding.getRoot();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initEvent();
        initData();
    }

    @Override
    public void initView() {
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (binding != null) {
            binding.unbind();
        }
    }
    // </editor-fold>

}
