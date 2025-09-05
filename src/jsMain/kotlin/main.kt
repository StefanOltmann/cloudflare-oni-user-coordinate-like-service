/*
 * Cloudflare ONI User Coordinate Like Service
 * Copyright (C) 2025 Stefan Oltmann
 * https://github.com/StefanOltmann/cloudflare-oni-user-coordinate-like-service
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import org.w3c.fetch.Headers
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import util.appendCorsOptions
import util.await
import util.getValidSteamId
import util.runSuspend
import kotlin.js.Promise

/**
 * Main code for the worker.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun handleRequest(request: Request, env: Env, ctx: dynamic): Promise<Response> =
    runSuspend { handleRequestAsync(request, env, ctx) }

suspend fun handleRequestAsync(request: Request, env: Env, ctx: dynamic): Response {

    /*
     * Handle CORS preflight requests.
     */
    if (request.method == "OPTIONS")
        return createDefaultResponse(204, "No Content")

    /*
     * We need a token or the request is unauthorized.
     */
    val token = request.headers.get("token")
        ?: return createDefaultResponse(401, "Missing header 'token'")

    /*
     * We need a valid Steam ID hash from the token, or the request is unauthorized.
     */
    val steamId: Long = getValidSteamId(token)
        ?: return createDefaultResponse(401, "Invalid token")

    /* Convert Long -> String so D1 accepts it */
    val steamIdStr = steamId.toString()

    /**
     * GET = Get the likes of the user
     */
    if (request.method == "GET") {

        /* Query the database for the likes */
        val result = env.db.prepare("SELECT coordinate FROM likes WHERE steam_id = ?")
            .bind(steamIdStr)
            .run()
            .await()

        val likes = result.results.map { it.coordinate }

        return createDefaultResponse(
            code = 200,
            text = "OK",
            body = JSON.stringify(likes)
        )
    }

    /**
     * PUT = Add a like
     */
    if (request.method == "PUT") {

        val coordinate: String = request.text().await().trim().ifBlank { null }
            ?: return createDefaultResponse(400, "Missing content")

        /*
         * Insert like into the database
         */
        env.db.prepare("INSERT OR IGNORE INTO likes (steam_id, coordinate) VALUES (?, ?)")
            .bind(steamIdStr, coordinate)
            .run()
            .await()

        return createDefaultResponse(200, "Added like for $coordinate")
    }

    /**
     * DELETE = Remove a like
     */
    if (request.method == "DELETE") {

        val coordinate: String = request.text().await().trim().ifBlank { null }
            ?: return createDefaultResponse(400, "Missing content")

        /* Delete the like from the database */
        env.db.prepare("DELETE FROM likes WHERE steam_id = ? AND coordinate = ?")
            .bind(steamIdStr, coordinate)
            .run()
            .await()

        return createDefaultResponse(200, "Removed like for $coordinate")
    }

    /*
     * Method is not supported.
     */
    return createDefaultResponse(405, "Method Not Allowed")
}

private fun createDefaultResponse(
    code: Short,
    text: String,
    body: dynamic? = null,
) = Response(
    body = body,
    init = ResponseInit(
        status = code,
        statusText = text,
        headers = Headers().apply {
            appendCorsOptions()
        }
    )
)
