package io.suprgames.dynamodb

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer
import org.apache.logging.log4j.LogManager
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.io.IOException
import java.net.ServerSocket
import java.net.URI

/**
 * Wrapper for a local DynamoDb server used in testing. Each instance of this class will find a new port to run on,
 * so multiple instances can be safely run simultaneously. Each instance of this service uses memory as a storage medium
 * and is thus completely ephemeral; no data will be persisted between stops and starts.
 *
 * val localDynamoDb: LocalDynamoDb = LocalDynamoDb()
 * localDynamoDb.start();       // Start the service running locally on host
 * dynamoDbClient: DynamoDbClient = localDynamoDb.createClient();
 * ...      // Do your testing with the client
 * localDynamoDb.stop();        // Stop the service and free up resources
 *
 * If possible it's recommended to keep a single running instance for all your tests, as it can be slow to teardown
 * and create new servers for every test, but there have been observed problems when dropping tables between tests for
 * this scenario, so it's best to write your tests to be resilient to tables that already have data in them.
 */
class LocalDynamoDb {
    private var server: DynamoDBProxyServer? = null
    private var port = 0

    companion object {
        private val logger = LogManager.getLogger(LocalDynamoDb::class.java)

        private fun propagate(e: Exception): RuntimeException {
            if (e is RuntimeException) {
                throw e
            }
            throw RuntimeException(e)
        }
    }

    /**
     * Start the local DynamoDb service and run in background
     */
    fun start() {
        port = freePort
        val portString = port.toString()
        try {
            server = createServer(portString)
            server!!.start()
            logger.info("****************************************************")
            logger.info("           Database started in port $port")
            logger.info("****************************************************")
        } catch (e: Exception) {
            throw propagate(e)
        }
    }

    /**
     * Create a standard AWS v2 SDK client pointing to the local DynamoDb instance
     * @return A DynamoDbClient pointing to the local DynamoDb instance
     */
    fun createClient(): DynamoDbClient? {
        val endpoint = String.format("http://localhost:%d", port)
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpoint)) // The region is meaningless for local DynamoDb but required for client builder validation
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("dummy-key", "dummy-secret")))
                .build()
    }

    fun createAsyncClient(): DynamoDbAsyncClient {
        val endpoint = String.format("http://localhost:%d", port)
        return DynamoDbAsyncClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("dummy-key", "dummy-secret")))
                .build()
    }

    /**
     * Stops the local DynamoDb service and frees up resources it is using.
     */
    fun stop() {
        try {
            server!!.stop()
            logger.info("****************************************************")
            logger.info("               Database stopped")
            logger.info("****************************************************")
        } catch (e: Exception) {
            throw propagate(e)
        }
    }

    @Throws(Exception::class)
    private fun createServer(portString: String): DynamoDBProxyServer {
        return ServerRunner.createServerFromCommandLineArgs(arrayOf(
                "-inMemory",
                "-port", portString
        ))
    }

    private val freePort: Int
        get() = try {
            val socket = ServerSocket(0)
            val port = socket.localPort
            socket.close()
            logger.info("Port assigned: $port")
            port
        } catch (ioe: IOException) {
            throw propagate(ioe)
        }

}
