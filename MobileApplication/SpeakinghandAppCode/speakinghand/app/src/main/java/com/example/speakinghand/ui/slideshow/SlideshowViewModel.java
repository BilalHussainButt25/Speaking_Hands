package com.example.speakinghand.ui.slideshow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SlideshowViewModel extends ViewModel {

    private final MutableLiveData<String> enteredText;

    public SlideshowViewModel() {
        enteredText = new MutableLiveData<>();
        enteredText.setValue("");
    }

    // Method to update the LiveData with entered text value
    public void updateEnteredText(String newText) {
        enteredText.setValue(newText);
    }

    // LiveData to observe the entered text
    public LiveData<String> getEnteredText() {
        return enteredText;
    }
}
