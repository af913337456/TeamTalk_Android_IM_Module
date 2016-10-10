package am.immodule;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import am.immodule.Common.Logger;
import am.immodule.Server.IMService;


/**
 * Created by LinGuanHong on 2016/10/9.
 */

public class IM {

    protected static Logger logger = Logger.getLogger(IM.class);

    public static IM getIntance(){
        return IMInstance.im;
    }

    private static class IMInstance{

        private static IM im = new IM();

    }

    public IMService imService;

    /** start 放在 application 里面 */
    public void start(Context context){
        Log.d("zzzzz","start IMService");
        Intent intent = new Intent();
        intent.setClass(context, IMService.class);
        /** onCreate 只会执行一次 */
        context.startService(intent);
        context.bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /** socket 的初始化在登录成功后会掉里面 */
    public void userLogin(String account,String psasword){
        if (imService==null){
            logger.d("imservice is null");
            return;
        }
        imService.getLoginManager().login(account, psasword);
    }

//    public static void

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (imService == null) {
                IMService.IMServiceBinder binder = (IMService.IMServiceBinder) service;
                imService = binder.getService();

                if (imService == null) {
                    logger.e("im#get imService failed");
                    return;
                }
                logger.d("im#get imService ok");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

}
