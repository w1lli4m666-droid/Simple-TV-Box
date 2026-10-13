package win.movie8.tv;

import android.app.Application;

public final class PureBoxApp extends Application {
    public void onCreate() {
        super.onCreate();
        AppText.initialize(this);
        WencaiClient.initialize(this);
    }
    @Override public void onConfigurationChanged(android.content.res.Configuration config) {
        super.onConfigurationChanged(config);
        AppText.initialize(this);
    }
}
