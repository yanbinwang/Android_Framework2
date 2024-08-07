package com.example.greendao.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.example.greendao.bean.OssDB;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "OSS_DB".
*/
public class OssDBDao extends AbstractDao<OssDB, String> {

    public static final String TABLENAME = "OSS_DB";

    /**
     * Properties of entity OssDB.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Baoquan = new Property(0, String.class, "baoquan", true, "BAOQUAN");
        public final static Property UserId = new Property(1, String.class, "userId", false, "USER_ID");
        public final static Property SourcePath = new Property(2, String.class, "sourcePath", false, "SOURCE_PATH");
        public final static Property ObjectName = new Property(3, String.class, "objectName", false, "OBJECT_NAME");
        public final static Property ObjectKey = new Property(4, String.class, "objectKey", false, "OBJECT_KEY");
        public final static Property State = new Property(5, int.class, "state", false, "STATE");
        public final static Property Extras = new Property(6, String.class, "extras", false, "EXTRAS");
    }


    public OssDBDao(DaoConfig config) {
        super(config);
    }
    
    public OssDBDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"OSS_DB\" (" + //
                "\"BAOQUAN\" TEXT PRIMARY KEY NOT NULL ," + // 0: baoquan
                "\"USER_ID\" TEXT," + // 1: userId
                "\"SOURCE_PATH\" TEXT," + // 2: sourcePath
                "\"OBJECT_NAME\" TEXT," + // 3: objectName
                "\"OBJECT_KEY\" TEXT," + // 4: objectKey
                "\"STATE\" INTEGER NOT NULL ," + // 5: state
                "\"EXTRAS\" TEXT);"); // 6: extras
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"OSS_DB\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, OssDB entity) {
        stmt.clearBindings();
 
        String baoquan = entity.getBaoquan();
        if (baoquan != null) {
            stmt.bindString(1, baoquan);
        }
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(2, userId);
        }
 
        String sourcePath = entity.getSourcePath();
        if (sourcePath != null) {
            stmt.bindString(3, sourcePath);
        }
 
        String objectName = entity.getObjectName();
        if (objectName != null) {
            stmt.bindString(4, objectName);
        }
 
        String objectKey = entity.getObjectKey();
        if (objectKey != null) {
            stmt.bindString(5, objectKey);
        }
        stmt.bindLong(6, entity.getState());
 
        String extras = entity.getExtras();
        if (extras != null) {
            stmt.bindString(7, extras);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, OssDB entity) {
        stmt.clearBindings();
 
        String baoquan = entity.getBaoquan();
        if (baoquan != null) {
            stmt.bindString(1, baoquan);
        }
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(2, userId);
        }
 
        String sourcePath = entity.getSourcePath();
        if (sourcePath != null) {
            stmt.bindString(3, sourcePath);
        }
 
        String objectName = entity.getObjectName();
        if (objectName != null) {
            stmt.bindString(4, objectName);
        }
 
        String objectKey = entity.getObjectKey();
        if (objectKey != null) {
            stmt.bindString(5, objectKey);
        }
        stmt.bindLong(6, entity.getState());
 
        String extras = entity.getExtras();
        if (extras != null) {
            stmt.bindString(7, extras);
        }
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public OssDB readEntity(Cursor cursor, int offset) {
        OssDB entity = new OssDB( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // baoquan
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // userId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // sourcePath
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // objectName
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // objectKey
            cursor.getInt(offset + 5), // state
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6) // extras
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, OssDB entity, int offset) {
        entity.setBaoquan(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setUserId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setSourcePath(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setObjectName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setObjectKey(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setState(cursor.getInt(offset + 5));
        entity.setExtras(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
     }
    
    @Override
    protected final String updateKeyAfterInsert(OssDB entity, long rowId) {
        return entity.getBaoquan();
    }
    
    @Override
    public String getKey(OssDB entity) {
        if(entity != null) {
            return entity.getBaoquan();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(OssDB entity) {
        return entity.getBaoquan() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
