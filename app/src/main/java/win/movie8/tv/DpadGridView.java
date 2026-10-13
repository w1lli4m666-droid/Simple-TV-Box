package win.movie8.tv;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public final class DpadGridView extends RecyclerView {
    public interface NearEndListener { void onNearEnd(); }
    public interface TopRowListener { void onTopRowUp(); }
    private NearEndListener nearEnd; private TopRowListener topRow;
    public DpadGridView(Context context) { super(context); setFocusable(true); setFocusableInTouchMode(true); setDescendantFocusability(FOCUS_AFTER_DESCENDANTS); setClipChildren(false); setClipToPadding(false); }
    public void setOnNearEndListener(NearEndListener listener){nearEnd=listener;}
    public void setOnTopRowUpListener(TopRowListener listener){topRow=listener;}
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN)) {
            View current = getFocusedChild();
            if (current != null && getLayoutManager() instanceof GridLayoutManager) {
                int position = getChildAdapterPosition(current); int columns = ((GridLayoutManager) getLayoutManager()).getSpanCount(); int next = position;
                if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP && position>=0 && position<columns && topRow!=null){topRow.onTopRowUp();return true;}
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) next = position - 1;
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) next = position + 1;
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) next = position - columns;
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) next = position + columns;
                if (next >= 0 && getAdapter() != null && next < getAdapter().getItemCount()) {
                    scrollToPosition(next); final int target = next; post(() -> { View holder = findViewHolderForAdapterPosition(target) == null ? null : findViewHolderForAdapterPosition(target).itemView; if (holder != null) holder.requestFocus(); if(nearEnd!=null && target>=getAdapter().getItemCount()-columns*2) nearEnd.onNearEnd(); }); return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
