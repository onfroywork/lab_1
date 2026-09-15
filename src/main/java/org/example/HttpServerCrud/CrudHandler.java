package org.example.HttpServerCrud;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CrudHandler implements HttpHandler {

    // Сховище даних у пам'яті. ConcurrentHashMap — потокобезпечний.
    private final Map<Integer, Item> items = new ConcurrentHashMap<>();
    // AtomicInteger для генерації унікальних ID.
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public CrudHandler() {
        // Початкові дані
        int id1 = idCounter.incrementAndGet();
        items.put(id1, new Item(id1, "First item"));
        int id2 = idCounter.incrementAndGet();
        items.put(id2, new Item(id2, "Second item"));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            final String method = exchange.getRequestMethod();
            switch (method) {
                case "GET"    -> handleGet(exchange);
                case "POST"   -> handlePost(exchange);
                case "PUT"    -> handlePut(exchange);
                case "DELETE" -> handleDelete(exchange);
                default       -> sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error\"}");
        } finally {
            exchange.close();
        }
    }

    // Читання тіла запиту в рядок
    private String parseRequestBody(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    // Відправка HTTP-відповіді
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    // GET /api/items — усі записи, GET /api/items/{id} — один запис
    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length == 4) { // запит одного запису, напр. /api/items/1
            try {
                int id = Integer.parseInt(pathParts[3]);
                Item item = items.get(id);
                if (item != null) {
                    sendResponse(exchange, 200, item.toJson());
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Item not found\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid ID format\"}");
            }
        } else { // запит усіх записів
            String allItemsJson = items.values().stream()
                    .map(Item::toJson)
                    .collect(Collectors.joining(", ", "[", "]"));
            sendResponse(exchange, 200, allItemsJson);
        }
    }


    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = parseRequestBody(exchange.getRequestBody());
        String newData = requestBody
                .split(":")[1]
                .replace("\"", "")
                .replace("}", "")
                .trim();

        int newId = idCounter.incrementAndGet();
        Item newItem = new Item(newId, newData);
        items.put(newId, newItem);

        sendResponse(exchange, 201, newItem.toJson()); // 201 Created
    }

    // PUT /api/items/{id} з тілом {"data": "Updated data"}
    private void handlePut(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length != 4) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid path for PUT request\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathParts[3]);
            if (!items.containsKey(id)) {
                sendResponse(exchange, 404, "{\"error\":\"Item not found\"}");
                return;
            }

            String requestBody = parseRequestBody(exchange.getRequestBody());
            String updatedData = requestBody
                    .split(":")[1]
                    .replace("\"", "")
                    .replace("}", "")
                    .trim();

            Item updatedItem = new Item(id, updatedData);
            items.put(id, updatedItem);

            sendResponse(exchange, 200, updatedItem.toJson());
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid ID format\"}");
        }
    }

    // DELETE /api/items/{id}
    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length != 4) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid path for DELETE request\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathParts[3]);
            Item removedItem = items.remove(id);

            if (removedItem != null) {
                sendResponse(exchange, 200, String.format("{\"message\":\"Item with id %d deleted\"}", id));
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Item not found\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid ID format\"}");
        }
    }
}
