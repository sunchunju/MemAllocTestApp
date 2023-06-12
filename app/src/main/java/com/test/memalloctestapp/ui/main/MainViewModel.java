package com.test.memalloctestapp.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    public MutableLiveData<String> currentMemSize = new MutableLiveData<>();
    public MutableLiveData<String> allocMemSize = new MutableLiveData<>();

    public MutableLiveData<Boolean> startBtnEnabled = new MutableLiveData<>();
}