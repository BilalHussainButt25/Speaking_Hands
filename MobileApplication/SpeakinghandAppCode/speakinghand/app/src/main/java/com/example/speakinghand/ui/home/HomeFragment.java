package com.example.speakinghand.ui.home;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.speakinghand.MainActivity3;
import com.example.speakinghand.MainActivity4;
import com.example.speakinghand.R;
import com.example.speakinghand.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }

        final Button bottomButton = binding.bottomButton;

        homeViewModel.getText().observe(getViewLifecycleOwner(), text -> {
        });

        bottomButton.setOnClickListener(view -> onBottomButtonClick());

        final Button dictionaryButton = binding.dictionary;
        dictionaryButton.setOnClickListener(view -> onDictionaryButtonClick());

        return root;
    }

    private void onBottomButtonClick() {
        Intent intent = new Intent(getActivity(), MainActivity3.class);
        startActivity(intent);
    }

    private void onDictionaryButtonClick() {
        Intent intent = new Intent(getActivity(), MainActivity4.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
