package hcmute.edu.vn.tick_tick.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import hcmute.edu.vn.tick_tick.R;

public abstract class TaskSwipeHelper extends ItemTouchHelper.SimpleCallback {

    private final Context context;
    private final Paint mClearPaint;
    private final ColorDrawable mBackground;
    private final int backgroundColorRight; // Green for Complete
    private final int backgroundColorLeft;  // Red for Delete
    private final Drawable iconComplete;
    private final Drawable iconDelete;
    private final int intrinsicWidth;
    private final int intrinsicHeight;

    public TaskSwipeHelper(Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;
        this.mBackground = new ColorDrawable();
        this.backgroundColorRight = Color.parseColor("#4CAF50"); // Green
        this.backgroundColorLeft = Color.parseColor("#F44336");  // Red
        this.mClearPaint = new Paint();
        this.mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        this.iconComplete = ContextCompat.getDrawable(context, R.drawable.ic_check);
        this.iconDelete = ContextCompat.getDrawable(context, R.drawable.ic_delete); // Assuming you have this, otherwise use ic_add rotated 45

        this.intrinsicWidth = iconComplete.getIntrinsicWidth();
        this.intrinsicHeight = iconComplete.getIntrinsicHeight();
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();
        boolean isCanceled = dX == 0f && !isCurrentlyActive;

        if (isCanceled) {
            clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        // Draw background
        if (dX > 0) { // Swiping Right (Complete)
            mBackground.setColor(backgroundColorRight);
            mBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
        } else { // Swiping Left (Delete)
            mBackground.setColor(backgroundColorLeft);
            mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        }
        mBackground.draw(c);

        // Calculate position of icon
        int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int iconMargin = (itemHeight - intrinsicHeight) / 2;
        int iconLeft, iconRight, iconBottom;

        if (dX > 0) { // Swiping Right
            iconLeft = itemView.getLeft() + iconMargin;
            iconRight = itemView.getLeft() + iconMargin + intrinsicWidth;
            iconBottom = iconTop + intrinsicHeight;
            iconComplete.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            iconComplete.setTint(Color.WHITE);
            iconComplete.draw(c);
        } else if (dX < 0) { // Swiping Left
            iconRight = itemView.getRight() - iconMargin;
            iconLeft = itemView.getRight() - iconMargin - intrinsicWidth;
            iconBottom = iconTop + intrinsicHeight;
            iconDelete.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            iconDelete.setTint(Color.WHITE);
            iconDelete.draw(c);
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, mClearPaint);
    }
}
