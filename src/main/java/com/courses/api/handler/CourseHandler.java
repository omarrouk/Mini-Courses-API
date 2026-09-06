package com.courses.api.handler;

import com.courses.api.dto.ErrorResponse;
import com.courses.api.dto.PageResponse;
import com.courses.api.model.Course;
import com.courses.api.service.CourseService;
import com.courses.api.util.ForbiddenException;
import com.courses.api.util.JsonUtil;
import com.courses.api.util.NotFoundException;
import com.courses.api.util.UnauthorizedException;
import com.courses.api.util.ValidationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CourseHandler implements HttpHandler {
    private static final String BASE_PATH = "/api/v1/courses";

    private final CourseService courseService;

    public CourseHandler(CourseService courseService) {
        this.courseService = courseService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method) && BASE_PATH.equals(path)) {
                handleList(exchange);
                return;
            }

            if ("GET".equals(method) && isCourseIdPath(path)) {
                requireRoles(exchange, "USER");
                handleGetById(exchange, extractId(path));
                return;
            }

            if ("POST".equals(method) && BASE_PATH.equals(path)) {
                requireRoles(exchange, "ADMIN");
                handleCreate(exchange);
                return;
            }

            if ("PUT".equals(method) && isCourseIdPath(path)) {
                requireRoles(exchange, "TEACHER", "ADMIN");
                handleUpdate(exchange, extractId(path));
                return;
            }

            if ("DELETE".equals(method) && isCourseIdPath(path)) {
                requireRoles(exchange, "ADMIN");
                handleDelete(exchange, extractId(path));
                return;
            }

            sendError(exchange, 404, "NOT_FOUND", "Endpoint not found");
        } catch (UnauthorizedException e) {
            sendError(exchange, 401, "UNAUTHORIZED", e.getMessage());
        } catch (ForbiddenException e) {
            sendError(exchange, 403, "FORBIDDEN", e.getMessage());
        } catch (ValidationException e) {
            sendError(exchange, 400, e.getErrorCode(), e.getMessage());
        } catch (NotFoundException e) {
            sendError(exchange, 404, "NOT_FOUND", e.getMessage());
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, "VALIDATION_ERROR", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "INTERNAL_ERROR", "An unexpected error occurred");
        }
    }

    private void handleList(HttpExchange exchange) throws IOException {
        Map<String, String> query = parseQuery(exchange.getRequestURI());
        int page = parseIntOrDefault(query.get("page"), 1, "page");
        int size = parseIntOrDefault(query.get("size"), 10, "size");

        PageResponse response = courseService.listCourses(page, size);
        sendJson(exchange, 200, response);
    }

    private void handleGetById(HttpExchange exchange, long id) throws IOException {
        Course course = courseService.getCourse(id);
        sendJson(exchange, 200, course);
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        Course request = readCourse(exchange);
        Course created = courseService.createCourse(request);

        exchange.getResponseHeaders().add("Location", BASE_PATH + "/" + created.getId());
        sendJson(exchange, 201, created);
    }

    private void handleUpdate(HttpExchange exchange, long id) throws IOException {
        Course request = readCourse(exchange);
        Course updated = courseService.updateCourse(id, request);
        sendJson(exchange, 200, updated);
    }

    private void handleDelete(HttpExchange exchange, long id) throws IOException {
        courseService.deleteCourse(id);
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private Course readCourse(HttpExchange exchange) throws IOException {
        try (InputStream body = exchange.getRequestBody()) {
            byte[] bytes = body.readAllBytes();
            if (bytes.length == 0) {
                throw new ValidationException("request body is required");
            }
            return JsonUtil.fromBytes(bytes, Course.class);
        }
    }

    private void requireRoles(HttpExchange exchange, String... allowedRoles) {
        String role = exchange.getRequestHeaders().getFirst("ROLE");
        if (role == null || role.isBlank()) {
            throw new UnauthorizedException("ROLE header is required");
        }

        String normalized = role.trim().toUpperCase();
        Set<String> knownRoles = new HashSet<>(Arrays.asList("USER", "ADMIN", "TEACHER"));
        Set<String> allowed = new HashSet<>();
        for (String allowedRole : allowedRoles) {
            allowed.add(allowedRole.toUpperCase());
        }

        if (!knownRoles.contains(normalized) || !allowed.contains(normalized)) {
            throw new ForbiddenException("Role is not authorized for this endpoint");
        }
    }

    private boolean isCourseIdPath(String path) {
        if (!path.startsWith(BASE_PATH + "/")) {
            return false;
        }
        String remainder = path.substring((BASE_PATH + "/").length());
        return !remainder.isEmpty() && !remainder.contains("/");
    }

    private long extractId(String path) {
        String idPart = path.substring((BASE_PATH + "/").length());
        try {
            long id = Long.parseLong(idPart);
            if (id <= 0) {
                throw new ValidationException("id must be a positive number");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new ValidationException("id must be a valid number");
        }
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return params;
        }

        for (String pair : query.split("&")) {
            if (pair.isBlank()) {
                continue;
            }
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    private int parseIntOrDefault(String value, int defaultValue, String fieldName) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid integer");
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = JsonUtil.toBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int status, String error, String message) throws IOException {
        sendJson(exchange, status, new ErrorResponse(status, error, message));
    }
}
