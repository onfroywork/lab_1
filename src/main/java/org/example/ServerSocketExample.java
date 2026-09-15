package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Простий TCP-сервер, який слухає порт 8080 і відповідає на HTTP GET-запити.
 */
public class ServerSocketExample {

    // Форматер для виведення часу в логах
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            log("Сервер запущено на порту 8080. Очікування підключень...");

            // Нескінченний цикл очікування нових клієнтів
            while (true) {
                // accept() чекає, доки клієнт не під'єднається
                Socket socket = serverSocket.accept();
                log("Клієнт під'єднався: " + socket.getRemoteSocketAddress());

                try (
                        BufferedReader input = new BufferedReader(
                                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                        PrintWriter output = new PrintWriter(socket.getOutputStream(), false, StandardCharsets.UTF_8)
                ) {
                    // Перший рядок запиту, наприклад: GET / HTTP/1.1
                    String firstLine = input.readLine();
                    if (firstLine == null) {
                        continue; // браузер відкрив з'єднання, але нічого не надіслав
                    }

                    log("Отримано дані від клієнта:");
                    log("  > " + firstLine);

                    // Читаємо заголовки до порожнього рядка
                    String line;
                    while ((line = input.readLine()) != null && !line.isEmpty()) {
                        log("  > " + line);
                    }

                    // Шлях запиту — друге слово першого рядка
                    String path = firstLine.split(" ")[1];

                    // Формування HTTP-відповіді з кодом 200 OK
                    output.println("HTTP/1.1 200 OK");
                    output.println("Content-Type: text/html; charset=utf-8");
                    output.println(); // порожній рядок — кінець заголовків
                    output.println("<p>Hello from your Java Simple Socket Server!</p>");
                    output.println("<p>Запитаний шлях: " + path + "</p>");
                    output.println("<p>Час сервера: " + LocalDateTime.now().format(TIME_FORMATTER) + "</p>");
                    output.flush();

                    log("Відповідь надіслано клієнту. З'єднання закрито.");
                } catch (Exception ex) {
                    log("Помилка обробки клієнта: " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void log(String message) {
        System.out.println("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] " + message);
    }
}
