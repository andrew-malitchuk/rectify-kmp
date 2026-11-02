package data.mcp.impl.source.server

import com.sun.net.httpserver.HttpServer
import data.handoff.api.source.handoff.AnnotationHandoff
import data.mcp.impl.core.http.McpHttpHandler
import data.mcp.impl.core.rpc.McpDispatcher
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.Executors

/** The path the endpoint serves. One endpoint, one path — the Streamable HTTP shape. */
private const val MCP_PATH: String = "/mcp"

/** Where Claude Code looks by default. Arbitrary, but it has to be *stable* to be configurable. */
public const val DEFAULT_MCP_PORT: Int = 7391

/**
 * How many ports past [DEFAULT_MCP_PORT] to try. A second Rectify instance, or anything else
 * squatting the port, must not stop the app from starting — the UI reports the port it actually
 * got, so a moved endpoint is visible rather than silently wrong.
 */
private const val PORT_SCAN_RANGE: Int = 10

/** Threads serving requests. Claude Code talks to this one call at a time; two is slack. */
private const val WORKER_THREADS: Int = 2

/** Seconds to let in-flight exchanges finish when the window closes. */
private const val SHUTDOWN_GRACE_SECONDS: Int = 1

/**
 * A running MCP endpoint on the loopback interface.
 *
 * Rectify serves the protocol itself now; there is no second process to install, and nothing
 * for the user to spawn. The agent reaches it at [url] for as long as the app is open — which
 * is the trade this transport makes against the old stdio server: that one could answer while
 * Rectify was closed, this one cannot.
 */
public class McpEndpoint internal constructor(private val server: HttpServer) {

    /** The port actually bound, which is [DEFAULT_MCP_PORT] unless it was taken. */
    public val port: Int get() = server.address.port

    /** The endpoint URL, as it goes into a Claude Code MCP config entry. */
    public val url: String get() = "http://127.0.0.1:$port$MCP_PATH"

    /** The one-liner that registers this endpoint, ready to paste into a terminal. */
    public val registerCommand: String get() = "claude mcp add --transport http rectify $url"

    /** Stops serving. Safe to call more than once. */
    public fun stop() {
        server.stop(SHUTDOWN_GRACE_SECONDS)
    }
}

/**
 * Binds the MCP endpoint to the loopback interface and starts serving.
 *
 * Binds to [InetAddress.getLoopbackAddress] explicitly rather than the wildcard address: an MCP
 * endpoint on `0.0.0.0` would hand everyone on the café wifi the user's latest screenshots.
 *
 * @param handoff the slot to read annotation batches out of.
 * @param preferredPort the first port to try; see [PORT_SCAN_RANGE] for what happens if it is taken.
 * @throws java.io.IOException if every port in the scan range is taken.
 */
public fun startMcpEndpoint(
    handoff: AnnotationHandoff,
    preferredPort: Int = DEFAULT_MCP_PORT,
): McpEndpoint {
    val loopback = InetAddress.getLoopbackAddress()
    var lastFailure: java.io.IOException? = null
    for (port in preferredPort until preferredPort + PORT_SCAN_RANGE) {
        val server = try {
            HttpServer.create(InetSocketAddress(loopback, port), 0)
        } catch (alreadyBound: java.io.IOException) {
            lastFailure = alreadyBound
            continue
        }
        server.createContext(MCP_PATH, McpHttpHandler(McpDispatcher(handoff)))
        server.executor = Executors.newFixedThreadPool(WORKER_THREADS)
        server.start()
        return McpEndpoint(server)
    }
    throw lastFailure ?: java.io.IOException("No free port in $preferredPort..${preferredPort + PORT_SCAN_RANGE}")
}
