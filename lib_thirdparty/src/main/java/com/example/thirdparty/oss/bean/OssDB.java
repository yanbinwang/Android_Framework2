package com.example.thirdparty.oss.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 针对oss创建的数据库bean
 */
@Entity
public class OssDB {
    @Id
    private String baoquan;//文件唯一识别码（保全号）
    private String userId;//当前用户的id(区别不同身份)
    private String sourcePath;//文件在手机中的路径
    private String objectName;//oss文件夹命名（每次都会根据时间戳产生，用于下次断点续传）
    private String objectKey;//oss上传完成后，服务器需要记录的值
    private int state;//0上传中 1上传失败 2上传完成（证据缺失直接校验源文件路径）
    private String extras;//保留字段

    @Generated(hash = 338958684)
    public OssDB(String baoquan, String userId, String sourcePath, String objectName, String objectKey, int state, String extras) {
        this.baoquan = baoquan;
        this.userId = userId;
        this.sourcePath = sourcePath;
        this.objectName = objectName;
        this.objectKey = objectKey;
        this.state = state;
        this.extras = extras;
    }
    @Generated(hash = 856899489)
    public OssDB() {
    }

    public String getBaoquan() {
        return this.baoquan;
    }
    public void setBaoquan(String baoquan) {
        this.baoquan = baoquan;
    }
    public String getUserId() {
        return this.userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getSourcePath() {
        return this.sourcePath;
    }
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }
    public int getState() {
        return this.state;
    }
    public void setState(int state) {
        this.state = state;
    }
    public String getExtras() {
        return this.extras;
    }
    public void setExtras(String extras) {
        this.extras = extras;
    }
    public String getObjectName() {
        return this.objectName;
    }
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
    public String getObjectKey() {
        return this.objectKey;
    }
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

}