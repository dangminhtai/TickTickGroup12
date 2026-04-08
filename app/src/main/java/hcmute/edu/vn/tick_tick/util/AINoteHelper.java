package hcmute.edu.vn.tick_tick.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Lightweight, offline heuristic to derive tags and a short summary from note text.
 * This keeps us functional without requiring an external AI key; swap this class with
 * a real API client later if desired.
 */
public final class AINoteHelper {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the","and","for","with","that","have","this","from","your","nhung",
            "voi","cua","ban","nhat","mot","cho","khi","nay","la","thi","trong"
    ));

    public static Suggestion suggest(String notes) {
        if (notes == null) notes = "";
        String cleaned = notes.trim();
        return new Suggestion(makeSummary(cleaned), String.join(", ", topKeywords(cleaned, 4)));
    }

    private static String makeSummary(String text) {
        if (text.isEmpty()) return "";
        int maxLen = 160;
        if (text.length() <= maxLen) return text;
        int dot = text.indexOf('.', 80);
        if (dot != -1 && dot <= maxLen) return text.substring(0, dot + 1).trim();
        return text.substring(0, Math.min(text.length(), maxLen)).trim() + "…";
    }

    private static List<String> topKeywords(String text, int limit) {
        if (text.isEmpty()) return Collections.emptyList();
        String[] tokens = text.toLowerCase(Locale.getDefault()).split("[^a-zA-Zà-ỹÀ-Ỹ0-9']+");
        Map<String, Integer> freq = new HashMap<>();
        for (String t : tokens) {
            if (t.length() < 4 || STOP_WORDS.contains(t)) continue;
            freq.put(t, freq.getOrDefault(t, 0) + 1);
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(freq.entrySet());
        list.sort(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed());
        List<String> out = new ArrayList<>();
        for (int i = 0; i < list.size() && out.size() < limit; i++) {
            out.add(list.get(i).getKey());
        }
        return out;
    }

    public static final class Suggestion {
        public final String summary;
        public final String tags;
        public Suggestion(String summary, String tags) {
            this.summary = summary;
            this.tags = tags;
        }
    }
}
