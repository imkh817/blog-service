package study.blog.global.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

public class P6SpyFormatter implements MessageFormattingStrategy {

    private static final String BASE_PACKAGE = "study.blog";
    private static final String LINE = "─".repeat(80);

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        if (sql == null || sql.isBlank()) {
            return "";
        }

        String caller = findCaller();
        return String.format(
                "\n┌ %s\n│  %-10s %s\n│  %-10s %dms\n│  %-10s %s\n├ %s\n│  %s\n└ %s",
                LINE,
                "[시각]",   now,
                "[실행시간]", elapsed,
                "[호출위치]", caller,
                LINE,
                sql,
                LINE
        );
    }

    private String findCaller() {
        for (StackTraceElement element : new Throwable().getStackTrace()) {
            String className = element.getClassName();
            if (className.startsWith(BASE_PACKAGE) && !className.equals(P6SpyFormatter.class.getName())) {
                // 풀 패키지명 대신 클래스명부터 표시
                String simpleClass = className.substring(className.lastIndexOf('.') + 1);
                return simpleClass + "." + element.getMethodName()
                        + "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
            }
        }
        return "unknown";
    }
}
