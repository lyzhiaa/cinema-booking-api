package kh.edu.istad.moviebooking.exception;

import java.util.Objects;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(
            String resource,
            String field,
            Object value
    ){
        super(
                "%s not found with %s: %s"
                        .formatted(resource, field, value)
        );
    }
}
