package am.immodule.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import am.immodule.Common.DBConstant;
import am.immodule.Common.Logger;
import am.immodule.Common.MessageConstant;
import am.immodule.DB.dao.DaoMaster;
import am.immodule.DB.dao.DaoSession;
import am.immodule.DB.dao.DepartmentDao;
import am.immodule.DB.dao.GroupDao;
import am.immodule.DB.dao.MessageDao;
import am.immodule.DB.dao.SessionDao;
import am.immodule.DB.dao.UserDao;
import am.immodule.DB.entity.DepartmentEntity;
import am.immodule.DB.entity.GroupEntity;
import am.immodule.DB.entity.MessageEntity;
import am.immodule.DB.entity.SessionEntity;
import am.immodule.DB.entity.UserEntity;
import am.immodule.Model.Beans.AudioMessage;
import am.immodule.Model.Beans.ImageMessage;
import am.immodule.Model.Beans.MixMessage;
import am.immodule.Model.Beans.TextMessage;
import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * @author : yingmu on 15-1-5.
 * @email : yingmu@mogujie.com.
 *
 *  有两个静态标识可开启QueryBuilder的SQL和参数的日志输出：
 *   QueryBuilder.LOG_SQL = true;
 *   QueryBuilder.LOG_VALUES = true;
 */
public class DBInterface {

    private Logger logger = Logger.getLogger(DBInterface.class);
    private static DBInterface dbInterface = null;
    private DaoMaster.DevOpenHelper openHelper;
    private Context context = null;
    private int  loginUserId =0;

    public static DBInterface instance(){
        if (dbInterface == null) {
            synchronized (DBInterface.class) {
                if (dbInterface == null) {
                    dbInterface = new DBInterface();
                }
            }
        }
        return dbInterface;
    }

    private DBInterface(){
    }

    /**
     * 上下文环境的更新
     * 1. 环境变量的clear
     * check
     */
    public void close() {
        if(openHelper !=null) {
            openHelper.close();
            openHelper = null;
            context = null;
            loginUserId = 0;
        }
    }


    public void initDbHelp(Context ctx,int loginId){
        if(ctx == null || loginId <=0 ){
            throw  new RuntimeException("#DBInterface# init DB exception!");
        }
        // 临时处理，为了解决离线登陆db实例初始化的过程
        if(context != ctx || loginUserId !=loginId ){
            context = ctx;
            loginUserId = loginId;
            close();
            logger.i("DB init,loginId:%d",loginId);
            String DBName = "tt_"+loginId+".db";
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(ctx, DBName, null);
            this.openHelper = helper;
        }
    }

    /**
     * Query for readable DB
     */
    private DaoSession openReadableDb() {
        isInitOk();
        SQLiteDatabase db = openHelper.getReadableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        return daoSession;
    }

    /**
     * Query for writable DB
     */
    private DaoSession openWritableDb(){
        isInitOk();
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        return daoSession;
    }


    private void isInitOk(){
        if(openHelper ==null){
            logger.e("DBInterface#isInit not success or start,cause by openHelper is null");
            // 抛出异常 todo
            throw  new RuntimeException("DBInterface#isInit not success or start,cause by openHelper is null");
        }
    }


    /**-------------------------下面开始department 操作相关---------------------------------------*/
    public void  batchInsertOrUpdateDepart(List<DepartmentEntity> entityList){
        if(entityList.size() <=0){
            return ;
        }
        DepartmentDao dao =  openWritableDb().getDepartmentDao();
        dao.insertOrReplaceInTx(entityList);
    }

    /**update*/
    public int getDeptLastTime(){
        DepartmentDao dao =  openReadableDb().getDepartmentDao();
        DepartmentEntity entity = dao.queryBuilder()
                .orderDesc(DepartmentDao.Properties.Updated)
                .limit(1)
                .unique();
        if(entity == null){
            return 0;
        }else{
            return entity.getUpdated();
        }
    }

