package com.mastertest.lasttest.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@RequiredArgsConstructor
public class ExceptionDto {

    private final LocalDateTime timestamp;
    private final String message;

    public ExceptionDto(String message, Clock clock) {
        this.timestamp = LocalDateTime.now(clock.withZone(ZoneId.of("Europe/Warsaw")));
        this.message = message;
    }

    public ExceptionDto(String message) {
        this(message, Clock.system(ZoneId.of("Europe/Warsaw")));
    }
}
