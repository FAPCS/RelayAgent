package me.fapcs.relay_agent

import io.javalin.Javalin
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.fapcs.shared.config.ConfigurationHandler
import me.fapcs.shared.log.Logger
import org.eclipse.jetty.websocket.api.Session

class JavalinService {

    private val app = Javalin.create()
    private val sessions = mutableMapOf<String, Session>()

    init {
        app.events { events ->
            events.serverStarting { Logger.info("Starting Javalin server...") }
            events.serverStarted { Logger.info("Javalin server started.") }
            events.serverStartFailed { Logger.error("Failed to start Javalin server.") }

            events.serverStopping { Logger.info("Stopping Javalin server...") }
            events.serverStopped { Logger.info("Javalin server stopped.") }
            events.serverStopFailed { Logger.error("Failed to stop Javalin server.") }
        }

        app.ws("/ws") { ws ->
            ws.onConnect { ctx ->
                sessions[ctx.sessionId] = ctx.session
                Logger.info("Client with id ${ctx.sessionId} connected from ${ctx.session.remoteAddress}")
            }

            ws.onMessage { ctx ->
                Logger.info("Received message from ${ctx.sessionId}: ${ctx.message()}")
                val message = ctx.message()

                val json = Json.parseToJsonElement(message).jsonObject
                val packet = json["packet"]?.jsonPrimitive?.content
                if (packet == "KeepAlivePacket") {
                    Logger.info("Received keep alive packet from ${ctx.sessionId}, ignoring...")
                    return@onMessage
                }

                Logger.info("Forwarding message to all clients...")
                sessions.filter { (it, _) -> it != ctx.sessionId }
                    .forEach { (_, session) -> session.remote.sendString(ctx.message()) }
            }

            ws.onClose { ctx ->
                sessions.remove(ctx.sessionId)
                Logger.info("Client with id ${ctx.sessionId} disconnected")
            }

            ws.onError { ctx ->
                Logger.error("Error from ${ctx.sessionId}: ${ctx.error()}")
                ctx.session.close()
            }

        }
    }

    fun start() {
        Logger.info("Starting Javalin service...")

        app.start(
            ConfigurationHandler.get("host", "0.0.0.0", true),
            ConfigurationHandler.get("port", 8080, true)
        )

        Logger.info("Javalin service started.")
    }

    fun stop() {
        Logger.info("Stopping Javalin service...")

        sessions.forEach { (_, session) -> session.close() }
        sessions.clear()

        app.stop()
        Logger.info("Javalin service stopped.")
    }

}