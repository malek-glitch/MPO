package tn.pcecom.service

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.webp.WebpWriter
import io.minio.*
import org.slf4j.LoggerFactory
import tn.pcecom.config.MinioConfig
import java.io.ByteArrayInputStream
import java.util.UUID

/**
 * Stores images in MinIO (or any S3-compatible storage).
 * Each upload is saved in three WebP sizes: <base>-thumb.webp, <base>-md.webp, <base>-lg.webp.
 * The database only keeps <base>. All methods are blocking: call them on Dispatchers.IO.
 */
class PhotoStorage(private val cfg: MinioConfig) {
    private val log = LoggerFactory.getLogger(PhotoStorage::class.java)

    private val client: MinioClient = MinioClient.builder()
        .endpoint(cfg.endpoint)
        .credentials(cfg.accessKey, cfg.secretKey)
        .build()

    private val sizes = linkedMapOf("thumb" to 320, "md" to 800, "lg" to 1600)

    init {
        ensureBucket()
    }

    private fun ensureBucket() {
        val exists = client.bucketExists(BucketExistsArgs.builder().bucket(cfg.bucket).build())
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(cfg.bucket).build())
            log.info("Created bucket {}", cfg.bucket)
        }
        // Photos are public: allow anonymous read of objects.
        val policy = """
            {"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"AWS":["*"]},
            "Action":["s3:GetObject"],"Resource":["arn:aws:s3:::${cfg.bucket}/*"]}]}
        """.trimIndent()
        try {
            client.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(cfg.bucket).config(policy).build())
        } catch (e: Exception) {
            log.warn("Could not set public-read policy on bucket {}: {}", cfg.bucket, e.message)
        }
    }

    /** Resizes, converts to WebP, uploads, and returns the base key. */
    fun upload(prefix: String, bytes: ByteArray): String {
        val image = try {
            ImmutableImage.loader().detectOrientation(true).fromBytes(bytes)
        } catch (e: Exception) {
            throw IllegalArgumentException("invalid or unsupported image")
        }
        val base = "$prefix/${UUID.randomUUID()}"
        for ((suffix, max) in sizes) {
            val resized = if (image.width > max || image.height > max) image.max(max, max) else image
            val out = resized.bytes(WebpWriter.DEFAULT.withQ(80))
            client.putObject(
                PutObjectArgs.builder()
                    .bucket(cfg.bucket)
                    .`object`("$base-$suffix.webp")
                    .stream(ByteArrayInputStream(out), out.size.toLong(), -1)
                    .contentType("image/webp")
                    .build()
            )
        }
        return base
    }

    fun delete(base: String) {
        sizes.keys.forEach { suffix ->
            try {
                client.removeObject(
                    RemoveObjectArgs.builder().bucket(cfg.bucket).`object`("$base-$suffix.webp").build()
                )
            } catch (e: Exception) {
                log.warn("Could not delete {}-{}: {}", base, suffix, e.message)
            }
        }
    }

    fun url(base: String, size: String): String = "${cfg.publicUrl.trimEnd('/')}/$base-$size.webp"
}
