package xyz.gianlu.librespot.api;

import com.stijndewitt.undertow.cors.AllowAll;
import com.stijndewitt.undertow.cors.Filter;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import xyz.gianlu.librespot.api.handlers.EventsHandler;
import xyz.gianlu.librespot.api.handlers.MetadataHandler;
import xyz.gianlu.librespot.api.handlers.PlayerHandler;
import xyz.gianlu.librespot.core.Session;

public class ApiServer {
    private static final Logger LOGGER = Logger.getLogger(ApiServer.class);
    private final int port;
    private Undertow undertow = null;

    public ApiServer(int port) {
        this.port = port;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (undertow != null) undertow.stop();
        }));
    }

    private static void prepareHandlers(@NotNull RoutingHandler root, @NotNull Session session) {
        root.post("/player/{cmd}", new PlayerHandler(session))
                .post("/metadata/{type}/{uri}", new MetadataHandler(session))
                .get("/events", new EventsHandler(session));
    }

    public void start(@NotNull Session session) {
        RoutingHandler handler = new RoutingHandler();
        prepareHandlers(handler, session);

        Filter corsFilter = new Filter(handler);
        corsFilter.setPolicyClass(AllowAll.class.getCanonicalName());
        corsFilter.setPolicyParam(null);
        corsFilter.setUrlPattern(".*");

        undertow = Undertow.builder().addHttpListener(port, "", corsFilter).build();
        undertow.start();
        LOGGER.info(String.format("Server started on port %d!", port));
    }

    public void stop() {
        if (undertow != null) {
            undertow.stop();
            undertow = null;
        }

        LOGGER.info("Server stopped!");
    }

    public void restart(@NotNull Session session) {
        stop();
        start(session);
    }
}
