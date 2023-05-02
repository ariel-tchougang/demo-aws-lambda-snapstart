package com.atn.digital.productapi;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductApiFunctionTest {

    private final Gson gson = new Gson();

    @Mock
    private Context context;

    @Mock
    private AmazonDynamoDB amazonDynamoDB;

    @Mock
    private Table table;

    private ProductApiFunction handler;

    @BeforeEach
    void setUp() {
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB) {
            @Override
            public Table getTable(String tableName) {
                return table;
            }
        };
        handler = new ProductApiFunction(amazonDynamoDB, dynamoDB);
    }

    @Nested
    class GetWithId {
        @Test
        void shouldReturnHttpStatus200WhenIdExists() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("GET");
            Map<String, String> pathParameters = new HashMap<>();
            String id = UUID.randomUUID().toString();
            pathParameters.put("id", id);
            requestEvent.setPathParameters(pathParameters);

            GetItemResult getItemResult = new GetItemResult();
            getItemResult.addItemEntry("id", new AttributeValue(id));
            getItemResult.addItemEntry("name", new AttributeValue("Test Product"));
            when(amazonDynamoDB.getItem(any(GetItemRequest.class))).thenReturn(getItemResult);
            when(context.getLogger()).thenReturn(new TestLogger());
            APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, context);

            assertEquals(200, response.getStatusCode());
            verify(amazonDynamoDB, times(1)).getItem(any(GetItemRequest.class));
        }

        @Test
        void shouldReturnHttpStatus404WhenIdDoesNotExist() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("GET");
            Map<String, String> pathParameters = new HashMap<>();
            String id = UUID.randomUUID().toString();
            pathParameters.put("id", id);
            requestEvent.setPathParameters(pathParameters);

            GetItemResult getItemResult = new GetItemResult();
            getItemResult.setItem(null);
            when(amazonDynamoDB.getItem(any(GetItemRequest.class))).thenReturn(getItemResult);
            when(context.getLogger()).thenReturn(new TestLogger());
            APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, context);

            assertEquals(404, response.getStatusCode());
            verify(amazonDynamoDB, times(1)).getItem(any(GetItemRequest.class));
        }
    }

    @Nested
    class GetWithoutId {
        @Test
        void shouldReturnHttpStatus200WhenThereAreNoProducts() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("GET");

            ScanResult scanResult = new ScanResult();
            when(amazonDynamoDB.scan(any(ScanRequest.class))).thenReturn(scanResult);
            when(context.getLogger()).thenReturn(new TestLogger());
            APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, context);

            assertEquals(200, response.getStatusCode());
            verify(amazonDynamoDB, times(1)).scan(any(ScanRequest.class));
        }

        @Test
        void shouldReturnAllProducts() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("GET");
            ScanResult scanResult = new ScanResult();
            Map<String, AttributeValue> attributes = new HashMap<>();
            attributes.put("id", new AttributeValue(UUID.randomUUID().toString()));
            attributes.put("name", new AttributeValue("Test Product"));
            List<Map<String, AttributeValue>> results = List.of(attributes);
            scanResult.setItems(results);

            when(amazonDynamoDB.scan(any(ScanRequest.class))).thenReturn(scanResult);
            when(context.getLogger()).thenReturn(new TestLogger());
            APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, context);

            assertEquals(200, response.getStatusCode());
            verify(amazonDynamoDB, times(1)).scan(any(ScanRequest.class));
        }
    }

    @Nested
    class Post {
        @Test
        void shouldReturnHttpStatus201() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("POST");
            Map<String, String> body = new HashMap<>();
            body.put("name", "Test Product");
            requestEvent.setBody(gson.toJson(body));

            when(table.putItem(any(Item.class))).thenReturn(new PutItemOutcome(new PutItemResult()));
            when(context.getLogger()).thenReturn(new TestLogger());
            APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, context);

            assertEquals(201, response.getStatusCode());
            verify(table, times(1)).putItem(any(Item.class));
        }

        @Test
        void shouldReturnHttpStatus500() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("POST");
            Map<String, String> body = new HashMap<>();
            body.put("name", "Test Product");
            requestEvent.setBody(gson.toJson(body));

            when(table.putItem(any(Item.class))).thenThrow(new RuntimeException());
            when(context.getLogger()).thenReturn(new TestLogger());
            APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, context);

            assertEquals(500, response.getStatusCode());
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldReturnHttpStatus200() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("DELETE");
            Map<String, String> pathParameters = new HashMap<>();
            String id = UUID.randomUUID().toString();
            pathParameters.put("id", id);
            requestEvent.setPathParameters(pathParameters);

            DeleteItemResult deleteItemResult = new DeleteItemResult();
            when(amazonDynamoDB.deleteItem(any(DeleteItemRequest.class))).thenReturn(deleteItemResult);
            when(context.getLogger()).thenReturn(new TestLogger());
            APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, context);

            assertEquals(200, response.getStatusCode());
            verify(amazonDynamoDB, times(1)).deleteItem(any(DeleteItemRequest.class));
        }
    }

    @Nested
    class OtherMethods {
        @Test
        void shouldReturnHttpStatus405() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("PATCH");
            when(context.getLogger()).thenReturn(new TestLogger());
            APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, context);

            assertEquals(405, response.getStatusCode());
        }
    }
}
