package com.example.greendao.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.io.File;

/**
 * 文件分片数据库
 */
@Entity
public class PartDB {
    @Id
    private String baoquan_no;//文件唯一识别码（保全号）
    private String sourcePath;//文件唯一识别码（保留字段）
    private String userId;//用户id
    private int index;//当前下标
    private long filePointer;//切片的下标
    private int state;//0上传中 1上传失败 2上传完成（证据缺失直接校验源文件路径）
    private String extras;//保留字段

    @Generated(hash = 1663948320)
    public PartDB(String baoquan_no, String sourcePath, String userId, int index, long filePointer, int state, String extras) {
        this.baoquan_no = baoquan_no;
        this.sourcePath = sourcePath;
        this.userId = userId;
        this.index = index;
        this.filePointer = filePointer;
        this.state = state;
        this.extras = extras;
    }
    @Generated(hash = 391260787)
    public PartDB() {
    }

    public String getBaoquan_no() {
        return this.baoquan_no;
    }
    public void setBaoquan_no(String baoquan_no) {
        this.baoquan_no = baoquan_no;
    }
    public String getSourcePath() {
        return this.sourcePath;
    }
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }
    public String getUserId() {
        return this.userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public int getIndex() {
        return this.index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public long getFilePointer() {
        return this.filePointer;
    }
    public void setFilePointer(long filePointer) {
        this.filePointer = filePointer;
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

    /**
     * 获取分片总数
     */
    public int getTotal() {
        long targetLength = new File(sourcePath).length();
        return targetLength % getSize() == 0 ? (int) (targetLength / getSize()) : (int) (targetLength / getSize() + 1);
    }

    /**
     * 配置分片大小
     */
    public long getSize() {
        return 100 * 1024 * 1024;
    }

}