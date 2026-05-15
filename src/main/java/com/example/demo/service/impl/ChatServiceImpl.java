package com.example.demo.service.impl;

import com.example.demo.model.dto.ChatRequestDTO;
import com.example.demo.model.entity.ChatRecord;
import com.example.demo.model.vo.ChatResponseVO;
import com.example.demo.service.ChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private static final int MAX_HISTORY_ROUNDS = 3;
    private final ObjectMapper objectMapper;

    private final StringRedisTemplate stringRedisTemplate;

    public ChatServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public ChatResponseVO chat(ChatRequestDTO requestDTO) {
        String sessionId = requestDTO.getSessionId();
        String message = requestDTO.getMessage();

        String redisKey = "chat:session:" + sessionId;

        List<String> records = stringRedisTemplate.opsForList().range(redisKey, 0, -1);
        StringBuilder historyText = new StringBuilder();

        int startIndex = records.size() > MAX_HISTORY_ROUNDS ? records.size() - MAX_HISTORY_ROUNDS : 0;
        for (int i = startIndex; i < records.size(); i++) {
            String recordJson = records.get(i);
            try {
                ChatRecord record = objectMapper.readValue(recordJson, ChatRecord.class);
                historyText.append("用户: ").append(record.getUserMessage()).append("\n");
                historyText.append("助手: ").append(record.getAssistantMessage()).append("\n");
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        String prompt = historyText + "用户: " + message;
        String answer = callDashScopeAPI(prompt);

        ChatRecord newRecord = new ChatRecord();
        newRecord.setSessionId(sessionId);
        newRecord.setUserMessage(message);
        newRecord.setAssistantMessage(answer);
        newRecord.setCreateTime(LocalDateTime.now());

        try {
            String recordJson = objectMapper.writeValueAsString(newRecord);
            stringRedisTemplate.opsForList().rightPush(redisKey, recordJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new ChatResponseVO(message, answer);
    }

    private String callDashScopeAPI(String prompt) {
        String apiKey = System.getenv("AI_DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            return "请设置环境变量 AI_DASHSCOPE_API_KEY";
        }

        try {
            java.net.URL url = new java.net.URL("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            com.fasterxml.jackson.databind.node.ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("model", "qwen-turbo");

            com.fasterxml.jackson.databind.node.ObjectNode inputNode = objectMapper.createObjectNode();
            com.fasterxml.jackson.databind.node.ArrayNode messagesArray = objectMapper.createArrayNode();

            com.fasterxml.jackson.databind.node.ObjectNode messageNode = objectMapper.createObjectNode();
            messageNode.put("role", "user");
            messageNode.put("content", prompt);

            messagesArray.add(messageNode);
            inputNode.set("messages", messagesArray);
            rootNode.set("input", inputNode);

            String jsonInput = objectMapper.writeValueAsString(rootNode);

            try (java.io.OutputStream outputStream = conn.getOutputStream()) {
                outputStream.write(jsonInput.getBytes("UTF-8"));
                outputStream.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                com.fasterxml.jackson.databind.JsonNode responseJson = objectMapper.readTree(response.toString());
                com.fasterxml.jackson.databind.JsonNode output = responseJson.get("output");
                if (output != null) {
                    com.fasterxml.jackson.databind.JsonNode text = output.get("text");
                    if (text != null) {
                        return text.asText();
                    }
                }
                return response.toString();
            } else {
                java.io.BufferedReader errorReader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getErrorStream(), "UTF-8"));
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