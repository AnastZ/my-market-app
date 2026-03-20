package ru.ya.practicum.mymarket.services;

public class PaymentError extends RuntimeException {
    public PaymentError(String message) {
        super(message);
    }
}