    // 部门被删除的情况
    public List<DepartmentEntity> loadAllDept(){
        DepartmentDao dao = openReadableDb().getDepartmentDao();
        List<DepartmentEntity> result = dao.loadAll();
        return result;
    }

    /**-------------------------下面开始User 操作相关---------------------------------------*/
    /**
     * @return
     *  todo  USER_STATUS_LEAVE
     */
    public List<UserEntity> loadAllUsers(){
        UserDao dao = openReadableDb().getUserDao();
        List<UserEntity> result = dao.loadAll();
        return result;
    }

    public UserEntity getByUserName(String uName){
        UserDao dao = openReadableDb().getUserDao();
        UserEntity entity = dao.queryBuilder().where(UserDao.Properties.PinyinName.eq(uName)).unique();
        return entity;
    }

    public UserEntity getByLoginId(int loginId){
        UserDao dao = openReadableDb().getUserDao();
        UserEntity entity = dao.queryBuilder().where(UserDao.Properties.PeerId.eq(loginId)).unique();
        return entity;
    }


    public void insertOrUpdateUser(UserEntity entity){
        UserDao userDao =  openWritableDb().getUserDao();
        long rowId = userDao.insertOrReplace(entity);
    }

    public void  batchInsertOrUpdateUser(List<UserEntity> entityList){
        if(entityList.size() <=0){
            return ;
        }
        UserDao userDao =  openWritableDb().getUserDao();
        userDao.insertOrReplaceInTx(entityList);
    }

    /**update*/
    public int getUserInfoLastTime(){
        UserDao sessionDao =  openReadableDb().getUserDao();
        UserEntity userEntity = sessionDao.queryBuilder()
                .orderDesc(UserDao.Properties.Updated)
                .limit(1)
                .unique();
        if(userEntity == null){
            return 0;
        }else{
            return userEntity.getUpdated();
        }
    }

    /**-------------------------下面开始Group 操作相关---------------------------------------*/
    /**
     * 载入Group的所有数据
     * @return
     */
    public List<GroupEntity> loadAllGroup(){
        GroupDao dao = openReadableDb().getGroupDao();
        List<GroupEntity> result = dao.loadAll();
        return result;
    }
    public  long insertOrUpdateGroup(GroupEntity groupEntity){
        GroupDao dao = openWritableDb().getGroupDao();
        long pkId =  dao.insertOrReplace(groupEntity);
        return pkId;
    }
    public void batchInsertOrUpdateGroup(List<GroupEntity> entityList){
        if(entityList.size() <=0){
            return;
        }
        GroupDao dao = openWritableDb().getGroupDao();
        dao.insertOrReplaceInTx(entityList);
    }

    /**-------------------------下面开始session 操作相关---------------------------------------*/
    /**
     * 载入session 表中的所有数据
     * @return
     */
    public List<SessionEntity> loadAllSession(){
        SessionDao dao = openReadableDb().getSessionDao();
        List<SessionEntity> result = dao.queryBuilder()
                .orderDesc(SessionDao.Properties.Updated)
                .list();
        return result;
    }

    public  long insertOrUpdateSession(SessionEntity sessionEntity){
        SessionDao dao = openWritableDb().getSessionDao();
        long pkId =  dao.insertOrReplace(sessionEntity);
        return pkId;
    }
    public void batchInsertOrUpdateSession(List<SessionEntity> entityList){
        if(entityList.size() <=0){
            return;
        }
        SessionDao dao = openWritableDb().getSessionDao();
        dao.insertOrReplaceInTx(entityList);
    }

    public void deleteSession(String sessionKey){
        SessionDao sessionDao =  openWritableDb().getSessionDao();
        DeleteQuery<SessionEntity> bd = sessionDao.queryBuilder()
                .where(SessionDao.Properties.SessionKey.eq(sessionKey))
                .buildDelete();

        bd.executeDeleteWithoutDetachingEntities();
    }

