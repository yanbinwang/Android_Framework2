package com.example.greendao.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.example.greendao.bean.PartDB;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "PART_DB".
*/
public class PartDBDao extends AbstractDao<PartDB, String> {

    public static final String TABLENAME = "PART_DB";

    /**
     * Properties of entity PartDB.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Baoquan_no = new Property(0, String.class, "baoquan_no", true, "BAOQUAN_NO");
        public final static Property SourcePath = new Property(1, String.class, "sourcePath", false, "SOURCE_PATH");
        public final static Property UserId = new Property(2, String.class, "userId", false, "USER_ID");
        public final static Property Index = new Property(3, int.class, "index", false, "INDEX");
        public final static Property FilePointer = new Property(4, long.class, "filePointer", false, "FILE_POINTER");
        public final static Property State = new Property(5, int.class, "state", false, "STATE");
        public final static Property Extras = new Property(6, String.class, "extras", false, "EXTRAS");
    }


    public PartDBDao(DaoConfig config) {
        super(config);
    }
    
    public PartDBDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"PART_DB\" (" + //
                "\"BAOQUAN_NO\" TEXT PRIMARY KEY NOT NULL ," + // 0: baoquan_no
                "\"SOURCE_PATH\" TEXT," + // 1: sourcePath
                "\"USER_ID\" TEXT," + // 2: userId
                "\"INDEX\" INTEGER NOT NULL ," + // 3: index
                "\"FILE_POINTER\" INTEGER NOT NULL ," + // 4: filePointer
                "\"STATE\" INTEGER NOT NULL ," + // 5: state
                "\"EXTRAS\" TEXT);"); // 6: extras
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"PART_DB\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, PartDB entity) {
        stmt.clearBindings();
 
        String baoquan_no = entity.getBaoquan_no();
        if (baoquan_no != null) {
            stmt.bindString(1, baoquan_no);
        }
 
        String sourcePath = entity.getSourcePath();
        if (sourcePath != null) {
            stmt.bindString(2, sourcePath);
        }
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(3, userId);
        }
        stmt.bindLong(4, entity.getIndex());
        stmt.bindLong(5, entity.getFilePointer());
        stmt.bindLong(6, entity.getState());
 
        String extras = entity.getExtras();
        if (extras != null) {
            stmt.bindString(7, extras);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, PartDB entity) {
        stmt.clearBindings();
 
        String baoquan_no = entity.getBaoquan_no();
        if (baoquan_no != null) {
            stmt.bindString(1, baoquan_no);
        }
 
        String sourcePath = entity.getSourcePath();
        if (sourcePath != null) {
            stmt.bindString(2, sourcePath);
        }
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(3, userId);
        }
        stmt.bindLong(4, entity.getIndex());
        stmt.bindLong(5, entity.getFilePointer());
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
    public PartDB readEntity(Cursor cursor, int offset) {
        PartDB entity = new PartDB( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // baoquan_no
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // sourcePath
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // userId
            cursor.getInt(offset + 3), // index
            cursor.getLong(offset + 4), // filePointer
            cursor.getInt(offset + 5), // state
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6) // extras
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, PartDB entity, int offset) {
        entity.setBaoquan_no(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setSourcePath(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setUserId(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setIndex(cursor.getInt(offset + 3));
        entity.setFilePointer(cursor.getLong(offset + 4));
        entity.setState(cursor.getInt(offset + 5));
        entity.setExtras(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
     }
    
    @Override
    protected final String updateKeyAfterInsert(PartDB entity, long rowId) {
        return entity.getBaoquan_no();
    }
    
    @Override
    public String getKey(PartDB entity) {
        if(entity != null) {
            return entity.getBaoquan_no();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(PartDB entity) {
        return entity.getBaoquan_no() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
