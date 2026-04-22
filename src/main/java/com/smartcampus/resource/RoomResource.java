package com.smartcampus.resource;

import com.smartcampus.exception.mappers.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/rooms")
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // Part 2.1: GET /api/v1/rooms - List all rooms [cite: 114]
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(store.getRooms().values());
        return Response.ok(rooms).build();
    }

    // Part 2.1: POST /api/v1/rooms - Create a new room [cite: 115]
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(400)
                    .entity(Map.of("error", "Room ID is required"))
                    .build();
        }
        if (store.getRooms().containsKey(room.getId())) {
            return Response.status(409)
                    .entity(Map.of("error", "Room already exists"))
                    .build();
        }
        store.getRooms().put(room.getId(), room);
        return Response.status(201).entity(room).build();
    }

    // Part 2.1: GET /api/v1/rooms/{roomId} - Fetch specific room metadata [cite: 116]
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity(Map.of("error", "Room not found: " + roomId))
                    .build();
        }
        return Response.ok(room).build();
    }

    // Part 2.2: DELETE /api/v1/rooms/{roomId} - Room decommissioning 
    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        
        if (room == null) {
            return Response.status(404)
                    .entity(Map.of("error", "Room not found: " + roomId))
                    .build();
        }

        // Business Logic Constraint: Block deletion if room has sensors [cite: 121, 150]
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room " + roomId + " has active sensors and cannot be deleted."
            );
        }
        
        store.getRooms().remove(roomId);
        return Response.ok(Map.of("message", "Room deleted successfully")).build();
    }

    /**
     * Part 5.4: Test Endpoint for Global Safety Net (500 Error) [cite: 160, 161]
     * This endpoint intentionally throws an exception to prove your 
     * GlobalExceptionMapper intercepts unexpected errors.
     */
    @GET
    @Path("/debug/trigger-error")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerInternalError() {
        throw new RuntimeException("Simulated unexpected server failure");
    }
}