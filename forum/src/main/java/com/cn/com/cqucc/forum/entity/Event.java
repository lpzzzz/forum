package com.cn.com.cqucc.forum.entity;


import java.util.HashMap;
import java.util.Map;

/**
 * 事件实体
 */
public class Event {

    private String topic; // 事件主题
    private int userId; // 触发事件的 用户id
    private int entityType; // 实体类型
    private int entityId;
    private int entityUserId;
    private Map<String, Object> data = new HashMap<>(); // 用于数据扩展如果有更多的数据将网map中存

    public String getTopic() {
        return topic;
    }

    //设计 set方法 返回值 为事件对象本身这样可以进行连续调用
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    // 重构data的set方法
    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return "Event{" +
                "topic='" + topic + '\'' +
                ", userId=" + userId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", entityUserId=" + entityUserId +
                ", data=" + data +
                '}';
    }
}
