package com.memoria.felipe.indoorlocation.Utils.Model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Created by felip on 23-07-2017.
 */
@Entity
public class Beacon_RSSI {
    @Id
    private Long Id;
    private Integer Rssi;
    private Long fingerprintId;

    @NotNull
    private Long beaconId;
    @ToOne(joinProperty = "beaconId")
    private Beacons beacon;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 955234591)
    private transient Beacon_RSSIDao myDao;
    @Generated(hash = 1950365892)
    public Beacon_RSSI(Long Id, Integer Rssi, Long fingerprintId,
            @NotNull Long beaconId) {
        this.Id = Id;
        this.Rssi = Rssi;
        this.fingerprintId = fingerprintId;
        this.beaconId = beaconId;
    }
    @Generated(hash = 1217178718)
    public Beacon_RSSI() {
    }
    public Long getId() {
        return this.Id;
    }
    public void setId(Long Id) {
        this.Id = Id;
    }
    public Integer getRssi() {
        return this.Rssi;
    }
    public void setRssi(Integer Rssi) {
        this.Rssi = Rssi;
    }
    public Long getFingerprintId() {
        return this.fingerprintId;
    }
    public void setFingerprintId(Long fingerprintId) {
        this.fingerprintId = fingerprintId;
    }
    public Long getBeaconId() {
        return this.beaconId;
    }
    public void setBeaconId(Long beaconId) {
        this.beaconId = beaconId;
    }
    @Generated(hash = 343574016)
    private transient Long beacon__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1700725075)
    public Beacons getBeacon() {
        Long __key = this.beaconId;
        if (beacon__resolvedKey == null || !beacon__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            BeaconsDao targetDao = daoSession.getBeaconsDao();
            Beacons beaconNew = targetDao.load(__key);
            synchronized (this) {
                beacon = beaconNew;
                beacon__resolvedKey = __key;
            }
        }
        return beacon;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 764428004)
    public void setBeacon(@NotNull Beacons beacon) {
        if (beacon == null) {
            throw new DaoException(
                    "To-one property 'beaconId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.beacon = beacon;
            beaconId = beacon.getId();
            beacon__resolvedKey = beaconId;
        }
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
    @Generated(hash = 1600583887)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getBeacon_RSSIDao() : null;
    }

}