package me.winflix.vitalcore.addons.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.logging.Level;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.managers.ResourcePackManager;

public class HttpServerManager {

    private HttpServer server;
    private final VitalCore plugin;
    private final ResourcePackManager resourcePackManager;
    private final int port;

    public HttpServerManager(VitalCore plugin, ResourcePackManager rpManager) {
        this.plugin = plugin;
        this.resourcePackManager = rpManager;
        this.port = plugin.getConfig().getInt("resourcepack-server-port", 8080);
    }

    public void startServer() {
        try {
            // Crear servidor escuchando en todas las IPs locales (0.0.0.0) y el puerto
            // especificado
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // Crear un contexto para la URL del resource pack (ej:
            // http://localhost:8080/resourcepack.zip)
            server.createContext("/resourcepack.zip", new ResourcePackHandler());

            // Usar el executor por defecto (puedes configurar uno si necesitas más control)
            server.setExecutor(null);

            // Iniciar el servidor
            server.start();
            plugin.getLogger().info("[HttpServer] Servidor HTTP iniciado en el puerto " + port);

        } catch (java.net.BindException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "[HttpServer] Error al iniciar: El puerto " + port + " ya está en uso.", e);
            server = null; // Asegurarse que no intentemos detener un servidor no iniciado
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "[HttpServer] Error al iniciar servidor HTTP", e);
            server = null;
        }
    }

    public void stopServer() {
        if (server != null) {
            // Detener el servidor (el segundo argumento es el delay en segundos para cerrar
            // conexiones)
            server.stop(0);
            plugin.getLogger().info("[HttpServer] Servidor HTTP detenido.");
            server = null;
        }
    }

    // Clase interna para manejar las peticiones a /resourcepack.zip
    class ResourcePackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Solo permitir peticiones GET
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
                return;
            }

            // Obtener la ruta al archivo ZIP generado
            File zipFile = resourcePackManager.getZippedResourcePackFile();


            if (zipFile == null || !zipFile.exists() || !zipFile.isFile()) {
                String response = "404 - Resource Pack Not Found";
                exchange.sendResponseHeaders(404, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                plugin.getLogger().warning("[HttpServer] Petición para resource pack, pero el archivo no existe: "
                        + (zipFile != null ? zipFile.getPath() : "null"));
                return;
            }

            // Preparar la respuesta
            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.sendResponseHeaders(200, zipFile.length()); // 200 OK, con el tamaño del archivo

            // Enviar el archivo
            try (OutputStream os = exchange.getResponseBody()) {
                Files.copy(zipFile.toPath(), os);
            } catch (Exception e) {
                // Es posible que el cliente cierre la conexión antes de tiempo
                plugin.getLogger().log(Level.WARNING,
                        "[HttpServer] Error enviando archivo resource pack (puede ser normal si el cliente desconectó)",
                        e);
            } finally {
                exchange.close(); // Asegurarse de cerrar el exchange
            }
        }
    }

}