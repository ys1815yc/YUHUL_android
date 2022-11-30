package com.gis.heartio;

import android.app.Application;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import androidx.multidex.MultiDexApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Cavin on 2018/1/10.
 */

public class heartioApplication extends MultiDexApplication{

    private ArrayList<HashMap<String, BluetoothGattService>> mGattServiceMasterData =
            new ArrayList<HashMap<String, BluetoothGattService>>();


    private List<BluetoothGattCharacteristic> mGattDataCharacteristics;
    private List<BluetoothGattCharacteristic> mGattGainCharacteristics;
    private List<BluetoothGattCharacteristic> mGattAngleEstimateCharacteristics;
    private List<BluetoothGattCharacteristic> mGattDeviceInfoCharacteristics;
    private BluetoothGattCharacteristic mBluetoothgattDatacharacteristic;
    private BluetoothGattCharacteristic mBluetoothgattGaincharacteristic;
    private BluetoothGattCharacteristic mBluetoothgattAngleEstimatecharacteristic;
    private BluetoothGattCharacteristic mBluetoothgattFirmwareVersioncharacteristic;
    private BluetoothGattCharacteristic mBluetoothgattMacAddrcharacteristic;
    private BluetoothGattCharacteristic mBluetoothgattUdiParacharacteristic;
    private BluetoothGattDescriptor mBluetoothGattDescriptor;

    private BluetoothGattCharacteristic mBluetoothgattRuncharacteristic;

    public static Boolean mApplicationInBackground = false;

    //added by brandon
    private boolean mBoolBackFromUltrasoundActivity=false;

    public heartioApplication() {

    }

    /**
     * getter method for Blue tooth GATT characteristic
     *
     * @return {@link BluetoothGattCharacteristic}
     */
    public BluetoothGattCharacteristic getBluetoothgattDatacharacteristic() {
        return mBluetoothgattDatacharacteristic;
    }

    public BluetoothGattCharacteristic getBluetoothgattGaincharacteristic() {
        return mBluetoothgattGaincharacteristic;
    }

    public BluetoothGattCharacteristic getmBluetoothgattAngleEstimatecharacteristic() {
        return mBluetoothgattAngleEstimatecharacteristic;
    }

    public BluetoothGattCharacteristic getBluetoothgattFirmwareVersioncharacteristic() {
        return mBluetoothgattFirmwareVersioncharacteristic;
    }

    public BluetoothGattCharacteristic getBluetoothgattMacAddrcharacteristic() {
        return mBluetoothgattMacAddrcharacteristic;
    }

    public BluetoothGattCharacteristic getBluetoothgattUdiParacharacteristic() {
        return mBluetoothgattUdiParacharacteristic;
    }

    public BluetoothGattCharacteristic getBluetoothgattRuncharacteristic() {
        return mBluetoothgattRuncharacteristic;
    }

    /**
     * setter method for Blue tooth GATT characteristics
     *
     * @param bluetoothgattcharacteristic
     */
    public void setBluetoothgattDatacharacteristic(BluetoothGattCharacteristic bluetoothgattcharacteristic) {
        mBluetoothgattDatacharacteristic = bluetoothgattcharacteristic;
    }

    public void setBluetoothgattGaincharacteristic(BluetoothGattCharacteristic bluetoothgattcharacteristic) {
        mBluetoothgattGaincharacteristic = bluetoothgattcharacteristic;
    }

    public void setBluetoothgattAngleEstimatecharacteristic(BluetoothGattCharacteristic bluetoothgattcharacteristic) {
        mBluetoothgattAngleEstimatecharacteristic = bluetoothgattcharacteristic;
    }

    public void setBluetoothgattFirmwareVersionharacteristic(BluetoothGattCharacteristic bluetoothgattcharacteristic){
        mBluetoothgattFirmwareVersioncharacteristic = bluetoothgattcharacteristic;
    }

    public void setBluetoothgattMacAddrcharacteristic(BluetoothGattCharacteristic bluetoothGattcharacteristic){
        mBluetoothgattMacAddrcharacteristic = bluetoothGattcharacteristic;
    }

    public void setBluetoothgattUdiParacharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic){
        mBluetoothgattUdiParacharacteristic = bluetoothGattCharacteristic;
    }

    public void setBluetoothgattRuncharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic){
        mBluetoothgattRuncharacteristic = bluetoothGattCharacteristic;
    }

    /**
     * getter method for Blue tooth GATT characteristic
     *
     * @return {@link BluetoothGattCharacteristic}
     */
    public BluetoothGattDescriptor getBluetoothgattDescriptor() {
        return mBluetoothGattDescriptor;
    }

    /**
     * setter method for Blue tooth GATT Descriptor
     *
     * @param bluetoothGattDescriptor
     */
    public void setBluetoothgattdescriptor(
            BluetoothGattDescriptor bluetoothGattDescriptor) {
        this.mBluetoothGattDescriptor = bluetoothGattDescriptor;
    }

    /**
     * getter method for blue tooth GATT Characteristic list
     *
     * @return {@link List<BluetoothGattCharacteristic>}
     */
    public List<BluetoothGattCharacteristic> getGattDataCharacteristics() {
        return mGattDataCharacteristics;
    }

    public List<BluetoothGattCharacteristic> getGattGainCharacteristics() {
        return mGattGainCharacteristics;
    }

    public List<BluetoothGattCharacteristic> getGattDeviceInfoCharacteristics() {
        return mGattDeviceInfoCharacteristics;
    }

    /**
     * setter method for blue tooth GATT Characteristic list
     *
     * @param gattCharacteristics
     */
    public void setGattDataCharacteristics(
            List<BluetoothGattCharacteristic> gattCharacteristics) {
        this.mGattDataCharacteristics = gattCharacteristics;
    }

    public void setGattGainCharacteristics(
            List<BluetoothGattCharacteristic> gattCharacteristics) {
        this.mGattGainCharacteristics = gattCharacteristics;
    }

    public ArrayList<HashMap<String, BluetoothGattService>> getGattServiceMasterData() {
        return mGattServiceMasterData;
    }

    public void setGattServiceMasterData(
            ArrayList<HashMap<String, BluetoothGattService>> gattServiceMasterData) {
        this.mGattServiceMasterData = gattServiceMasterData;

    }

    public void setGattDeviceInfoCharacteristics(
            List<BluetoothGattCharacteristic> gattCharacteristics){
        this.mGattDeviceInfoCharacteristics = gattCharacteristics;
    }
}
