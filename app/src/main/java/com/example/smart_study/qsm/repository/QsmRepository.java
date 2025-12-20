package com.example.smart_study.qsm.repository;

import com.example.smart_study.qsm.model.QsmQuestion;
import java.util.Arrays;
import java.util.List;

public class QsmRepository {

    public List<QsmQuestion> getQuestions() {
        return Arrays.asList(
                new QsmQuestion(
                        "1",
                        "What is Java?",
                        Arrays.asList(
                                "Database",
                                "Programming language",
                                "Operating system",
                                "Browser"
                        ),
                        1
                ),
                new QsmQuestion(
                        "2",
                        "Android is based on?",
                        Arrays.asList(
                                "Windows",
                                "Linux",
                                "iOS",
                                "Unix"
                        ),
                        1
                )
        );
    }
}
