package com.mariston.redis;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * 心电图
 *
 * @author mariston
 * @version V1.0
 * @since 2017/09/22
 */
public class Heart implements Serializable {

    private static final long serialVersionUID = 7127604719543001677L;
    /**
     * 修改时间
     */
    private int monitoredTime;

    /**
     * 通道
     */
    private int channel;

    /**
     * 采样率
     */
    private int sampleRate;

    /**
     * 波形数据
     */
    private byte[] data;

    /**
     * 导联数
     */
    private short leadEvent;

    /**
     * 备注
     */
    private String remark;

    public int getMonitoredTime() {
        return monitoredTime;
    }

    public void setMonitoredTime(int monitoredTime) {
        this.monitoredTime = monitoredTime;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public short getLeadEvent() {
        return leadEvent;
    }

    public void setLeadEvent(short leadEvent) {
        this.leadEvent = leadEvent;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
