package am.immodule;

import android.app.Application;

import am.immodule.Common.ImageLoaderUtil;

/**
 * Created by LinGuanHong on 2016/10/10.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        IM.getIntance().start(getApplicationContext());
        ImageLoaderUtil.initImageLoaderConfig(getApplicationContext());
    }
}
