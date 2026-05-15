package com.example.demo.service.impl;

import com.example.demo.model.dto.ChatRequestDTO;
import com.example.demo.model.vo.ChatResponseVO;
import com.example.demo.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final StringRedisTemplate stringRedisTemplate;

    public ChatServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public ChatResponseVO chat(ChatRequestDTO requestDTO) {
        String sessionId = requestDTO.getSessionId();
        String message = requestDTO.getMessage();

        String redisKey = "chat:session:" + sessionId;

        List<String> records = stringRedisTemplate.opsForList().range(redisKey, 0, -1);
        String historyText = "";
        if (records != null && !records.isEmpty()) {
            historyText = String.join("\n", records);
        }

        String finalPrompt = String.format("以下是历史对话：\n%s\n\n当前用户问题：\n%s", historyText, message);

        String answer = callDashScopeAPI(finalPrompt);

        String recordText = "用户：" + message + "\n助手：" + answer;
        stringRedisTemplate.opsForList().rightPush(redisKey, recordText);

        Long size = stringRedisTemplate.opsForList().size(redisKey);
        if (size != null && size > 3) {
            stringRedisTemplate.opsForList().trim(redisKey, size - 3, size - 1);
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

            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("model", "qwen-turbo");

            ObjectNode inputNode = objectMapper.createObjectNode();
            ArrayNode messagesArray = objectMapper.createArrayNode();

            ObjectNode messageNode = objectMapper.createObjectNode();
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