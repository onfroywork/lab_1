package org.example.HttpServerCrud;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Завдання 2. CRUD-сервер на основі вбудованого HttpServer.
 * Ендпоїнти: GET/POST /api/items, GET/PUT/DELETE /api/items/{id}
 */
public class CrudServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/items", new CrudHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        System.out.println("Starting server on port: " + port);
        server.start();
    }
}
