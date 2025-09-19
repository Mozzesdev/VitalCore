package me.winflix.vitalcore.skins.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojang.authlib.properties.Property;

public class Skin {

    private UUID ownerId;
    private String ownerName;
    private Property property;

    public Skin() {
    }

    public Skin(UUID ownerId, String ownerName, Property property) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.property = property;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    // Serializar Skin a JSON
    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Deserializar JSON a Skin
    public static Skin fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, Skin.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, String> getPropertyAsMap() {
        Map<String, String> map = new HashMap<>();
        map.put("name", property.name());
        map.put("value", property.value());
        if (property.hasSignature()) {
            map.put("signature", property.signature());
        }
        return map;
    }

    public static Property propertyFromMap(Map<String, String> map) {
        return new Property(
                map.get("name"),
                map.get("value"),
                map.get("signature"));
    }

    @Override
    public String toString() {
        return "Skin [ownerId=" + ownerId + ", ownerName=" + ownerName + "]";
    }
}
