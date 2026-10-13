package win.movie8.tv;

import android.view.KeyEvent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public abstract class RemoteActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(AppLanguage.wrap(base));
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() >= KeyEvent.KEYCODE_DPAD_LEFT && event.getKeyCode() <= KeyEvent.KEYCODE_DPAD_DOWN) {
            View current = getWindow().getDecorView().findFocus();
            if (!(current instanceof android.widget.EditText) && current != null) {
                int direction = event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT ? View.FOCUS_LEFT : event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT ? View.FOCUS_RIGHT : event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP ? View.FOCUS_UP : View.FOCUS_DOWN;
                View next = current.focusSearch(direction);
                if (next != null && next != current) { next.requestFocus(); return true; }
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
