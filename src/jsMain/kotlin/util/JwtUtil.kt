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

import js.array.jsArrayOf
import js.buffer.ArrayBuffer
import js.json.parse
import js.objects.unsafeJso
import js.typedarrays.Uint8Array
import js.typedarrays.toByteArray
import js.typedarrays.toUint8Array
import web.crypto.*
import web.encoding.TextEncoder
import kotlin.io.encoding.Base64

private const val JWT_PUBLIC_KEY =
    "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEBHeRvXUxh4O12jjfoGNN/naxqfXboyYY7Ma+pkALk2hk9PYPhVoHk5Ar03k94kyhE9v0i1AEVLXN9WuSqE5+eA=="

/**
 * JWTs are encoded Base64 URL-safe without padding.
 */
private val base64jwt = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

private val textEncoder = TextEncoder()

private fun utf8Decode(uint8arr: Uint8Array<*>): String =
    uint8arr.toByteArray().decodeToString()

private fun base64urlToUint8Array(base64url: String): Uint8Array<ArrayBuffer> =
    base64jwt.decode(base64url).toUint8Array()

private fun base64ToUint8Array(base64: String): Uint8Array<ArrayBuffer> =
    Base64.decode(base64).toUint8Array()

private external interface HeaderJson {
    val alg: String
}

private external interface Payload {
    val sub: String
}

suspend fun getValidSteamId(
    token: String
): Long? {

    val parts = token.split('.')

    /*
     * Exclude malformed tokens.
     */
    if (parts.size != 3)
        return null

    val (headerBase64, payloadBase64, signatureBase64) = parts

    val headerJson = parse<HeaderJson>(
        text = utf8Decode(
            uint8arr = base64urlToUint8Array(headerBase64)
        )
    )

    if (headerJson.alg != "ES256")
        return null

    val data = textEncoder.encode("$headerBase64.$payloadBase64")

    val signature = base64urlToUint8Array(signatureBase64)

    val keyData = base64ToUint8Array(JWT_PUBLIC_KEY)

    val cryptoKey: CryptoKey = crypto.subtle.importKey(
        format = KeyFormat.spki,
        keyData = keyData,
        algorithm = unsafeJso<EcKeyImportParams> {
            name = "ECDSA"
            namedCurve = "P-256"
        },
        extractable = false,
        keyUsages = jsArrayOf(KeyUsage.verify),
    )

    val verified: Boolean = crypto.subtle.verify(
        algorithm = unsafeJso<EcdsaParams> {
            name = "ECDSA"
            hash = "SHA-256"
        },
        key = cryptoKey,
        signature = signature,
        data = data
    )

    if (!verified)
        return null

    val result: Payload =
        parse(utf8Decode(base64urlToUint8Array(payloadBase64)))

    return result.sub.toLong()
}
