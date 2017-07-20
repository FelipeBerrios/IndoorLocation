package com.memoria.felipe.indoorlocation.Utils;

/**
 * Created by felip on 18-07-2017.
 */

public class CustomBeacon {

    private String MAC;
    private String NameSpace;
    private String InstanceId;
    private String Name;
    private int TxPower;
    private String UniqueId;
    private int Rssi;

    public CustomBeacon() {
        this.Rssi = -10000;
    }

    public CustomBeacon(CustomBeacon original){
        this.MAC = original.getMAC();
        this.NameSpace = original.getNameSpace();
        this.InstanceId = original.getInstanceId();
        this.Name = original.getName();
        this.TxPower = original.getTxPower();
        this.UniqueId = original.getUniqueId();
        this.Rssi = original.getRssi();
    }

    public int getRssi() {
        return Rssi;
    }

    public void setRssi(int rssi) {
        Rssi = rssi;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public String getNameSpace() {
        return NameSpace;
    }

    public void setNameSpace(String nameSpace) {
        NameSpace = nameSpace;
    }

    public String getInstanceId() {
        return InstanceId;
    }

    public void setInstanceId(String instanceId) {
        InstanceId = instanceId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getTxPower() {
        return TxPower;
    }

    public void setTxPower(int txPower) {
        TxPower = txPower;
    }

    public String getUniqueId() {
        return UniqueId;
    }

    public void setUniqueId(String uniqueId) {
        UniqueId = uniqueId;
    }

    public void clearFields(){
        this.InstanceId = null;
        this.MAC = null;
        this.Name = null;
        this.NameSpace = null;
        this.Rssi = -10000;
        this.TxPower = 0;
        this.UniqueId = null;
    }
}
