package com.courses.api;

import com.courses.api.config.DatabaseConfig;
import com.courses.api.handler.CourseHandler;
import com.courses.api.repository.CourseRepository;
import com.courses.api.service.CourseService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        Path configPath = Path.of("config", "db.properties");
        DatabaseConfig databaseConfig = new DatabaseConfig(configPath);
        CourseRepository courseRepository = new CourseRepository(databaseConfig);
        CourseService courseService = new CourseService(courseRepository);
        CourseHandler courseHandler = new CourseHandler(courseService);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/v1/courses", courseHandler);

        ExecutorService executor = Executors.newFixedThreadPool(8);
        server.setExecutor(executor);
        server.start();

        System.out.println("Mini Courses API started on http://localhost:" + PORT);
        System.out.println("Base path: /api/v1/courses");
        System.out.println("Press Ctrl+C to stop.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            server.stop(0);
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("Server stopped.");
        }));
    }
}
