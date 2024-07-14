package com.example.speakinghand.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    // New MutableLiveData to represent the button click event
    private final MutableLiveData<Boolean> startButtonClickEvent = new MutableLiveData<>();

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    // Method to trigger the button click event
    public void onStartButtonClick() {
        startButtonClickEvent.setValue(true);
    }

    // LiveData to observe the button click event
    public LiveData<Boolean> getStartButtonClickEvent() {
        return startButtonClickEvent;
    }
}
