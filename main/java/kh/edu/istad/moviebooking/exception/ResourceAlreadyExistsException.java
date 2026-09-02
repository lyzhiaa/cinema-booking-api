package kh.edu.istad.moviebooking.exception;

public class ResourceAlreadyExistsException
        extends RuntimeException {

    public ResourceAlreadyExistsException(
            String resource,
            String field,
            Object value
    ) {
        super(
                "%s already exists with %s: %s"
                        .formatted(resource, field, value)
        );
    }
}
