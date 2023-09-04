package me.winflix.vitalcore.general.utils;

import java.util.Map;

public class Placeholders {

    public final static String TARGET_NAME = "${target:name}";

    public final static String OFF_TARGET_NAME = "${off-target:name}";

    public final static String TRIBE_NAME = "${tribe:name}";

    public final static String TRIBE_RANK = "${tribe:name}";

    public final static String PROMPT_ACCEPT = "${pr:accept}";

    public final static String PROMPT_REJECT = "${pr:reject}";

    public final static String COMMAND_SYNTAX = "${cm:syntax}";

    public final static String PLUGIN_NAME = "${plugin}";

    public static String replacePlaceholders(String message, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = entry.getKey();
            String value = entry.getValue();
            if (message.contains(placeholder)) {
                message = message.replace(placeholder, value);
            }
        }
        return message;
    }

}
