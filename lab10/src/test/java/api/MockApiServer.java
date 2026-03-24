package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class MockApiServer {

    private static HttpServer server;

    private MockApiServer() {
    }

    public static synchronized void start(int port) throws IOException {
        if (server != null) {
            return;
        }

        server = HttpServer.create(new InetSocketAddress("localhost", port), 0);

        server.createContext("/api/users", new UsersHandler());
        server.createContext("/api/users/1", exchange -> sendJson(exchange, 200,
                "{\"data\":{\"id\":1,\"email\":\"george@reqres.in\",\"first_name\":\"George\",\"last_name\":\"Bluth\"}}"));
        server.createContext("/api/users/2", new User2Handler());
        server.createContext("/api/users/9999", exchange -> sendJson(exchange, 404, "{}"));
        server.createContext("/api/posts", exchange -> sendJson(exchange, 200,
                "[{\"id\":1,\"title\":\"post-1\"},{\"id\":2,\"title\":\"post-2\"}]"));

        server.start();
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            if ("GET".equalsIgnoreCase(method)) {
                if (query != null && query.contains("page=1")) {
                    sendJson(exchange, 200,
                            "{\"page\":1,\"total_pages\":2,\"data\":[{\"id\":1,\"email\":\"george@reqres.in\",\"first_name\":\"George\",\"last_name\":\"Bluth\",\"avatar\":\"https://reqres.in/img/faces/1-image.jpg\"}]}");
                    return;
                }

                sendJson(exchange, 200,
                        "{\"page\":1,\"total_pages\":2,\"data\":[{\"id\":1},{\"id\":2}]}");
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                sendJson(exchange, 201,
                        "{\"name\":\"Huynh Kom\",\"job\":\"Tester\",\"id\":\"123\",\"createdAt\":\"2026-03-24T00:00:00.000Z\"}");
                return;
            }

            sendJson(exchange, 405, "{\"error\":\"method_not_allowed\"}");
        }
    }

    private static class User2Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                sendJson(exchange, 200,
                        "{\"data\":{\"id\":2,\"email\":\"janet@reqres.in\",\"first_name\":\"Janet\",\"last_name\":\"Weaver\"}}");
                return;
            }

            if ("PUT".equalsIgnoreCase(method)) {
                sendJson(exchange, 200,
                        "{\"name\":\"Huynh Kom\",\"job\":\"Senior Tester\",\"updatedAt\":\"2026-03-24T00:00:00.000Z\"}");
                return;
            }

            if ("PATCH".equalsIgnoreCase(method)) {
                sendJson(exchange, 200,
                        "{\"name\":\"Huynh Kom\",\"job\":\"Lead Tester\",\"updatedAt\":\"2026-03-24T00:00:00.000Z\"}");
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                sendEmpty(exchange, 204);
                return;
            }

            sendJson(exchange, 405, "{\"error\":\"method_not_allowed\"}");
        }
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendEmpty(HttpExchange exchange, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, -1);
        exchange.close();
    }
}
