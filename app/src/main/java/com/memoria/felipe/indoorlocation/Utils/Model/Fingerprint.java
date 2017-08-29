package com.memoria.felipe.indoorlocation.Utils.Model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Created by felip on 23-07-2017.
 */
@Entity
public class Fingerprint {

    @Id
    private Long Id;

    private Double xPosition;
    private Double yPosition;

    @ToMany(referencedJoinProperty = "fingerprintId")
    @OrderBy("beaconId ASC")
    List<Beacon_RSSI> rssi;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1556433806)
    private transient FingerprintDao myDao;



    @Generated(hash = 621753556)
    public Fingerprint() {
    }



    @Generated(hash = 1415065902)
    public Fingerprint(Long Id, Double xPosition, Double yPosition) {
        this.Id = Id;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }



    public Long getId() {
        return this.Id;
    }

    public void setId(Long Id) {
        this.Id = Id;
    }

    public Double getXPosition() {
        return this.xPosition;
    }

    public void setXPosition(Double xPosition) {
        this.xPosition = xPosition;
    }

    public Double getYPosition() {
        return this.yPosition;
    }

    public void setYPosition(Double yPosition) {
        this.yPosition = yPosition;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 442481294)
    public List<Beacon_RSSI> getRssi() {
        if (rssi == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            Beacon_RSSIDao targetDao = daoSession.getBeacon_RSSIDao();
            List<Beacon_RSSI> rssiNew = targetDao._queryFingerprint_Rssi(Id);
            synchronized (this) {
                if (rssi == null) {
                    rssi = rssiNew;
                }
            }
        }
        return rssi;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 2135351388)
    public synchronized void resetRssi() {
        rssi = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 540710114)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getFingerprintDao() : null;
    }

}