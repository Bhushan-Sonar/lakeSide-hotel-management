package com.dailycodework.lakeSidehotel.exception;

public class PhotoRetrievalException extends RuntimeException {
    public PhotoRetrievalException(String message) {
        super(message);
    }

    public PhotoRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}

