package com.example.speakinghand.ui.gallery;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.speakinghand.databinding.FragmentGalleryBinding;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView textView = binding.textView;

        if (textView != null) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);

            String htmlText = "Here's a step-by-step guide on how to use the app along with a brief explanation of each point:<br><br>" +
                    "<ol>" +
                    "<li>Launch the Speaking Hand app.</li>" +
                    "<li>Explore features: Predict Sign, Dictionary, Suggestions.</li>" +
                    "<li>On the home page, you have 'Predict Sign' and 'Dictionary'.</li>" +
                    "<li>Initiate Bluetooth connection by pairing HC-05 with mobile device.</li>" +
                    "<li>Click on the predict sign button.</li>" +
                    "<li>Press paired devices and look for HC-05 device.</li>" +
                    "<li>Click the button Perform sign when the light is red.</li>" +
                    "<li>Perform the sign when the light is green.</li>" +
                    "<li>Press the 'Predict' button to get your prediction.</li>" +
                    "<li>Press the 'Speak' button to hear the predicted sign.</li>" +
                    "<li>Clear displayed data with the 'Clear' button.</li>" +
                    "<li>In the dictionary, watch videos demonstrating sign language.</li>" +
                    "</ol>";

            textView.setText(Html.fromHtml(htmlText));
        }

        WebView webView = binding.webView;

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String youtubeVideoUrl = "https://www.youtube.com/embed/uw-A_OghSuQ?si=du6JV4qMv-25olCL";
        String html = "<iframe width=\"100%\" height=\"100%\" src=\"" + youtubeVideoUrl + "\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen></iframe>";

        webView.loadData(html, "text/html", "utf-8");

        return root;
    }
}