    /**
     * 获取最后回话的时间，便于获取联系人列表变化
     * 问题: 本地消息发送失败，依旧会更新session的时间 [存在会话、不存在的会话]
     * 本质上还是最后一条成功消息的时间
     * @return
     */
    public int getSessionLastTime(){
        int timeLine = 0;
        MessageDao messageDao =  openReadableDb().getMessageDao();
        String successType = String.valueOf(MessageConstant.MSG_SUCCESS);
        String sql = "select created from Message where status=? order by created desc limit 1";
        Cursor cursor =  messageDao.getDatabase().rawQuery(sql, new String[]{successType});
        try {
            if(cursor!=null && cursor.getCount() ==1){
                cursor.moveToFirst();
                timeLine = cursor.getInt(0);
            }
        }catch (Exception e){
           logger.e("DBInterface#getSessionLastTime cursor 查询异常");
        }finally {
            cursor.close();
        }
        return timeLine;
    }

    /**-------------------------下面开始message 操作相关---------------------------------------*/

    // where (msgId >= startMsgId and msgId<=lastMsgId) or
    // (msgId=0 and status = 0)
    // order by created desc
    // limit count;
    // 按照时间排序
    public List<MessageEntity> getHistoryMsg(String chatKey,int lastMsgId,int lastCreateTime,int count){
        /**解决消息重复的问题*/
        int preMsgId = lastMsgId +1;
        MessageDao dao = openReadableDb().getMessageDao(); /** 找到对应的表 */
        List<MessageEntity> listMsg =
                dao.queryBuilder()
                        .where(
                                 //MessageDao.Properties.Created.le(lastCreateTime)
                                MessageDao.Properties.SessionKey.eq(chatKey)
                                ,MessageDao.Properties.MsgId.notEq(preMsgId)
                        )
                        .whereOr(
                                MessageDao.Properties.MsgId.le(lastMsgId),
                                MessageDao.Properties.MsgId.gt(90000000)
                        )
                        .orderDesc(MessageDao.Properties.Created)
                        .orderDesc(MessageDao.Properties.MsgId)
                        /** 限制条数 */
                        .limit(count)
                        .list();
        return formatMessage(listMsg);
    }

