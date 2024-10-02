package com.aboher.sessionsecureapp.service;

public interface MessageSender<T> {

    void sendMessage(T message);
}
