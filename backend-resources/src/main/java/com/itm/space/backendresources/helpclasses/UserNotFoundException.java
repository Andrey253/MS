package com.itm.space.backendresources.helpclasses;

import java.util.UUID;
//@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID userId) {
        super("User not found with ID: " + userId);
    }

    // Или более общий вариант:
    public UserNotFoundException(String message) {
        super(message);
    }
}
