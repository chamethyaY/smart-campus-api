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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(store.getRooms().values());
        return Response.ok(rooms).build();
    }

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
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room " + roomId + " has active sensors and cannot be deleted."
            );
        }
        store.getRooms().remove(roomId);
        return Response.ok(Map.of("message", "Room deleted successfully")).build();
    }
}