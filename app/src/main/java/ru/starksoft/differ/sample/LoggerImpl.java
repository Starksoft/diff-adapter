package ru.starksoft.differ.sample;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import ru.starksoft.differ.Logger;

public final class LoggerImpl implements Logger {

	private static final Logger INSTANCE = new LoggerImpl();

	@NonNull
	public static Logger getInstance() {
		return INSTANCE;
	}

	@Override
	public void log(@NotNull String tag, @NotNull String message) {
		Log.d(tag, message);
	}

	@Override
	public void log(@NotNull String tag, @NotNull String message, @NotNull Throwable t) {
		Log.d(tag, message, t);
	}

	@Override
	public void w(@NotNull String tag, @NotNull String message) {
		Log.w(tag, message);
	}

	@Override
	public void d(@NotNull String tag, @NotNull String message) {
		Log.d(tag, message);
	}

	@Override
	public void e(@NotNull String tag, @NotNull String message, @NotNull Throwable t) {
		Log.e(tag, message, t);
	}
}
