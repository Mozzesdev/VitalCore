package me.winflix.vitalcore.addons.interfaces;

import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

import me.winflix.vitalcore.addons.interfaces.BbModel.ModelTexture;

public class ProcessedPackData {
    private final String modelName;
    private final Map<String, JsonObject> geometryJsons;
    private final Map<String, JsonObject> itemDefinitionJsons;
    private final Set<ModelTexture> texturesUsed;

    public ProcessedPackData(String modelName,
            Map<String, JsonObject> geometryJsons, Map<String, JsonObject> itemDefinitionJsons, Set<ModelTexture>texturesUsed) {
        this.modelName = modelName;
        this.geometryJsons = geometryJsons;
        this.itemDefinitionJsons = itemDefinitionJsons;
        this.texturesUsed = texturesUsed;
    }

    public String getModelName() {
        return modelName;
    }

    public Set<ModelTexture> getTexturesUsed() {
        return texturesUsed;
    }

    public Map<String, JsonObject> getGeometryJsons() {
        return geometryJsons;
    }

    public Map<String, JsonObject> getItemDefinitionJsons() {
        return itemDefinitionJsons;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ProcessedModelData{");
        sb.append("modelName='").append(modelName).append('\'');
        sb.append(", geometryJsonsKeys=").append(geometryJsons != null ? geometryJsons.keySet() : "null");
        sb.append(", itemDefinitionJsonsKeys=")
                .append(itemDefinitionJsons != null ? itemDefinitionJsons.keySet() : "null");
        sb.append('}');
        return sb.toString();
    }
}