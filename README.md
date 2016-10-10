# TeamTalk_Android_IM_Module

### 把TT的安卓通讯部分抽离出来成一个 module 了，测试已通过，仅是通讯部分，没多余代码，方便移植到个人应用。

### 测试方法：

#### build.gradle

```java
compile project(':immodule')
```

#### Activity
```java
findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        IM.getIntance().userLogin("123","123");
    }
});

findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        List<RecentInfo> recentSessionList = IM.getIntance().imService.getSessionManager().getRecentListInfo();
        //IM.getIntance().imService.getSessionManager().findPeerEntity(recentSessionList.get(1).getSessionKey())
        //PeerEntity
        UserEntity loginUser = IM.getIntance().imService.getLoginManager().getLoginInfo();
        TextMessage textMessage = TextMessage.buildForSend("你好",loginUser, IM.getIntance().imService.getSessionManager().findPeerEntity(recentSessionList.get(1).getSessionKey()));
        /** 发送 */
        IM.getIntance().imService.getMessageManager().sendText(textMessage);
        /** 结束 */
    }
});
```
