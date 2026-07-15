package com.epaper.controller;

import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class LocalHttpServer {
    private final AssetManager assets;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean running;
    private ServerSocket serverSocket;
    private Thread acceptThread;

    LocalHttpServer(AssetManager assets) {
        this.assets = assets;
    }

    synchronized int start() throws IOException {
        if (running && serverSocket != null) return serverSocket.getLocalPort();
        serverSocket = new ServerSocket(0, 16, InetAddress.getByName("127.0.0.1"));
        running = true;
        acceptThread = new Thread(this::acceptLoop, "epaper-local-server");
        acceptThread.start();
        return serverSocket.getLocalPort();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                pool.execute(() -> handle(socket));
            } catch (IOException ignored) {
                if (!running) break;
            }
        }
    }

    private void handle(Socket socket) {
        try (Socket s = socket;
             BufferedInputStream in = new BufferedInputStream(s.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream())) {

            String requestLine = readLine(in);
            if (requestLine == null || requestLine.isEmpty()) return;
            String[] parts = requestLine.split(" ");
            String rawPath = parts.length > 1 ? parts[1] : "/";
            while (true) {
                String line = readLine(in);
                if (line == null || line.isEmpty()) break;
            }

            String path = rawPath.split("\\?", 2)[0];
            if (path.equals("/")) path = "/index.html";
            path = path.replace("..", "");
            if (path.startsWith("/")) path = path.substring(1);

            try (InputStream asset = assets.open(path)) {
                byte[] body = readAll(asset);
                String headers = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mime(path) + "\r\n" +
                        "Content-Length: " + body.length + "\r\n" +
                        "Cache-Control: no-cache\r\n" +
                        "Connection: close\r\n\r\n";
                out.write(headers.getBytes(StandardCharsets.US_ASCII));
                out.write(body);
            } catch (IOException notFound) {
                byte[] body = "Không tìm thấy tài nguyên".getBytes(StandardCharsets.UTF_8);
                String headers = "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Type: text/plain; charset=utf-8\r\n" +
                        "Content-Length: " + body.length + "\r\n" +
                        "Connection: close\r\n\r\n";
                out.write(headers.getBytes(StandardCharsets.US_ASCII));
                out.write(body);
            }
            out.flush();
        } catch (IOException ignored) {
        }
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int previous = -1;
        int current;
        while ((current = in.read()) != -1) {
            if (previous == '\r' && current == '\n') break;
            if (previous != -1) buffer.write(previous);
            previous = current;
        }
        if (current == -1 && previous == -1 && buffer.size() == 0) return null;
        if (previous != -1 && previous != '\r') buffer.write(previous);
        return buffer.toString(StandardCharsets.UTF_8.name());
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }

    private static String mime(String path) {
        String p = path.toLowerCase(Locale.ROOT);
        if (p.endsWith(".html")) return "text/html; charset=utf-8";
        if (p.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (p.endsWith(".css")) return "text/css; charset=utf-8";
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".jpg") || p.endsWith(".jpeg")) return "image/jpeg";
        if (p.endsWith(".webp")) return "image/webp";
        if (p.endsWith(".svg")) return "image/svg+xml";
        if (p.endsWith(".json")) return "application/json; charset=utf-8";
        return "application/octet-stream";
    }

    synchronized void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
        pool.shutdownNow();
    }
}
