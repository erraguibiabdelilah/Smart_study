package com.example.smart_study.outils;

import android.content.Context;
import android.net.Uri;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.InputStream;

public class MyPdfExtractor {

    public static String extractText(Context context, Uri pdfUri) {
        StringBuilder text = new StringBuilder();
        PdfReader reader = null;
        InputStream inputStream = null;

        try {
            inputStream = context.getContentResolver().openInputStream(pdfUri);
            if (inputStream == null) return null;

            reader = new PdfReader(inputStream);
            int pages = reader.getNumberOfPages();

            for (int i = 1; i <= pages; i++) {
                text.append(
                        com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage(reader, i)
                );
                text.append("\n");
            }

            return text.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;

        } finally {
            try {
                if (reader != null) reader.close();
                if (inputStream != null) inputStream.close();
            } catch (Exception ignored) {}
        }
    }
}
