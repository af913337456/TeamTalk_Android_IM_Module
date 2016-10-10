package am.immodule.Callback;

import android.os.Handler;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import am.immodule.Common.Logger;

/**
 * @author : yingmu on 15-1-7.
 * @email : yingmu@mogujie.com.
 */

/**
 *
 * 用来装载每次发起请求的时候，传入的监听接口对象
 *
 * */

public class ListenerQueue {

    private static ListenerQueue listenerQueue = new ListenerQueue();
    private Logger logger = Logger.getLogger(ListenerQueue.class);
    public static ListenerQueue instance(){
        return listenerQueue;
    }

    private volatile  boolean stopFlag = false;
    private volatile  boolean hasTask = false;


    //callback 队列
    private Map<Integer,Packetlistener> callBackQueue = new ConcurrentHashMap<>();
    private Handler timerHandler = new Handler();


    public void onStart(){
        logger.d("ListenerQueue#onStart run");
        stopFlag = false;
        startTimer();
    }
    public void onDestory(){
        logger.d("ListenerQueue#onDestory ");
        callBackQueue.clear();
        stopTimer();
    }

    //以前是TimerTask处理方式
    private void startTimer() {
        if(!stopFlag && hasTask == false) {
            hasTask = true;
            timerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    timerImpl();
                    hasTask = false;
                    startTimer();
                }
            }, 5 * 1000);
        }
    }

    private void stopTimer(){
        stopFlag = true;
    }

    private void timerImpl() {
        long currentRealtime =   System.currentTimeMillis();//SystemClock.elapsedRealtime();

        for (Map.Entry<Integer, Packetlistener> entry : callBackQueue.entrySet()) {

            Packetlistener packetlistener = entry.getValue();
            Integer seqNo = entry.getKey();
            long timeRange = currentRealtime - packetlistener.getCreateTime();

            try {
                if (timeRange >= packetlistener.getTimeOut()) {
                    logger.d("ListenerQueue#find timeout msg");
                    Packetlistener listener = pop(seqNo);
                    if (listener != null) {
                        listener.onTimeout();
                    }
                }
            } catch (Exception e) {
                logger.d("ListenerQueue#timerImpl onTimeout is Error,exception is %s", e.getCause());
            }
        }
    }

    public void push(int seqNo,Packetlistener packetlistener){
        Log.d("zzzzz","push(int seqNo,Packetlistener packetlistener)");
        if(seqNo <=0 || null==packetlistener){
            logger.d("ListenerQueue#push error, cause by Illegal params");
            return;
        }
        Log.d("zzzzz","packetlistener not null");
        callBackQueue.put(seqNo,packetlistener);
    }


    public Packetlistener pop(int seqNo){
        synchronized (ListenerQueue.this) {
            if (callBackQueue.containsKey(seqNo)) {
                Packetlistener packetlistener = callBackQueue.remove(seqNo);
                return packetlistener;
            }
            return null;
        }
    }
}
