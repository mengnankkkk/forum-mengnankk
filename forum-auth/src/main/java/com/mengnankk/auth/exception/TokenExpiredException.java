package com.mengnankk.auth.exception;

/**
 * Token过期异常
 */
public class TokenExpiredException extends AuthException {

    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
