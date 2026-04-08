package hcmute.edu.vn.tick_tick.util;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NLPParser {

    public static class ParseResult {
        public String cleanTitle;
        public Long date;
        public Integer hour = -1;
        public Integer minute = -1;
        public int matchStart = -1;
        public int matchEnd = -1;
    }

    public static ParseResult parse(String input) {
        ParseResult result = new ParseResult();
        String lowerInput = input.toLowerCase();
        Calendar cal = Calendar.getInstance();
        boolean dateFound = false;

        // Pattern tổng hợp để bóc tách và highlight
        String dateRegex = "\\b(hôm nay|nay|ngày mai|mai|ngày kia|mốt|chủ nhật|thứ\\s*[2-7]|cn|t[2-7])\\b";
        String timeRegex = "\\b(?:lúc|vào|hồi)?\\s*(\\d{1,2})(?:[:h]|\\s*giờ)\\s*(\\d{0,2})\\b";
        String periodRegex = "\\b(sáng|trưa|chiều|tối|đêm)\\b";

        // Tìm kiếm toàn bộ cụm từ liên quan đến thời gian để highlight
        Pattern fullPattern = Pattern.compile("(?i)(" + dateRegex + "|" + timeRegex + "|" + periodRegex + ")");
        Matcher fullMatcher = fullPattern.matcher(input);
        
        int earliestStart = -1;
        int latestEnd = -1;

        while (fullMatcher.find()) {
            if (earliestStart == -1) earliestStart = fullMatcher.start();
            latestEnd = fullMatcher.end();
            
            String match = fullMatcher.group().toLowerCase();
            
            // Xử lý ngày
            if (match.contains("mai")) { cal.add(Calendar.DAY_OF_YEAR, 1); dateFound = true; }
            else if (match.contains("kia") || match.contains("mốt")) { cal.add(Calendar.DAY_OF_YEAR, 2); dateFound = true; }
            else if (match.contains("nay")) { dateFound = true; }
            else if (match.matches(".*(thứ|cn|t[2-7]).*")) {
                // Xử lý thứ (giữ nguyên logic cũ nhưng gọn hơn)
                String[] days = {"cn", "t2", "t3", "t4", "t5", "t6", "t7", "chủ nhật", "thứ hai", "thứ ba", "thứ tư", "thứ năm", "thứ sáu", "thứ bảy"};
                for (int i = 0; i < days.length; i++) {
                    if (match.contains(days[i])) {
                        int targetDay = (i % 7 == 0) ? Calendar.SUNDAY : (i % 7 + 1);
                        int currentDay = cal.get(Calendar.DAY_OF_WEEK);
                        int daysUntil = (targetDay - currentDay + 7) % 7;
                        if (daysUntil == 0) daysUntil = 7;
                        cal.add(Calendar.DAY_OF_YEAR, daysUntil);
                        dateFound = true;
                        break;
                    }
                }
            }

            // Xử lý giờ (nếu khớp pattern x giờ y phút)
            Pattern tPattern = Pattern.compile(timeRegex);
            Matcher tMatcher = tPattern.matcher(match);
            if (tMatcher.find()) {
                result.hour = Integer.parseInt(tMatcher.group(1));
                result.minute = (tMatcher.group(2) == null || tMatcher.group(2).isEmpty()) ? 0 : Integer.parseInt(tMatcher.group(2));
            }

            // Xử lý buổi
            if (match.contains("sáng")) { if (result.hour == -1) result.hour = 8; else if (result.hour == 12) result.hour = 0; }
            else if (match.contains("trưa")) { if (result.hour == -1) result.hour = 12; }
            else if (match.contains("chiều") || match.contains("tối") || match.contains("đêm")) {
                 if (result.hour != -1 && result.hour < 12) result.hour += 12;
                 else if (result.hour == -1) {
                     if (match.contains("chiều")) result.hour = 16;
                     else if (match.contains("tối")) result.hour = 20;
                     else result.hour = 23;
                 }
            }
        }

        result.matchStart = earliestStart;
        result.matchEnd = latestEnd;
        result.cleanTitle = input.trim();
        
        if (dateFound || result.hour != -1) {
            cal.set(Calendar.HOUR_OF_DAY, result.hour != -1 ? result.hour : 9);
            cal.set(Calendar.MINUTE, result.minute != -1 ? result.minute : 0);
            cal.set(Calendar.SECOND, 0);
            result.date = cal.getTimeInMillis();
        }

        return result;
    }
}
