package com.example.speakinghand.ui.slideshow;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.speakinghand.R;
import com.example.speakinghand.databinding.FragmentSlideshowBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private EditText nameEditText, emailEditText, feedbackEditText;
    private Button submitButton;
    private DatabaseReference feedbacksRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        nameEditText = root.findViewById(R.id.editTextName);
        emailEditText = root.findViewById(R.id.editTextEmail);
        feedbackEditText = root.findViewById(R.id.editTextFeedback);
        submitButton = root.findViewById(R.id.button);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        feedbacksRef = database.getReference("feedbacks");

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isValidEmail(s)) {
                    emailEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    nameEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        feedbackEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    feedbackEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String suggestion = feedbackEditText.getText().toString().trim();

                boolean isValid = true;

                if (TextUtils.isEmpty(name)) {
                    nameEditText.setError("Name is required");
                    isValid = false;
                }

                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Email is required");
                    isValid = false;
                } else if (!isValidEmail(email)) {
                    emailEditText.setError("Invalid email format");
                    isValid = false;
                }

                if (TextUtils.isEmpty(suggestion)) {
                    feedbackEditText.setError("Feedback is required");
                    isValid = false;
                }

                if (isValid) {
                    DatabaseReference newPostRef = feedbacksRef.push();
                    newPostRef.child("name").setValue(name);
                    newPostRef.child("email").setValue(email);
                    newPostRef.child("suggestion").setValue(suggestion);

                    nameEditText.setText("");
                    emailEditText.setText("");
                    feedbackEditText.setText("");

                    Toast.makeText(getContext(), "Feedback submitted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return root;
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
