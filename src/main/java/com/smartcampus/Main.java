package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import java.net.URI;

public class Main {

    // Added /api/v1/ to the BASE_URI so your paths match Postman
    public static final String BASE_URI = "http://0.0.0.0:8082/api/v1/";

    public static void main(String[] args) throws Exception {
        // ResourceConfig scans your package for @Path and @Provider classes
        final ResourceConfig rc = new ResourceConfig()
                .packages("com.smartcampus")
                .register(JacksonFeature.class);

        // This starts the Grizzly server at the BASE_URI
        final HttpServer server = GrizzlyHttpServerFactory
                .createHttpServer(URI.create(BASE_URI), rc);

        System.out.println("----------------------------------------------");
        System.out.println("Smart Campus API started!");
        System.out.println("Access the API at: http://localhost:8082/api/v1/");
        System.out.println("Test Rooms at: http://localhost:8082/api/v1/rooms");
        System.out.println("Press ENTER to stop the server...");
        System.out.println("----------------------------------------------");

        System.in.read();
        server.shutdownNow();
    }
}