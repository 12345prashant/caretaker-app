//package com.example.caretakerapp;
//
//public class Message {
//    private String id;
//    private String sender;
//    private String receiver;
//    private String text;
//    private long timestamp;
//    private String status;
//    private boolean isAnimated;
//
//    public Message() {
//        // Default constructor required for Firebase
//        this.isAnimated = false;
//    }
//
//    // Getters and setters
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getSender() {
//        return sender;
//    }
//
//    public void setSender(String sender) {
//        this.sender = sender;
//    }
//
//    public String getReceiver() {
//        return receiver;
//    }
//
//    public void setReceiver(String receiver) {
//        this.receiver = receiver;
//    }
//
//    public String getText() {
//        return text;
//    }
//
//    public void setText(String text) {
//        this.text = text;
//    }
//
//    public long getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(long timestamp) {
//        this.timestamp = timestamp;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public boolean isAnimated() {
//        return isAnimated;
//    }
//
//    public void setAnimated(boolean animated) {
//        isAnimated = animated;
//    }
//}

package com.example.caretakerapp;

public class Message {
    private String id;
    private String sender;
    private String text;
    private long timestamp;
    private String status;

    public Message() {
        // Default constructor required for Firebase
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}