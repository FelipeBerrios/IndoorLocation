package com.memoria.felipe.indoorlocation.Utils.Model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by felip on 12-07-2017.
 */
@Entity
public class Beacons {

    @Id
    private Long Id;

    @NotNull
    private String MAC;
    private String NameSpace;
    private String InstanceId;
    private String Name;
    private int TxPower;
    private String UniqueId;
    private int Rssi;
    private Double XPosition;
    private Double YPosition;
    @Generated(hash = 1571348315)
    public Beacons(Long Id, @NotNull String MAC, String NameSpace,
            String InstanceId, String Name, int TxPower, String UniqueId, int Rssi,
            Double XPosition, Double YPosition) {
        this.Id = Id;
        this.MAC = MAC;
        this.NameSpace = NameSpace;
        this.InstanceId = InstanceId;
        this.Name = Name;
        this.TxPower = TxPower;
        this.UniqueId = UniqueId;
        this.Rssi = Rssi;
        this.XPosition = XPosition;
        this.YPosition = YPosition;
    }
    @Generated(hash = 589740425)
    public Beacons() {
    }
    public Long getId() {
        return this.Id;
    }
    public void setId(Long Id) {
        this.Id = Id;
    }
    public String getMAC() {
        return this.MAC;
    }
    public void setMAC(String MAC) {
        this.MAC = MAC;
    }
    public String getNameSpace() {
        return this.NameSpace;
    }
    public void setNameSpace(String NameSpace) {
        this.NameSpace = NameSpace;
    }
    public String getInstanceId() {
        return this.InstanceId;
    }
    public void setInstanceId(String InstanceId) {
        this.InstanceId = InstanceId;
    }
    public String getName() {
        return this.Name;
    }
    public void setName(String Name) {
        this.Name = Name;
    }
    public int getTxPower() {
        return this.TxPower;
    }
    public void setTxPower(int TxPower) {
        this.TxPower = TxPower;
    }
    public String getUniqueId() {
        return this.UniqueId;
    }
    public void setUniqueId(String UniqueId) {
        this.UniqueId = UniqueId;
    }
    public int getRssi() {
        return this.Rssi;
    }
    public void setRssi(int Rssi) {
        this.Rssi = Rssi;
    }
    public Double getXPosition() {
        return this.XPosition;
    }
    public void setXPosition(Double XPosition) {
        this.XPosition = XPosition;
    }
    public Double getYPosition() {
        return this.YPosition;
    }
    public void setYPosition(Double YPosition) {
        this.YPosition = YPosition;
    }



    
}
