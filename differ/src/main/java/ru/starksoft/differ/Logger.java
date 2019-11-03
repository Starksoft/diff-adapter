package ru.starksoft.differ;

import androidx.annotation.NonNull;

public interface Logger {

    void log(@NonNull Throwable t);

    void log(@NonNull String tag, @NonNull String message);

    void log(@NonNull String tag, @NonNull String message, @NonNull Throwable t);

    void w(@NonNull String tag, @NonNull String message);

    void d(@NonNull String tag, @NonNull String message);
}
