package me.winflix.vitalcore.addons.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.winflix.vitalcore.addons.interfaces.EffectKeyframe;

public class BbModelEffectLoader {

    public static Map<String, List<EffectKeyframe>> load(InputStream stream) throws IOException {
        Map<String, List<EffectKeyframe>> map = new HashMap<>();

        JsonObject root = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
        JsonArray animations = root.getAsJsonArray("animations");

        for (JsonElement animElement : animations) {
            JsonObject anim = animElement.getAsJsonObject();
            String name = anim.get("name").getAsString();
            JsonObject animators = anim.getAsJsonObject("animators");

            List<EffectKeyframe> list = new ArrayList<>();

            for (Map.Entry<String, JsonElement> entry : animators.entrySet()) {
                JsonObject animator = entry.getValue().getAsJsonObject();
                if (!"effect".equals(animator.get("type").getAsString()))
                    continue;

                JsonArray keyframes = animator.getAsJsonArray("keyframes");
                for (JsonElement kfElem : keyframes) {
                    JsonObject kf = kfElem.getAsJsonObject();
                    String channel = kf.get("channel").getAsString();
                    double time = kf.get("time").getAsDouble();
                    JsonArray dataPoints = kf.getAsJsonArray("data_points");

                    for (JsonElement dpElem : dataPoints) {
                        String effect = dpElem.getAsJsonObject().get("effect").getAsString();
                        list.add(new EffectKeyframe(channel, effect, time));
                    }
                }
            }

            map.put(name, list);
        }

        return map;
    }
}