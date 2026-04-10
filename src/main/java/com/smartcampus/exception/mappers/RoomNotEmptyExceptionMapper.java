package com.smartcampus.exception.mappers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class RoomNotEmptyExceptionMapper
        implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException e) {
        return Response.status(409)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "error", "Conflict",
                        "message", e.getMessage(),
                        "status", 409
                ))
                .build();
    }
}