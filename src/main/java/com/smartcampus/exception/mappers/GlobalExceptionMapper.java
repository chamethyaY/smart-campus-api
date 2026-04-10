package com.smartcampus.exception.mappers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper
        implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER =
            Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        LOGGER.severe("Unexpected error: " + e.getMessage());
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "error", "Internal Server Error",
                        "message", "An unexpected error occurred.",
                        "status", 500
                ))
                .build();
    }
}