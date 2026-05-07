package com.example.demo.service.impl;

import com.example.demo.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class ChatServiceImpl implements ChatService {

    private static final String DASHSCOPE_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String chat(String message) {
        String apiKey = System.getenv("AI_DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            return "请设置环境变量 AI_DASHSCOPE_API_KEY";
        }

        try {
            URL url = new URL(DASHSCOPE_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("model", "qwen-turbo");
            
            ObjectNode inputNode = objectMapper.createObjectNode();
            ArrayNode messagesArray = objectMapper.createArrayNode();
            
            ObjectNode messageNode = objectMapper.createObjectNode();
            messageNode.put("role", "user");
            messageNode.put("content", message);
            
            messagesArray.add(messageNode);
            inputNode.set("messages", messagesArray);
            rootNode.set("input", inputNode);

            String jsonInput = objectMapper.writeValueAsString(rootNode);

            try (OutputStream outputStream = conn.getOutputStream()) {
                outputStream.write(jsonInput.getBytes("UTF-8"));
                outputStream.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonNode responseJson = objectMapper.readTree(response.toString());
                JsonNode output = responseJson.get("output");
                if (output != null) {
                    JsonNode text = output.get("text");
                    if (text != null) {
                        return text.asText();
                    }
                }
                return response.toString();
            } else {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                return "API调用失败: " + errorResponse.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "调用失败: " + e.getMessage();
        }
    }
}