package io.suprgames.dynamodb

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import kotlin.test.assertTrue

class LocalDynamoDbTest {

    companion object {

        val localDynamoDb = LocalDynamoDb()

        @BeforeClass
        @JvmStatic
        fun `Initialise DB`() {
            localDynamoDb.start()
        }

        @AfterClass
        @JvmStatic
        fun `Teardown DB`() {
            localDynamoDb.stop()
        }
    }

    private fun tableRequest(tableName: String) = CreateTableRequest
            .builder()
            .tableName(tableName)
            .attributeDefinitions(listOf(AttributeDefinition.builder().attributeName("id").attributeType("S").build(), AttributeDefinition.builder().attributeName("range").attributeType("S").build()))
            .keySchema(listOf(KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build(), KeySchemaElement.builder().attributeName("range").keyType(KeyType.RANGE).build()))
            .billingMode(BillingMode.PAY_PER_REQUEST)
            .build()

    @Test
    fun `Test DynamoDb Sync Client`() {
        val client: DynamoDbClient = localDynamoDb.createClient()
        client.createTable(tableRequest("TestSyncTable"))
        assertTrue(client.listTables().tableNames().contains("TestSyncTable"))
    }

    @Test
    fun `Test DynamoDb Async Client`() {
        val client: DynamoDbAsyncClient = localDynamoDb.createAsyncClient()
        client.createTable(tableRequest("TestAsyncTable")).join()
        assertTrue(client.listTables().join().tableNames().contains("TestAsyncTable"))
    }

}