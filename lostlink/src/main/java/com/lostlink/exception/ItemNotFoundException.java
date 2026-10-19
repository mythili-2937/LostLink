
package com.lostlink.exception;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Long id) {
        super("Item not found with ID: " + id);
    }
}
