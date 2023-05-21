package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/jsonfile")
public class JsonFileController {
    private static final String FILE_NAME = "sample.json";
    private final ObjectMapper objectMapper;

    public JsonFileController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadJsonFile(@RequestBody Map<String, Integer> requestBody) throws IOException {
        File file = getFile();
        objectMapper.writeValue(file, requestBody);
        return ResponseEntity.ok("JSON file uploaded successfully.");
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateJsonFile(@RequestBody JsonNode requestBody) throws IOException {
        JsonNode rootNode = readJsonFile();
        if (requestBody.has("valueX")) {
            ((ObjectNode) rootNode).put("valueX", requestBody.get("valueX").asInt());
        }

        if (requestBody.has("valueY")) {
            ((ObjectNode) rootNode).put("valueY", requestBody.get("valueY").asInt());
        }
        writeJsonFile(rootNode);
        return ResponseEntity.ok("JSON file updated successfully");
    }

    @GetMapping("/getResult")
    public ResponseEntity<InputStreamResource> getResultJsonFile() throws IOException {
        JsonNode rootNode = readJsonFile();
        int valueX = rootNode.get("valueX").asInt();
        int valueY = rootNode.get("valueY").asInt();
        ((ObjectNode) rootNode).put("result", valueX + valueY);
        writeJsonFile(rootNode);

        File file = getFile();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + FILE_NAME);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteJsonFile() throws IOException {
        File file = getFile();
        if (file.delete()) {
            return ResponseEntity.ok("JSON file deleted");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the JSON file");
        }
    }

    private File getFile() throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    private JsonNode readJsonFile() throws IOException {
        Path path = Path.of(FILE_NAME);
        byte[] jsonData = Files.readAllBytes(path);
        return objectMapper.readTree(jsonData);
    }

    private void writeJsonFile(JsonNode jsonNode) throws IOException {
        Path path = Path.of(FILE_NAME);
        String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        Files.writeString(path, jsonString, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
