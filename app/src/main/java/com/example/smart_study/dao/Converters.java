package com.example.smart_study.dao;
import androidx.room.TypeConverter;
import com.example.smart_study.services.generateExams.ExamQuestionModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
public class Converters {

    private static final Gson gson = new Gson();

    // Convertir Date en timestamp
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromQuestionList(List<ExamQuestionModel> questions) {
        if (questions == null) {
            return null;
        }
        return gson.toJson(questions);
    }

    @TypeConverter
    public static List<ExamQuestionModel> toQuestionList(String questionsString) {
        if (questionsString == null) {
            return null;
        }
        Type listType = new TypeToken<List<ExamQuestionModel>>() {}.getType();
        return gson.fromJson(questionsString, listType);
    }

    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        if (value == null) {
            return null;
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }
}