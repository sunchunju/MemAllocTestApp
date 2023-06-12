package com.test.memalloctestapp.ui.main;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.test.memalloctestapp.databinding.FragmentMainBinding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MainFragment";

    private FragmentMainBinding binding;
    private MainViewModel mViewModel;
    TextView mTotalMemTv;
    TextView mAvaliMemTv;
    EditText mMaxMemEd;
    EditText mAllocMemPerEd;
    EditText mIntervalEd;
    TextView mTotalAllocMemTv;
    Button mStartBtn;
    String avaliMemStr;
    String totalMemStr;

    private int maxMemory; // 用户设置的内存上限
    private int allocMemoryPer; // 用户设置的单次内存分配大小
    private int allocMemTotal; // 当前已分配的内存大小
    private int allocMemForeground; // 当前前台已分配的内存大小
    private int allocMemBackground; // 当前前台已分配的内存大小
    private boolean isAppInForeground = true; // 是否在前台运行
    private long memAllocInterval = 200; // 每隔200ms尝试分配1MB内存
    private boolean isMemoryAllocationStop = false; // 是否正在进行内存分配


    private List<Long> allocTimesForeground = new ArrayList<>();
    private List<Integer> allocMemorySizesForeground = new ArrayList<>();

    private List<Long> allocTimesBackground = new ArrayList<>();
    private List<Integer> allocMemorySizesBackground = new ArrayList<>();

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable memoryAllocatorTask = new Runnable() {
        @Override
        public void run() {
            if (!isMemoryAllocationStop){
                allocateMemory();
                scheduleMemoryAllocatorTask();
            }
        }
    };

    Utils utilsInstance;
    private ByteBuffer mBuffer;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        setup();
        initData();
        initObserver();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        isAppInForeground = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isAppInForeground = false;
    }

    private void setup() {
        mTotalMemTv = binding.totalMemTv;
        mAvaliMemTv = binding.avaMemTv;
        mMaxMemEd = binding.maxMemEd;
        mAllocMemPerEd = binding.allocMemPerEd;
        mIntervalEd = binding.intervalED;
        mTotalAllocMemTv = binding.totalAllocMem;
        mStartBtn = binding.startBtn;

        mStartBtn.setOnClickListener(this);
    }

    private void initData() {
        utilsInstance = Utils.getInstance();
        long avaliMem = utilsInstance.getFreeMem(getContext());
        long totalMem = utilsInstance.getTotalMem(getContext());

        avaliMemStr = Formatter.formatFileSize(getContext(), avaliMem);
        totalMemStr = Formatter.formatFileSize(getContext(), totalMem);
        mTotalMemTv.setText("总内存："+totalMemStr);
        mAvaliMemTv.setText("当前可用内存："+avaliMemStr);

        allocMemTotal = 0;
        allocMemForeground = 0;
        allocMemBackground = 0;
    }

    private void initObserver() {
        // Create the observer which updates the UI.
        final Observer<String> avaliMemObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newName) {
                // Update the UI, in this case, a TextView.
                mAvaliMemTv.setText("当前可用内存："+newName);
            }
        };

        // Create the observer which updates the UI.
        final Observer<String> totalAllocMemObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newName) {
                // Update the UI, in this case, a TextView.
                mTotalAllocMemTv.setText(newName);
            }
        };

        // Create the observer which updates the UI.
        final Observer<Boolean> startBtnObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean enabled) {
                // Update the UI, in this case, a TextView.
                mStartBtn.setEnabled(enabled);
            }
        };
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mViewModel.currentMemSize.observe(getViewLifecycleOwner(),avaliMemObserver);
        mViewModel.allocMemSize.observe(getViewLifecycleOwner(),totalAllocMemObserver);
        mViewModel.startBtnEnabled.observe(getViewLifecycleOwner(),startBtnObserver);
    }

    @Override
    public void onClick(View v) {
        getValue();
        isMemoryAllocationStop = false;
        mViewModel.startBtnEnabled.setValue(false);
        scheduleMemoryAllocatorTask();
    }

    private void getValue() {
        //获取用户设置的分配内存上限，默认是1024MB
        String maxMemTemp = mMaxMemEd.getText().toString();
        maxMemory = Integer.parseInt(TextUtils.isEmpty(maxMemTemp)?"1024":maxMemTemp);

        //获取用户设置的单次分配内存大小,默认是1MB
        String allocMemPerTemp = mAllocMemPerEd.getText().toString();
        allocMemoryPer = Integer.parseInt(TextUtils.isEmpty(allocMemPerTemp)?"1":allocMemPerTemp);

        //获取用户设置的分配内存时间间隔,默认是200ms
        String memAllocInterTemp = mIntervalEd.getText().toString();
        memAllocInterval = Integer.parseInt(TextUtils.isEmpty(memAllocInterTemp)?"200":memAllocInterTemp);
    }

    private void scheduleMemoryAllocatorTask() {
        handler.postDelayed(memoryAllocatorTask, memAllocInterval);
    }

    private void allocateMemory() {
        Log.d(TAG, "allocateMemory() allocMemTotal = " + allocMemTotal + ", maxMemory = "+maxMemory);
        if (allocMemTotal >= maxMemory ){
            stopMemoryAllocation();
            return;
        }
        allocMemTotal += allocMemoryPer;
        long startTime = System.currentTimeMillis();
        mBuffer = NativeMemoryUtil.allocateNativeMemory(allocMemoryPer * 1024 * 1024);
        Log.d(TAG, "mBuffer = " + mBuffer.toString());

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        Log.d(TAG, "elapsedTime = " + elapsedTime);
        Log.d(TAG, "allocMemTotal = " + allocMemTotal);
        //write times and memory info to arraylist
        if (isAppInForeground) {
            allocMemForeground += allocMemoryPer;
            allocTimesForeground.add(elapsedTime);
            allocMemorySizesForeground.add(allocMemForeground);
            Log.d(TAG, "allocMemForeground = " + allocMemForeground);
            Log.d(TAG, "allocTimesForeground = " + allocTimesForeground);
            Log.d(TAG, "allocMemorySizesForeground = " + allocMemorySizesForeground);
        } else {
            allocMemBackground += allocMemoryPer;
            allocTimesBackground.add(elapsedTime);
            allocMemorySizesBackground.add(allocMemBackground);
            Log.d(TAG, "allocMemBackground = " + allocMemBackground);
            Log.d(TAG, "allocTimesBackground = " + allocTimesBackground);
            Log.d(TAG, "allocMemorySizesBackground = " + allocMemorySizesBackground);
        }
        //update ui
        updateUiWithMemoryInfo(allocMemTotal);
    }

    private void updateUiWithMemoryInfo(int allocMemTotal) {
        //获取当前可用内存
        long avaliMem = utilsInstance.getFreeMem(getContext());
        avaliMemStr = Formatter.formatFileSize(getContext(), avaliMem);
        mViewModel.currentMemSize.setValue(avaliMemStr);

        //已分配内存数
        mViewModel.allocMemSize.setValue(Integer.toString(allocMemTotal));
    }

    private void stopMemoryAllocation() {
        Log.i(TAG,"stopMemoryAllocation");
        isMemoryAllocationStop = true;
        mViewModel.startBtnEnabled.setValue(true);
        handler.removeCallbacks(memoryAllocatorTask);
        writeToFile(allocTimesForeground, allocMemorySizesForeground, "foreground.csv");
        writeToFile(allocTimesBackground, allocMemorySizesBackground, "background.csv");
    }

    private void writeToFile(List<Long> allocTimes, List<Integer> allocMemorySizes, String fileName) {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), fileName);
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            for (int i = 0; i < allocTimes.size(); i++) {
                bw.write(allocMemorySizes.get(i) + "," + allocTimes.get(i));
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}