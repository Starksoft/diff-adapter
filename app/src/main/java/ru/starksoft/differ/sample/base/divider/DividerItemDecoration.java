package ru.starksoft.differ.sample.base.divider;

import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;

import ru.starksoft.differ.sample.R;
import ru.starksoft.differ.sample.base.adapter.BaseAdapter;

import static ru.starksoft.differ.sample.base.divider.SeparatorType.SEPARATOR_DISABLED;
import static ru.starksoft.differ.sample.base.divider.SeparatorType.SEPARATOR_LEFT_PADDING_16;
import static ru.starksoft.differ.sample.base.divider.SeparatorType.SEPARATOR_LEFT_PADDING_48;
import static ru.starksoft.differ.sample.base.divider.SeparatorType.SEPARATOR_PADDING_16;
import static ru.starksoft.differ.sample.base.divider.SeparatorType.SEPARATOR_WITHOUT_PADDING;

public class DividerItemDecoration extends DividerPaintItemDecoration {

    private static final @ColorRes
    int COLOR_RES_ID = R.color.separatorDark;
    private static final int DIVIDER_HEIGHT = R.dimen.separator_height;
    private static int padding16;
    private static int padding48;

    private final BaseAdapter adapter;
    private final ExtraPaddingCache cache = new ExtraPaddingCache();

    public DividerItemDecoration(@NonNull Context context, @Nullable BaseAdapter adapter) {
        this(context, adapter, COLOR_RES_ID);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public DividerItemDecoration(@NonNull Context context, @Nullable BaseAdapter adapter, @ColorRes int separatorColor) {
        super(context, separatorColor, DIVIDER_HEIGHT);
        this.adapter = adapter;
        padding16 = (int) context.getResources().getDimension(R.dimen.padding_normal);
        padding48 = (int) context.getResources().getDimension(R.dimen.padding_extra_large);
    }

    @Override
    @NonNull
    protected ExtraPadding getExtraPadding(@IntRange(from = 0) int childPosition) {
        return cache.get(getDividerType(childPosition));
    }

    @SeparatorType
    private int getDividerType(@IntRange(from = 0) int childPosition) {
        return adapter != null && childPosition >= 0 ? adapter.getDividerType(childPosition) : SEPARATOR_DISABLED;
    }

    @Override
    public boolean disableDraw(@IntRange(from = 0) int childPosition) {
        return getDividerType(childPosition) == SEPARATOR_DISABLED;
    }

    @Override
    public boolean isTopDivider(int childPosition) {
        return getDividerType(childPosition) == SeparatorType.SEPARATOR_TOP;
    }

    private static class ExtraPaddingCache {

        private SparseArrayCompat<ExtraPadding> data = new SparseArrayCompat<>();

        @NonNull
        public ExtraPadding get(@SeparatorType int key) {
            ExtraPadding value = data.get(key);
            if (value == null) {
                value = createExtraPadding(key);
                data.put(key, value);
            }
            return value;
        }

        @NonNull
        private ExtraPadding createExtraPadding(@SeparatorType int type) {
            switch (type) {
                case SEPARATOR_LEFT_PADDING_16:
                    return new ExtraPadding(padding16, 0, 0, 0);

                case SEPARATOR_PADDING_16:
                    return new ExtraPadding(padding16, 0, padding16, 0);

                case SEPARATOR_LEFT_PADDING_48:
                    return new ExtraPadding(padding48, 0, 0, 0);

                case SEPARATOR_WITHOUT_PADDING:
                case SEPARATOR_DISABLED:
                default:
                    return new ExtraPadding(0, 0, 0, 0);
            }
        }
    }
}
