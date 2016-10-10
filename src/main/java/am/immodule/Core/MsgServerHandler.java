package am.immodule.Core;

import android.util.Log;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import am.immodule.Common.Logger;
import am.immodule.IMManagers.IMHeartBeatManager;
import am.immodule.IMManagers.IMSocketManager;

/**
 * Fixed: LinGuanHong
 *
 * 核心：和服务器交互的所有信息返回获取在这里再分发
 *
 * */

public class MsgServerHandler extends SimpleChannelHandler {

    private Logger logger = Logger.getLogger(MsgServerHandler.class);

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
    {
		super.channelConnected(ctx, e);
        logger.d("channel#channelConnected");

        /** 设计形式是先连接服务器，建立 socket 连接 再登录 */
        IMSocketManager.instance().onMsgServerConnected(); /** 登录操作在这里开始 */
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
        /**
         * 1. 已经与远程主机建立的连接，远程主机主动关闭连接，或者网络异常连接被断开的情况
         2. 已经与远程主机建立的连接，本地客户机主动关闭连接的情况
         3. 本地客户机在试图与远程主机建立连接时，遇到类似与connection refused这样的异常，未能连接成功时
         而只有当本地客户机已经成功的与远程主机建立连接（connected）时，连接断开的时候才会触发channelDisconnected事件，即对应上述的1和2两种情况。
         *
         **/
        logger.d("channel#channelDisconnected");
  		super.channelDisconnected(ctx, e);
        IMSocketManager.instance().onMsgServerDisconn();
        IMHeartBeatManager.instance().onMsgServerDisconn();
        // 断线了，先尝试重连。统一入口，否则会产生循环锁
        //IMReconnectManager.instance().tryReconnect();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		super.messageReceived(ctx, e);
        Log.d("zzzzz","channel#messageReceived");
        // 重置AlarmManager的时间
        ChannelBuffer channelBuffer = (ChannelBuffer) e.getMessage();
        if(null!=channelBuffer)
            IMSocketManager.instance().packetDispatch(channelBuffer);
	}

    /**
     * bug问题点:
     * exceptionCaught会调用断开链接， channelDisconnected 也会调用断开链接，事件通知冗余不合理。
     * a.另外exceptionCaught 之后channelDisconnected 依旧会被调用。 [切花网络方式]
     * b.关闭channel 也可能触发exceptionCaught
     * recvfrom failed: ETIMEDOUT (Connection timed out) 没有关闭长连接
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        super.exceptionCaught(ctx, e);
        if(e.getChannel() == null || !e.getChannel().isConnected()){
            IMSocketManager.instance().onConnectMsgServerFail();
        }
        logger.e("channel#[网络异常了]exceptionCaught:%s", e.getCause().toString());
    }
}
