package com.provectus.kafka.ui.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.provectus.kafka.ui.api.ApiClient;
import com.provectus.kafka.ui.api.api.KafkaConnectApi;
import com.provectus.kafka.ui.api.api.MessagesApi;
import com.provectus.kafka.ui.api.api.TopicsApi;
import com.provectus.kafka.ui.api.model.CreateTopicMessage;
import com.provectus.kafka.ui.api.model.ErrorResponse;
import com.provectus.kafka.ui.api.model.NewConnector;
import com.provectus.kafka.ui.api.model.TopicCreation;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@Slf4j
public class ApiHelper {

    int partitions = 1;
    int replicationFactor = 1;
    String newTopic = "new-topic";
    String baseURL = "http://localhost:8080";


    @SneakyThrows
    private TopicsApi topicApi() {
        return new TopicsApi(new ApiClient().setBasePath(baseURL));
    }


    public void createTopic(String clusterName, String topicName) {
        TopicCreation topic = new TopicCreation();
        topic.setName(topicName);
        topic.setPartitions(partitions);
        topic.setReplicationFactor(replicationFactor);
        topic.setConfigs(new HashMap<>());
        deleteTopic(clusterName, topicName);
        try {
            topicApi().createTopic(clusterName, topic).block();
        } catch (WebClientResponseException ex) {
            ex.printStackTrace();
        }
    }

    @SneakyThrows
    public void deleteTopic(String clusterName, String topicName) {
        try {
            topicApi().deleteTopic(clusterName, topicName).block();
        } catch (WebClientResponseException ex) {
            ErrorResponse errorResponse = new Gson().fromJson(ex.getResponseBodyAsString(), ErrorResponse.class);
            if (errorResponse.getMessage().startsWith("This server does not host this")) {
                log.info("This server does not host this " + topicName);
            } else {
                throw ex;
            }
        }
    }

    @SneakyThrows
    private KafkaConnectApi connectorApi() {
        ApiClient defaultClient = new ApiClient();
        defaultClient.setBasePath(baseURL);
        KafkaConnectApi connectorsApi = new KafkaConnectApi(defaultClient);
        return connectorsApi;
    }

    @SneakyThrows
    public void deleteConnector(String clusterName, String connectName, String connectorName) {
        try {
            connectorApi().deleteConnector(clusterName, connectName, connectorName).block();
        } catch (WebClientResponseException ignore) {
        }
    }

    @SneakyThrows
    public void createConnector(String clusterName, String connectName, String connectorName, String configJson) {
        NewConnector connector = new NewConnector();
        connector.setName(connectorName);
        Map<String, Object> configMap = new ObjectMapper().readValue(configJson, HashMap.class);
        connector.setConfig(configMap);
        try {
            connectorApi().deleteConnector(clusterName, connectName, connectorName).block();
        } catch (WebClientResponseException ignored){
        }
        connectorApi().createConnector(clusterName, connectName, connector).block();
    }

    public String getFirstConnectName(String clusterName) {
        return connectorApi().getConnects(clusterName).blockFirst().getName();
    }

    @SneakyThrows
    private MessagesApi messageApi() {
        ApiClient defaultClient = new ApiClient();
        defaultClient.setBasePath(baseURL);
        MessagesApi messagesApi = new MessagesApi(defaultClient);
        return messagesApi;
    }

    @SneakyThrows
    public void sendMessage(String clusterName, String topicName, String messageContentJson,
            String messageKey) {
        CreateTopicMessage createMessage = new CreateTopicMessage();
        createMessage.partition(0);
        createMessage.setContent(messageContentJson);
        createMessage.setKey(messageKey);
        try {
            messageApi().sendTopicMessages(clusterName, topicName, createMessage).block();
        } catch (WebClientResponseException ex) {
            ex.getRawStatusCode();
        }
    }
}
