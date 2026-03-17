package study.blog.global.infra;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import study.blog.post.infrastructure.persistence.query.PostQueryRepository;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LinkPreviewService {

    private static final int TIMEOUT_MS = 5_000;
    private static final Pattern POST_PATH_PATTERN = Pattern.compile("/posts/(\\d+)$");

    private final PostQueryRepository postQueryRepository;

    @Value("${app.base-url:}")
    private String appBaseUrl;

    public LinkPreviewResponse fetchPreview(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return empty(url);
        }

        LinkPreviewResponse selfPreview = tryResolveSelfPost(url);
        if (selfPreview != null) return selfPreview;

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .timeout(TIMEOUT_MS)
                    .get();

            String title = ogOrFallback(doc, "og:title", null);
            if (title.isEmpty()) title = doc.title();

            String description = ogOrFallback(doc, "og:description", "description");
            String image       = ogOrFallbackAbsUrl(doc, "og:image", url);
            String favicon     = extractFavicon(doc, url);

            return new LinkPreviewResponse(title, description, image, favicon, url);
        } catch (Exception e) {
            return empty(url);
        }
    }

    // 자체 블로그 포스트 URL이면 DB에서 직접 조회
    private LinkPreviewResponse tryResolveSelfPost(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (appBaseUrl.isEmpty() || !url.startsWith(appBaseUrl)) {
                // appBaseUrl 미설정 시 host만으로 비교 (localhost 포함)
                URI base = new URI(appBaseUrl.isEmpty() ? url : appBaseUrl);
                if (!host.equals(base.getHost())) return null;
            }
            Matcher m = POST_PATH_PATTERN.matcher(uri.getPath());
            if (!m.find()) return null;

            long postId = Long.parseLong(m.group(1));
            return postQueryRepository.findById(postId)
                    .map(post -> new LinkPreviewResponse(
                            post.getTitle(),
                            post.getContent() != null
                                    ? post.getContent().replaceAll("[#*`>\\-\\n]", " ").strip().substring(0, Math.min(150, post.getContent().length()))
                                    : "",
                            post.getThumbnailUrl() != null ? post.getThumbnailUrl() : "",
                            "",
                            url
                    ))
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    // og:property → meta name fallback 순서로 조회
    private String ogOrFallback(Document doc, String property, String nameFallback) {
        Element el = doc.select("meta[property=" + property + "]").first();
        if (el != null && !el.attr("content").isEmpty()) return el.attr("content");
        if (nameFallback != null) {
            el = doc.select("meta[name=" + nameFallback + "]").first();
            if (el != null) return el.attr("content");
        }
        return "";
    }

    // og:image 전용 — 상대경로를 절대 URL로 변환
    private String ogOrFallbackAbsUrl(Document doc, String property, String pageUrl) {
        doc.setBaseUri(pageUrl);
        Element el = doc.select("meta[property=" + property + "]").first();
        if (el != null) {
            String abs = el.absUrl("content");
            if (!abs.isEmpty()) return abs;
            String raw = el.attr("content");
            if (!raw.isEmpty()) return raw;
        }
        return "";
    }

    // <link rel="icon"> 절대 URL 추출 → 없으면 /favicon.ico
    private String extractFavicon(Document doc, String pageUrl) {
        Element link = doc.select("link[rel~=(?i)(shortcut )?icon]").first();
        if (link != null) {
            String href = link.attr("abs:href");
            if (!href.isEmpty()) return href;
        }
        try {
            URI uri = new URI(pageUrl);
            return uri.getScheme() + "://" + uri.getHost() + "/favicon.ico";
        } catch (Exception e) {
            return "";
        }
    }

    private LinkPreviewResponse empty(String url) {
        return new LinkPreviewResponse("", "", "", "", url);
    }
}
