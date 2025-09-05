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
import kotlin.js.Promise

/**
 * The environment variables that are available to the worker.
 */
external interface Env {
    val db: D1Database
}

/**
 * The database connection.
 */
external interface D1Database {

    /** Prepares a statement. */
    fun prepare(query: String): D1PreparedStatement
}

external interface D1PreparedStatement {

    /**
     * Binds a parameter to the prepared statement.
     */
    fun bind(vararg values: String): D1PreparedStatement

    /**
     * Runs the prepared query (or queries) and returns results.
     * The returned results include metadata.
     */
    fun run(): Promise<D1Result>
}

/**
 * The result of a D1 query.
 */
external interface D1Result {
    val results: Array<Row>
}

/**
 * A single row of a D1 query.
 */
external interface Row {
    val coordinate: String
}
