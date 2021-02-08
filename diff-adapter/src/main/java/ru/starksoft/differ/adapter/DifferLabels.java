package ru.starksoft.differ.adapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pools;

/**
 * Список меток
 */
public class DifferLabels {

    private static final int MAX_SIZE_POOL = 20;
    private static final Pools.Pool<DifferLabels> labelsPool = new Pools.SynchronizedPool<>(MAX_SIZE_POOL);

    @NonNull
    protected final List<Integer> items = new ArrayList<>();

    @NonNull
    public static DifferLabels obtain() {
        DifferLabels labels = labelsPool.acquire();
        if (labels == null) {
            labels = new DifferLabels();
        }
        return labels;
    }

    public void release() {
        clear();
        labelsPool.release(this);
    }

    public void add(@Nullable int... labels) {
        if (labels != null && labels.length > 0) {
            for (int label : labels) {
                items.add(label);
            }
        }
    }

    public void add(@NonNull List<Integer> labels) {
        items.addAll(labels);
    }

    @NonNull
    public List<Integer> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
    }

    public boolean has(int label) {
        return items.contains(label);
    }

    @NonNull
    public String log() {
        if (!items.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("size=").append(items.size());
            for (int i = 0, size = items.size(); i < size; i++) {
                sb.append(", ").append("[").append(i).append("]=").append(items.get(i));
            }
            sb.append("}");
            return sb.toString();
        }
        return "";
    }
}
