package net.flaily.util;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NBTParser {
    private static final Pattern ENTRY = Pattern.compile("(\\w+):((\"[^\"]*\")|([^,{}\"]+))");

    public static HashMap<String, String> parse(String input) {
        HashMap<String, String> map = new HashMap<>();
        Matcher matcher = ENTRY.matcher(input);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).replace("\"", "");
            map.put(key, value);
        }
        return map;
    }
}