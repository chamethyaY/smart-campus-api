package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.net.URI;

public class Main {

    public static final String BASE_URI = "http://0.0.0.0:8081/";

    public static void main(String[] args) throws Exception {
        final ResourceConfig rc = new ResourceConfig()
                .packages("com.smartcampus")
                .register(JacksonFeature.class);

        final HttpServer server = GrizzlyHttpServerFactory
                .createHttpServer(URI.create(BASE_URI), rc);

        System.out.println("Smart Campus API started!");
        System.out.println("Access at: http://localhost:8080/api/v1");
        System.out.println("Press ENTER to stop the server...");
        System.in.read();
        server.shutdownNow();
    }
}