package com.chandsharma.kajugoals.data.sync

import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
private data class DriveFileListResponse(
    val files: List<DriveFileMetadata> = emptyList(),
)

@Serializable
data class DriveFileMetadata(
    val id: String,
    val name: String,
    val modifiedTime: String? = null,
    val size: String? = null,
)

data class DriveSnapshotFile(
    val metadata: DriveFileMetadata,
    val body: String,
    val etag: String?,
)

class DriveApiException(
    val statusCode: Int,
    message: String,
) : Exception(message)

@Singleton
class DriveApi @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    fun listSnapshots(accessToken: String): List<DriveFileMetadata> {
        val query = "name = '$SNAPSHOT_FILE_NAME' and 'appDataFolder' in parents and trashed = false"
        val url = buildString {
            append("https://www.googleapis.com/drive/v3/files")
            append("?spaces=appDataFolder")
            append("&orderBy=modifiedTime%20desc")
            append("&pageSize=100")
            append("&fields=files(id,name,modifiedTime,size)")
            append("&q=")
            append(URLEncoder.encode(query, StandardCharsets.UTF_8.name()))
        }
        val response = request("GET", url, accessToken)
        return json.decodeFromString<DriveFileListResponse>(response.body).files
    }

    fun downloadSnapshot(accessToken: String, metadata: DriveFileMetadata): DriveSnapshotFile {
        val response = request(
            method = "GET",
            url = "https://www.googleapis.com/drive/v3/files/${metadata.id}?alt=media",
            accessToken = accessToken,
        )
        return DriveSnapshotFile(
            metadata = metadata,
            body = response.body,
            etag = response.etag,
        )
    }

    fun createSnapshot(accessToken: String, body: String): DriveFileMetadata {
        val boundary = "kaju-${UUID.randomUUID()}"
        val metadata = """{"name":"$SNAPSHOT_FILE_NAME","parents":["appDataFolder"],"mimeType":"application/json"}"""
        val payload = ByteArrayOutputStream().use { output ->
            output.write("--$boundary\r\n".toByteArray())
            output.write("Content-Type: application/json; charset=UTF-8\r\n\r\n".toByteArray())
            output.write(metadata.toByteArray())
            output.write("\r\n--$boundary\r\n".toByteArray())
            output.write("Content-Type: application/json\r\n\r\n".toByteArray())
            output.write(body.toByteArray())
            output.write("\r\n--$boundary--\r\n".toByteArray())
            output.toByteArray()
        }
        val response = request(
            method = "POST",
            url = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name,modifiedTime,size",
            accessToken = accessToken,
            contentType = "multipart/related; boundary=$boundary",
            payload = payload,
        )
        return json.decodeFromString(response.body)
    }

    fun updateSnapshot(
        accessToken: String,
        file: DriveSnapshotFile,
        body: String,
    ): DriveFileMetadata {
        val response = request(
            method = "PATCH",
            url = "https://www.googleapis.com/upload/drive/v3/files/${file.metadata.id}" +
                "?uploadType=media&fields=id,name,modifiedTime,size",
            accessToken = accessToken,
            contentType = "application/json; charset=UTF-8",
            payload = body.toByteArray(),
            ifMatch = file.etag,
        )
        return json.decodeFromString(response.body)
    }

    private fun request(
        method: String,
        url: String,
        accessToken: String,
        contentType: String? = null,
        payload: ByteArray? = null,
        ifMatch: String? = null,
    ): HttpResponse {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = method
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("Accept", "application/json")
            contentType?.let { connection.setRequestProperty("Content-Type", it) }
            ifMatch?.let { connection.setRequestProperty("If-Match", it) }
            if (payload != null) {
                connection.doOutput = true
                connection.setFixedLengthStreamingMode(payload.size)
                connection.outputStream.use { it.write(payload) }
            }
            val status = connection.responseCode
            val responseBody = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            if (status !in 200..299) {
                throw DriveApiException(status, "Google Drive request failed ($status)")
            }
            HttpResponse(
                body = responseBody,
                etag = connection.getHeaderField("ETag"),
            )
        } finally {
            connection.disconnect()
        }
    }

    private data class HttpResponse(
        val body: String,
        val etag: String?,
    )

    companion object {
        const val SNAPSHOT_FILE_NAME = "kaju_goals_snapshot_v1.json"
    }
}
