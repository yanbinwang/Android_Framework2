package com.example.greendao.dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.example.greendao.bean.OssDB;

import com.example.greendao.dao.OssDBDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig ossDBDaoConfig;

    private final OssDBDao ossDBDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        ossDBDaoConfig = daoConfigMap.get(OssDBDao.class).clone();
        ossDBDaoConfig.initIdentityScope(type);

        ossDBDao = new OssDBDao(ossDBDaoConfig, this);

        registerDao(OssDB.class, ossDBDao);
    }
    
    public void clear() {
        ossDBDaoConfig.clearIdentityScope();
    }

    public OssDBDao getOssDBDao() {
        return ossDBDao;
    }

}
