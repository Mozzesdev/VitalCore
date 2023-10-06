package me.winflix.vitalcore.skins.models.Fetching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class SkinFetch {
    private final String value;
    private final String signature;

    public SkinFetch(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);

        if (rootNode.has("properties")) {
            JsonNode propertiesNode = rootNode.get("properties");

            if (propertiesNode.isArray() && !propertiesNode.isEmpty()) {
                JsonNode firstProperty = propertiesNode.get(0);

                if (firstProperty.has("value") && firstProperty.has("signature")) {
                    this.value = firstProperty.get("value").asText();
                    this.signature = firstProperty.get("signature").asText();
                    return;
                }
            }
        }

        this.value = null;
        this.signature = null;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public String toString(){
        return "SkinFetch [value="
                + value
                + ", signature=" + signature
                + "]";
    }
}