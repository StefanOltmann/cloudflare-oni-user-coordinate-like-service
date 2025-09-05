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
package util

import kotlin.coroutines.*
import kotlin.js.Promise

suspend fun <T> Promise<T>.await(): T =
    suspendCoroutine { continuation ->
        then(
            onFulfilled = { value -> continuation.resume(value) },
            onRejected = { error -> continuation.resumeWithException(Throwable(error.toString())) }
        )
    }

fun <T> runSuspend(block: suspend () -> T): Promise<T> =
    Promise { resolve, reject ->
        block.startCoroutine(
            completion = object : Continuation<T> {

                override val context: CoroutineContext = EmptyCoroutineContext

                override fun resumeWith(result: Result<T>) {
                    result.fold(resolve, reject)
                }
            }
        )
    }
