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

import kotlin.io.encoding.Base64
import kotlin.js.Promise

private const val JWT_PUBLIC_KEY =
    "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEBHeRvXUxh4O12jjfoGNN/naxqfXboyYY7Ma+pkALk2hk9PYPhVoHk5Ar03k94kyhE9v0i1AEVLXN9WuSqE5+eA=="

/**
 * JWTs are encoded Base64 URL-safe without padding.
 */
private val base64jwt = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

private val textEncoder = TextEncoder()

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

    val headerJson = JSON.parse(
        utf8Decode(base64urlToUint8Array(headerBase64))
    ).unsafeCast<HeaderJson>()

    if (headerJson.alg != "ES256")
        return null

    val data = textEncoder.encode("$headerBase64.$payloadBase64")

    val signature = base64urlToUint8Array(signatureBase64)

    val keyData = base64ToUint8Array(JWT_PUBLIC_KEY)

    val keyAlgorithm = js("{name: 'ECDSA', namedCurve: 'P-256'}")

    val cryptoKey: CryptoKey = crypto.subtle.importKey(
        format = "spki",
        keyData = keyData,
        algorithm = keyAlgorithm,
        extractable = false,
        keyUsages = arrayOf("verify")
    ).await()

    val verifyAlgorithm = js("{name: 'ECDSA', hash: 'SHA-256'}")

    val verified: Boolean = crypto.subtle.verify(
        algorithm = verifyAlgorithm,
        key = cryptoKey,
        signature = signature,
        data = data
    ).await()

    if (!verified)
        return null

    val result: Payload = JSON.parse(
        utf8Decode(base64urlToUint8Array(payloadBase64))
    ).unsafeCast<Payload>()

    return result.sub.toLong()
}

/*
 * Helper methods
 */

private fun utf8Decode(uint8arr: Uint8Array<ArrayBuffer>): String {
    val bytes = ByteArray(uint8arr.length) { i -> uint8arr[i] }
    return bytes.decodeToString()
}

private fun base64urlToUint8Array(base64url: String): Uint8Array<ArrayBuffer> {

    val bytes = base64jwt.decode(base64url)

    val uint8Array = Uint8Array(bytes.size)

    bytes.forEachIndexed { index, byte ->
        uint8Array[index] = byte
    }

    return uint8Array
}

private fun base64ToUint8Array(base64: String): Uint8Array<ArrayBuffer> {

    val bytes = Base64.decode(base64)

    val uint8Array = Uint8Array(bytes.size)

    bytes.forEachIndexed { index, byte ->
        uint8Array[index] = byte
    }

    return uint8Array
}

/*
 * External interfaces
 */

private external interface ArrayBuffer

private external interface Uint8Array<T> {

    val length: Int

    operator fun get(index: Int): Byte

    operator fun set(index: Int, value: Byte)
}

private external fun Uint8Array(length: Int): Uint8Array<ArrayBuffer>

private external interface TextEncoder {
    fun encode(input: String): Uint8Array<ArrayBuffer>
}

private external fun TextEncoder(): TextEncoder

// Web Crypto API
private external interface Crypto {
    val subtle: SubtleCrypto
}

private external interface SubtleCrypto {

    fun importKey(
        format: String,
        keyData: Uint8Array<ArrayBuffer>,
        algorithm: dynamic,
        extractable: Boolean,
        keyUsages: Array<String>
    ): Promise<CryptoKey>

    fun verify(
        algorithm: dynamic,
        key: CryptoKey,
        signature: Uint8Array<ArrayBuffer>,
        data: Uint8Array<ArrayBuffer>
    ): Promise<Boolean>
}

private external interface CryptoKey

private external object JSON {
    fun parse(text: String): dynamic
}

private external val crypto: Crypto

private external interface HeaderJson {
    val alg: String
}

private external interface Payload {
    val sub: String
}