    /**
     * IMGetLatestMsgIdReq 后去最后一条合法的msgid
     * */
    public List<Integer> refreshHistoryMsgId(String chatKey, int beginMsgId, int lastMsgId){
        MessageDao dao = openReadableDb().getMessageDao();

        String sql = "select MSG_ID from Message where SESSION_KEY = ? and MSG_ID >= ? and MSG_ID <= ? order by MSG_ID asc";
        Cursor cursor =  dao.getDatabase().rawQuery(sql, new String[]{chatKey, String.valueOf(beginMsgId), String.valueOf(lastMsgId)});

        List<Integer> msgIdList = new ArrayList<>();
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int msgId = cursor.getInt(0);
                msgIdList.add(msgId);
            }
        }finally {
            cursor.close();
        }
        return msgIdList;
    }


    public long insertOrUpdateMix(MessageEntity message){
        MessageDao dao = openWritableDb().getMessageDao();
        MessageEntity parent =  dao.queryBuilder().where(MessageDao.Properties.MsgId.eq(message.getMsgId())
        ,MessageDao.Properties.SessionKey.eq(message.getSessionKey())).unique();

        long resId = parent.getId();
        if(parent.getDisplayType() != DBConstant.SHOW_MIX_TEXT){
            return resId;
        }

        boolean needUpdate = false;
        MixMessage mixParent = (MixMessage) formatMessage(parent);
        List<MessageEntity> msgList = mixParent.getMsgList();
        for(int index =0;index < msgList.size(); index ++){
            if(msgList.get(index).getId() ==  message.getId()){
                msgList.set(index, message);
                needUpdate = true;
                break;
            }
        }

        if(needUpdate){
            mixParent.setMsgList(msgList);
            long pkId = dao.insertOrReplace(mixParent);
            return pkId;
        }
       return resId;
    }

    /**有可能是混合消息
     * 批量接口{batchInsertOrUpdateMessage} 没有存在场景
     * */
    public long insertOrUpdateMessage(MessageEntity message){
        if(message.getId()!=null && message.getId() < 0){
            // mix消息
            return insertOrUpdateMix(message);
        }
        MessageDao dao = openWritableDb().getMessageDao();
        long pkId = dao.insertOrReplace(message);
        return pkId;
    }

    /**
     * todo 这个地方调用存在特殊场景，如果list中包含Id为负Mix子类型，更新就有问题
     * 现在的调用列表没有这个情景，使用的时候注意
     * */
    public void batchInsertOrUpdateMessage(List<MessageEntity> entityList){
        MessageDao dao = openWritableDb().getMessageDao();
        dao.insertOrReplaceInTx(entityList);
    }


    public void deleteMessageById(long localId){
        if(localId<=0){return;}
        Set<Long> setIds = new TreeSet<>();
        setIds.add(localId);
        batchDeleteMessageById(setIds);
    }

    public void batchDeleteMessageById(Set<Long> pkIds){
        if(pkIds.size() <=0){
            return;
        }
        MessageDao dao = openWritableDb().getMessageDao();
        dao.deleteByKeyInTx(pkIds);
    }

    public void deleteMessageByMsgId(int msgId){
        if(msgId <= 0){
            return;
        }
        MessageDao messageDao =  openWritableDb().getMessageDao();
        QueryBuilder<MessageEntity> qb = openWritableDb().getMessageDao().queryBuilder();
        DeleteQuery<MessageEntity> bd = qb.where(MessageDao.Properties.MsgId.eq(msgId)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    public MessageEntity getMessageByMsgId(int messageId){
        MessageDao dao = openReadableDb().getMessageDao();
        Query query = dao.queryBuilder().where(
                MessageDao.Properties.Id.eq(messageId))
                .build();
        return formatMessage((MessageEntity)query.unique());
    }

    /**根据主键查询
     * not use
     * */
    public MessageEntity getMessageById(long localId){
        MessageDao dao = openReadableDb().getMessageDao();
        MessageEntity messageEntity=
                dao.queryBuilder().where(MessageDao.Properties.Id.eq(localId)).unique();
        return formatMessage(messageEntity);
    }


    private MessageEntity formatMessage(MessageEntity msg){
         MessageEntity messageEntity = null;
            int displayType = msg.getDisplayType();
            switch (displayType){
                case DBConstant.SHOW_MIX_TEXT:
                    try {
                        messageEntity =  MixMessage.parseFromDB(msg);
                    } catch (JSONException e) {
                        logger.e(e.toString());
                    }
                    break;
                case DBConstant.SHOW_AUDIO_TYPE:
                    messageEntity = AudioMessage.parseFromDB(msg);
                    break;
                case DBConstant.SHOW_IMAGE_TYPE:
                    messageEntity = ImageMessage.parseFromDB(msg);
                    break;
                case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
                    messageEntity = TextMessage.parseFromDB(msg);
                    break;
            }
        return messageEntity;
    }


    public List<MessageEntity> formatMessage(List<MessageEntity> msgList){
        if(msgList.size() <= 0){
            Log.d("zzzzz","msgList.size() <= 0");
            return Collections.emptyList();
        }
        ArrayList<MessageEntity> newList = new ArrayList<>();
        for(MessageEntity info:msgList){
            int displayType = info.getDisplayType();
            switch (displayType){
                case DBConstant.SHOW_MIX_TEXT:
                    try {
                        newList.add(MixMessage.parseFromDB(info));
                    } catch (JSONException e) {
                        logger.e(e.toString());
                    }
                    break;
                case DBConstant.SHOW_AUDIO_TYPE:
                    newList.add(AudioMessage.parseFromDB(info));
                    break;
                case DBConstant.SHOW_IMAGE_TYPE:
                    newList.add(ImageMessage.parseFromDB(info));
                    break;
                case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
                    newList.add(TextMessage.parseFromDB(info));
                    break;
            }
        }
        return newList;
    }

}
