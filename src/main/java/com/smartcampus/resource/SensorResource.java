package com.smartcampus.resource;

import com.smartcampus.exception.mappers.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/sensors")
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(store.getSensors().values());
        if (type != null && !type.isEmpty()) {
            sensors.removeIf(s -> !s.getType().equalsIgnoreCase(type));
        }
        return Response.ok(sensors).build();
    }

    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Sensor not found: " + sensorId);
            error.put("status", 404);
            return Response.status(404)
                    .entity(error)
                    .build();
        }
        return Response.ok(sensor).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Sensor ID is required");
            error.put("status", 400);
            return Response.status(400)
                    .entity(error)
                    .build();
        }
        if (!store.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Room not found: " + sensor.getRoomId()
            );
        }
        store.getSensors().put(sensor.getId(), sensor);
        store.getRooms().get(sensor.getRoomId())
                .getSensorIds().add(sensor.getId());
        store.getReadings().put(sensor.getId(), new ArrayList<>());
        return Response.status(201).entity(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(
            @PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}