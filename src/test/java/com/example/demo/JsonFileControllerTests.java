package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileControllerTests {
    private static final String FILE_NAME = "sample.json";
    private JsonFileController jsonFileController;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        jsonFileController = new JsonFileController(objectMapper);
    }

    @Test
    void uploadJsonFile_shouldCreateFileAndWriteJson() throws IOException {
        File file = new File(FILE_NAME);
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("valueX", 10);
        requestBody.put("valueY", 20);

        ResponseEntity<String> response = jsonFileController.uploadJsonFile(requestBody);

        assertTrue(file.exists());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("JSON file uploaded successfully.", response.getBody());
    }

    @Test
    void updateJsonFile_shouldUpdateJsonFile() throws IOException {
        createSampleJsonFile();
        JsonNode jsonNode = createJsonNode("{\"valueX\": 30, \"valueY\": 40}");

        ResponseEntity<String> response = jsonFileController.updateJsonFile(jsonNode);

        JsonNode updatedJson = readJsonFromFile();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("JSON file updated successfully", response.getBody());
        assertEquals("30", updatedJson.get("valueX").asText());
        assertEquals("40", updatedJson.get("valueY").asText());
    }

    @Test
    void getResultJsonFile_shouldGetResultToExistingJson() throws IOException {
        createSampleJsonFile();
        JsonNode originalJson = readJsonFromFile();

        ResponseEntity<InputStreamResource> response = jsonFileController.getResultJsonFile();

        JsonNode updatedJson = readJsonFromFile();
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Convert the response body InputStreamResource to a String
        String responseBody = new BufferedReader(new InputStreamReader(response.getBody().getInputStream()))
                .lines().collect(Collectors.joining("\n"));

        assertEquals(originalJson.get("valueX").asInt() + originalJson.get("valueY").asInt(),
                updatedJson.get("result").asInt());
    }


    @Test
    void deleteJsonFile_shouldDeleteJsonFile() throws IOException {
        createSampleJsonFile();

        ResponseEntity<String> response = jsonFileController.deleteJsonFile();

        assertFalse(new File(FILE_NAME).exists());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("JSON file deleted", response.getBody());
    }

    private void createSampleJsonFile() throws IOException {
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("valueX", 10);
        requestBody.put("valueY", 20);
        jsonFileController.uploadJsonFile(requestBody);
    }

    private JsonNode createJsonNode(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(json);
    }

    private JsonNode readJsonFromFile() throws IOException {
        Path path = Path.of(FILE_NAME);
        byte[] jsonData = Files.readAllBytes(path);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(jsonData);
    }
}
