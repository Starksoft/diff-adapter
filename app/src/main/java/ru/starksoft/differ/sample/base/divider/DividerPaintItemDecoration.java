package ru.starksoft.differ.sample.base.divider;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public abstract class DividerPaintItemDecoration extends RecyclerView.ItemDecoration {

    private final Paint paint;

    private int color;

    public DividerPaintItemDecoration(@NonNull Context context, @ColorRes int colorResId, int dividerWidthDimenResId) {
        color = ContextCompat.getColor(context, colorResId);
        paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth((int) context.getResources().getDimension(dividerWidthDimenResId));
    }

    @NonNull
    protected abstract ExtraPadding getExtraPadding(@IntRange(from = 0) int childPosition);

    @Override
    public void onDrawOver(@NotNull Canvas c, @NotNull RecyclerView parent, @NotNull RecyclerView.State state) {
        super.onDraw(c, parent, state);

        paint.setColor(color);
        int alpha = paint.getAlpha();

        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            int childPosition = parent.getChildAdapterPosition(child);

            if (childPosition >= 0 && disableDraw(childPosition) || childPosition < 0) {
                continue;
            }

            paint.setAlpha((int) (child.getAlpha() * alpha));


            ExtraPadding extraPadding = getExtraPadding(childPosition);
            int startX = parent.getPaddingLeft() + extraPadding.left + (int) child.getTranslationX();
            int stopX = parent.getWidth() - parent.getPaddingRight() - extraPadding.right + (int) child.getTranslationX();

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int y;
            if (isTopDivider(childPosition)) {
                y = child.getTop() + params.topMargin + (int) child.getTranslationY();
            } else {
                y = child.getBottom() + params.bottomMargin + (int) child.getTranslationY();
            }
            c.drawLine(startX, y, stopX, y, paint);

        }
    }

    public boolean disableDraw(@IntRange(from = 0) int childPosition) {
        return false;
    }

    public boolean isTopDivider(@IntRange(from = 0) int childPosition) {
        return false;
    }

    public static class ExtraPadding {

        public final int left;
        public final int top;
        public final int right;
        public final int bottom;

        public ExtraPadding(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }
}
