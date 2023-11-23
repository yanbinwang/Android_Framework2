package com.example.thirdparty.greendao.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.File;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class EvidenceDB {
    @Id
    private String baoquan;//文件唯一识别码（保全号）
    private String sourcePath;//源文件路径
    private String userId;//用户id
    private int index;//当前下标
    private long filePointer;//切片的下标
    private boolean isUpload;//是否正在提交
    private boolean isComplete;//是否成功提交

    @Generated(hash = 1707638910)
    public EvidenceDB() {
    }
    @Generated(hash = 748046952)
    public EvidenceDB(String baoquan, String sourcePath, String userId, int index, long filePointer, boolean isUpload, boolean isComplete) {
        this.baoquan = baoquan;
        this.sourcePath = sourcePath;
        this.userId = userId;
        this.index = index;
        this.filePointer = filePointer;
        this.isUpload = isUpload;
        this.isComplete = isComplete;
    }

    //获取分片总数
    public int getTotal() {
        long targetLength = new File(sourcePath).length();
        return targetLength % getSize() == 0 ? (int) (targetLength / getSize()) : (int) (targetLength / getSize() + 1);
    }

    //配置分片大小
    public long getSize() {
        return 100 * 1024 * 1024;
    }

    public String getBaoquan() {
        return this.baoquan;
    }

    public void setBaoquan(String baoquan) {
        this.baoquan = baoquan;
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

    public boolean getIsUpload() {
        return this.isUpload;
    }

    public void setIsUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    public boolean getIsComplete() {
        return this.isComplete;
    }

    public void setIsComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }
}
