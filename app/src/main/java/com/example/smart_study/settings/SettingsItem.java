package com.example.smart_study.settings;

public class SettingsItem {
    private final String title;
    private final int iconResId;
    private final int backgroundResId;

    public SettingsItem(String title, int iconResId, int backgroundResId) {
        this.title = title;
        this.iconResId = iconResId;
        this.backgroundResId = backgroundResId;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getBackgroundResId() {
        return backgroundResId;
    }
}
