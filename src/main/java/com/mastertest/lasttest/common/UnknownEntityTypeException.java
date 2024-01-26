package com.mastertest.lasttest.common;

public class UnknownEntityTypeException extends RuntimeException{

    public UnknownEntityTypeException(String message){
        super(message);
    }

    public UnknownEntityTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
