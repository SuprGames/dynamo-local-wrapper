# DynamoDB Local Wrapper

The DynamoDB Local Wrapper is an utility dependency to run DynamoDB locally and easily for your testing. It provides a AWS v2 DynamoDB Server/Clients


## How to use it

The main reason of the library is to provide a DynamoDB that can be used and integrated in Unit Testing.

Kotlin Code for main Configuration

### Quickstart

```
	val localDynamoDb: LocalDynamoDb = LocalDynamoDb()

	// Start the service running locally on host
	localDynamoDb.start();

	dynamoDbClient: DynamoDbClient = localDynamoDb.createClient();

	// Do whatever you want here
	// ...

	// Stop the service and free up resources
	localDynamoDb.stop();    
```

### Import it!!!!

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

```
dependencies {
	        implementation 'io.suprgames:dynamo-local-wrapper:v0.1.0'
}
```

[![](https://jitpack.io/v/io.suprgames/dynamo-local-wrapper.svg)](https://jitpack.io/#io.suprgames/dynamo-local-wrapper)

### Set up the Test Class

```
class TestClass {

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
    
}
```

1) In the static section of our class we declare the Local DynamoDb server.

2) We need to create a `BeforeClass` method that will START our server when the testing in the class starts. 
* This method need to be JvmStatic annotated because of the nature of the LocalDynamoDb server, specially because of the initialisation of the SqlLite, that is what internally is providing the libraries for our LocalDynamoDb to work.

3) We need to also create a `AfterClass` method that will STOP our server when the testing in the class is done.

### Using the Server to test


The creation of the DynamoDb client it is straight forward:

```
class TestClass {

    //Our companion object
    companion object {
        //...
    }

    // Creation of an AsynClient
    val dynamoDbClient: DynamoDbAsyncClient = localDynamoDb.createAsyncClient()


    // Creation of a SyncClient
    val dynamoDbClient: DynamoDbClient = localDynamoDb.createClient()

}
```

### Considerations

* Each instance of the LocalDynamoDb server will find a new port to run at the very beginning, for this reason, running more than one instance simultaneosly is safe.

* No data will be persisted between Stops and Starts, the storage medium is ephemeral.

* It's recommended to keep a single running instance for all your tests, as it can be slow to teardown and create new servers for every test, but there have been observed problems  when dropping tables between tests for this scenario, so it's best to write your tests to be resilient to tables that already have data in them.

* Only AWS v2 is supported at the moment, and there is no plans of implementing v1


So now... everything is ready!!! Happy Code and Happy Testing!!!!!

