package com.trading.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateResponse(String prompt, MultipartFile image) {
        if ("YOUR_API_KEY_HERE".equals(apiKey) || apiKey == null || apiKey.trim().isEmpty()) {
            return "Please configure your Gemini API Key in `application.properties` to talk to Nova!";
        }

        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey;

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();

            // Construct contents — embed system context directly in the prompt for compatibility
            String systemContext = "You are Nova, an expert AI trading assistant for a virtual stock trading simulator called FinNova. Briefly answer questions regarding stock trading, indicators, market analysis, and evaluate chart patterns if the user uploads a screenshot. You have a polite, concise, and encouraging tone. Do not use markdown headers, keep the layout simple for a compact chat window.\n\nUser: ";

            ArrayNode contents = requestBody.putArray("contents");
            ObjectNode content = contents.addObject();
            content.put("role", "user");
            ArrayNode parts = content.putArray("parts");

            // Add Text part with system context prepended
            parts.addObject().put("text", systemContext + prompt);

            // Add Image part if it exists
            if (image != null && !image.isEmpty()) {
                String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
                String mimeType = image.getContentType();
                if (mimeType == null || mimeType.isEmpty()) {
                    mimeType = "image/jpeg"; // default fallback
                }

                ObjectNode inlineData = parts.addObject().putObject("inlineData");
                inlineData.put("mimeType", mimeType);
                inlineData.put("data", base64Image);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            String responseBody = restTemplate.postForObject(apiUrl, entity, String.class);
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            // Extract the generated text: response.candidates[0].content.parts[0].text
            if (rootNode.has("candidates") && rootNode.get("candidates").isArray() && rootNode.get("candidates").size() > 0) {
                JsonNode candidate = rootNode.get("candidates").get(0);
                if (candidate.has("content") && candidate.get("content").has("parts")) {
                    return candidate.get("content").get("parts").get(0).get("text").asText();
                }
            }

            return "I'm sorry, I couldn't generate a response at this moment.";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Oops! I encountered an error communicating with my AI core. Error: " + e.getMessage();
        }
    }
}
