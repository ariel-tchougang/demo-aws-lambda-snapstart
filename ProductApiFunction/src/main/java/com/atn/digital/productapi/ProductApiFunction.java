package com.atn.digital.productapi;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProductApiFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");

    private final AmazonDynamoDB amazonDynamoDB;
    private final Table table;

    private final Gson gson = new Gson();

    public ProductApiFunction() {
        this(AmazonDynamoDBClientBuilder.defaultClient());
    }

    public ProductApiFunction(AmazonDynamoDB amazonDynamoDB) {
        this(amazonDynamoDB, new DynamoDB(amazonDynamoDB));
    }

    public ProductApiFunction(AmazonDynamoDB amazonDynamoDB, DynamoDB dynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.table = dynamoDB.getTable(TABLE_NAME);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        String method = event.getHttpMethod().toUpperCase();
        context.getLogger().log("httpMethod = " + method);

        switch (method) {
            case "GET": return handleGet(event);
            case "POST": return handlePost(event, context);
            case "DELETE": return handleDelete(event, context);
            default: {
                APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
                response.setStatusCode(405);
                return response;
            }
        }
    }

    private APIGatewayProxyResponseEvent handleDelete(APIGatewayProxyRequestEvent event, Context context) {
        String id = event.getPathParameters().get("id");
        context.getLogger().log("deleting " + id);
        DeleteItemRequest request = new DeleteItemRequest()
                .withTableName(TABLE_NAME)
                .withKey(Map.of("id", new AttributeValue(id)));
        amazonDynamoDB.deleteItem(request);
        return buildProxyResponseEvent(null, 200);
    }

    private APIGatewayProxyResponseEvent handlePost(APIGatewayProxyRequestEvent event, Context context) {

        try {
            Product product = gson.fromJson(event.getBody(), Product.class);
            context.getLogger().log("name = " + product.name);
            Item item = new Item()
                    .withPrimaryKey("id", UUID.randomUUID().toString())
                    .withString("name", product.name);
            table.putItem(item);
            product.setId(item.getString("id"));
            product.setName(item.getString("name"));
            return buildProxyResponseEvent(product, 201);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(500);
            response.setBody(e.getMessage());
            return response;
        }
    }

    private APIGatewayProxyResponseEvent handleGet(APIGatewayProxyRequestEvent event) {

        if (event.getPathParameters() != null) {
            String id = event.getPathParameters().get("id");
            GetItemRequest request = new GetItemRequest()
                    .withTableName(TABLE_NAME)
                    .withKey(Map.of("id", new AttributeValue(id)));

            Product product = new Product();
            GetItemResult item = amazonDynamoDB.getItem(request);
            if (item.getItem() != null && !item.getItem().isEmpty()) {
                product.setId(id);
                product.setName(item.getItem().get("name").getS());
            }

            return product.getId() == null ? buildNotFoundResponseEvent() : buildProxyResponseEvent(product, 200);
        }

        ScanRequest request = new ScanRequest()
                .withTableName(TABLE_NAME)
                .withLimit(20);
        ScanResult scanResult = amazonDynamoDB.scan(request);
        Map<String, List<Product>> result = new HashMap<>();
        result.put("Items", new ArrayList<>());

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        if (scanResult.getItems() != null && !scanResult.getItems().isEmpty()) {
            scanResult.getItems().forEach(item -> {
                Product product = new Product();
                product.setId(item.get("id").getS());
                product.setName(item.get("name").getS());
                result.get("Items").add(product);
            });
        }

        response.setStatusCode(200);
        response.setBody(gson.toJson(result));
        return response;
    }

    private APIGatewayProxyResponseEvent buildProxyResponseEvent(Product product, int statusCode) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(gson.toJson(product));
        return response;
    }

    private APIGatewayProxyResponseEvent buildNotFoundResponseEvent() {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(404);
        return response;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Product {
        private String id;
        private String name;
    }
}

