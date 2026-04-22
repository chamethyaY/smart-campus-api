package com.smartcampus.exception.mappers;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        // 1. Check if the error is already a standard JAX-RS web exception (like 404, 405, etc.)
        if (e instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) e;
            // Return the existing response instead of turning it into a 500
            return webEx.getResponse();
        }

        // 2. If it's a real unexpected Java crash, log it for the developer
        LOGGER.severe("CRITICAL ERROR: " + e.getMessage());
        e.printStackTrace(); // Useful for debugging in your IntelliJ console

        // 3. Return a clean, "masked" 500 error to the client
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", "The server encountered an unexpected condition."
                ))
                .build();
    }
}