package com.example.smart_study.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_study.R;
import com.example.smart_study.beans.CourseHistory;
import com.example.smart_study.db.AppDatabase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourseHistoryAdapter extends RecyclerView.Adapter<CourseHistoryAdapter.CourseHistoryViewHolder> {

    private Context context;
    private List<CourseHistory> courseList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

    public CourseHistoryAdapter(Context context) {
        this.context = context;
    }

    public void setCourses(List<CourseHistory> courses) {
        this.courseList = courses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_history, parent, false);
        return new CourseHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseHistoryViewHolder holder, int position) {
        CourseHistory course = courseList.get(position);

        holder.courseTitle.setText(course.getCourseTitle());
        holder.savedDate.setText(dateFormat.format(course.getSavedDate()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(course.getPdfUri()), "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        });

        holder.deleteIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Supprimer le cours")
                    .setMessage("Êtes-vous sûr de vouloir supprimer ce cours?")
                    .setPositiveButton("Supprimer", (dialog, which) -> {
                        deleteCourse(course, position);
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    private void deleteCourse(CourseHistory course, int position) {
        new Thread(() -> {
            try {
                // Delete from database
                AppDatabase db = AppDatabase.getInstance(context);
                db.courseHistoryDao().delete(course);

                // Delete the file
                Uri pdfUri = Uri.parse(course.getPdfUri());
                File file = new File(pdfUri.getPath());
                if (file.exists()) {
                    if (!file.delete()) {
                        // Handle failed deletion if needed
                    }
                }

                // Update UI
                ((android.app.Activity) context).runOnUiThread(() -> {
                    courseList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, courseList.size());
                    Toast.makeText(context, "Cours supprimé.", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "Échec de la suppression du cours.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class CourseHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView courseTitle;
        TextView savedDate;
        ImageView deleteIcon;

        public CourseHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            courseTitle = itemView.findViewById(R.id.courseTitle);
            savedDate = itemView.findViewById(R.id.savedDate);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }
    }
}
