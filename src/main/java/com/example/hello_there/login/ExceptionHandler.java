package com.example.hello_there.login;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response MissingRequestHeaderException(Exception e) {
        e.printStackTrace();
        return new Response("400", "MissingRequestHeaderException");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UnsupportedJwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response UnsupportedJwtException(Exception e) {
        e.printStackTrace();
        return new Response("401", "UnsupportedJwtException");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MalformedJwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response MalformedJwtException(Exception e) {
        e.printStackTrace();
        return new Response("402", "MalformedJwtException");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response ExpiredJwtException(Exception e) {
        e.printStackTrace();
        return new Response("403", "ExpiredJwtException");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(SignatureException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response SignatureException(Exception e) {
        e.printStackTrace();
        return new Response("404", "SignatureException");
    }

    //Response DTO
    @Data
    @AllArgsConstructor
    static class Response {
        private String code;
        private String msg;
    }
}

