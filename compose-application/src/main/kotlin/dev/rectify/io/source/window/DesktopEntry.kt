package dev.rectify.io.source.window

import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import data.handoff.api.source.handoff.AnnotationHandoff
import data.mcp.impl.source.server.McpEndpoint
import data.mcp.impl.source.server.startMcpEndpoint
import dev.rectify.io.core.version.RECTIFY_VERSION
import org.koin.mp.KoinPlatform

private val log = Logger.withTag("DesktopEntry")

/**
 * Launches the desktop application window.
 *
 * Called from the `:desktop-application` module's `main()` entry point.
 *
 * Renders [RectifyWindow] directly: the Rectify shell *is* the application root. There is no
 * navigation host and no `App()` composable — both went with the navigation modules.
 *
 * Also owns the MCP endpoint's lifetime. `application` blocks until the last window closes, so
 * the endpoint starts before the first frame and stops after the last one — the agent can reach
 * Rectify for exactly as long as Rectify is open, which is the whole bargain of serving the
 * protocol in-process instead of spawning a second one.
 *
 * @see RectifyWindow
 */
public fun runDesktopApp() {
    val endpoint = openMcpEndpoint()
    try {
        application {
            RectifyWindow(
                mcpUrl = endpoint?.url,
                mcpRegisterCommand = endpoint?.registerCommand,
            )
        }
    } finally {
        endpoint?.stop()
    }
}

/**
 * The endpoint, or `null` if it could not bind.
 *
 * A port it cannot have is a degraded app, not a dead one: everything except the handoff still
 * works, and the status bar says so. Refusing to open the window over it would be the harsher
 * failure by far.
 */
private fun openMcpEndpoint(): McpEndpoint? = runCatching {
    startMcpEndpoint(
        handoff = KoinPlatform.getKoin().get<AnnotationHandoff>(),
        serverVersion = RECTIFY_VERSION,
    )
}.onFailure {
    log.w(it) { "MCP endpoint could not start — Claude Code will not be able to reach Rectify." }
}.getOrNull()
