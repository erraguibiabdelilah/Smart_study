package com.example.smart_study.fragments.resumes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;

public class ResumeDisplayFragment extends Fragment {

    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resume_display, container, false);

        webView = view.findViewById(R.id.webView);

        if (getArguments() != null) {
            String resume = getArguments().getString("resume");
            if (resume != null) {
                displayStyledResume(resume);
            }
        }

        return view;
    }

    private void displayStyledResume(String resume) {
        String cleanedResume = resume.replaceAll("<s>", "").replaceAll("</s>", "").trim();

        String htmlResume = cleanedResume
                .replace("\n\n", "</p><p>")
                .replace("\n", "<br>");

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset='UTF-8'>\n" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "<style>\n" +
                "body {\n" +
                "    font-family: 'Georgia', 'Times New Roman', serif;\n" +
                "    line-height: 1.8;\n" +
                "    color: #2c3e50;\n" +
                "    max-width: 800px;\n" +
                "    margin: 0 auto;\n" +
                "    padding: 30px;\n" +
                "    background: #ffffff;\n" +
                "}\n" +
                "h1 {\n" +
                "    color: #1a237e;\n" +
                "    border-bottom: 3px solid #3f51b5;\n" +
                "    padding-bottom: 10px;\n" +
                "    margin-bottom: 25px;\n" +
                "    font-size: 28px;\n" +
                "}\n" +
                "h2 {\n" +
                "    color: #283593;\n" +
                "    margin-top: 25px;\n" +
                "    margin-bottom: 12px;\n" +
                "    font-size: 20px;\n" +
                "    border-left: 4px solid #3f51b5;\n" +
                "    padding-left: 10px;\n" +
                "}\n" +
                "h3 {\n" +
                "    color: #3949ab;\n" +
                "    margin-top: 18px;\n" +
                "    margin-bottom: 10px;\n" +
                "    font-size: 16px;\n" +
                "}\n" +
                "p {\n" +
                "    margin-bottom: 12px;\n" +
                "    text-align: justify;\n" +
                "}\n" +
                "ul, ol {\n" +
                "    margin-left: 25px;\n" +
                "    margin-bottom: 15px;\n" +
                "}\n" +
                "li {\n" +
                "    margin-bottom: 8px;\n" +
                "    line-height: 1.6;\n" +
                "}\n" +
                "strong {\n" +
                "    color: #1a237e;\n" +
                "    font-weight: bold;\n" +
                "}\n" +
                "em {\n" +
                "    color: #5c6bc0;\n" +
                "    font-style: italic;\n" +
                "}\n" +
                ".section {\n" +
                "    margin-bottom: 25px;\n" +
                "    padding: 15px;\n" +
                "    background: #f8f9fa;\n" +
                "    border-radius: 5px;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>Résumé du Document</h1>\n" +
                "<div class='section'>" +
                "<p>" + htmlResume + "</p>" +
                "</div>" +
                "</body>\n" +
                "</html>";

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }
}