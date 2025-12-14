package com.example.smart_study.outils;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileManager {


    public static String saveFileLocally(Context context, Uri fileUri, String fileName) {
        try {
            File directory = new File(context.getFilesDir(), "uploads");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File destinationFile = new File(directory, fileName);

            // Copier le fichier
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            FileOutputStream outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return destinationFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Uri getLocalFileUri(Context context, String fileName) {
        try {
            File directory = new File(context.getFilesDir(), "uploads");
            File file = new File(directory, fileName);

            if (file.exists()) {
                return Uri.fromFile(file);
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static boolean deleteLocalFile(Context context, String fileName) {
        try {
            File directory = new File(context.getFilesDir(), "uploads");
            File file = new File(directory, fileName);

            if (file.exists()) {
                return file.delete();
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static String getFileName(Uri uri) {
        String path = uri.getPath();
        if (path != null) {
            int index = path.lastIndexOf('/');
            if (index != -1) {
                return path.substring(index + 1);
            }
        }
        return "cour.pdf";
    }

}
/**
 * EXEMPLE D'UTILISATION COMPLÈTE DANS VOTRE ACTIVITY:
 *
 * // 1. Sélectionner un fichier avec l'interface XML
 * btnSelectFile.setOnClickListener(v -> {
 *     Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 *     intent.setType("application/pdf");
 *     startActivityForResult(intent, PICK_FILE_REQUEST);
 * });
 *
 * // 2. Récupérer le fichier sélectionné
 * @Override
 * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 *     if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK) {
 *         Uri fileUri = data.getData();
 *         String fileName = "mon_cours.pdf";
 *
 *         // Sauvegarder localement
 *         String filePath = FileManager.saveFileLocally(this, fileUri, fileName);
 *
 *         if (filePath != null) {
 *             Toast.makeText(this, "Fichier sauvegardé!", Toast.LENGTH_SHORT).show();
 *
 *             // 3. Extraire le texte du fichier sauvegardé
 *             Uri savedFileUri = FileManager.getLocalFileUri(this, fileName);
 *             String texte = PdfTextExtractor.extractText(this, savedFileUri);
 *
 *             if (texte != null) {
 *                 // Utiliser le texte extrait
 *                 Log.d("PDF", "Contenu: " + texte);
 *             }
 *         }
 *     }
 * }
 *
 * // Optionnel: Supprimer le fichier plus tard
 * FileManager.deleteLocalFile(this, "mon_cours.pdf");
 */