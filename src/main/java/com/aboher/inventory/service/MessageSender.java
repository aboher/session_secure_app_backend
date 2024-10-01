package com.aboher.inventory.service;

public interface MessageSender<T> {

    void sendMessage(T message);
}
