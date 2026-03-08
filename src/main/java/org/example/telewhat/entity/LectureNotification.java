package org.example.telewhat.entity;

import java.io.Serializable;

public class LectureNotification implements Serializable {
    private static final long serialVersionUID = 1L;
    private String reader;
    private String sender;

    public LectureNotification(String reader, String sender) {
        this.reader = reader;
        this.sender = sender;
    }

    public String getReader() { return reader; }
    public String getSender() { return sender; }
}