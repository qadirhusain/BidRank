package outbid.clone.product.util;

import org.springframework.stereotype.Component;
import outbid.clone.product.entity.ListingType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@Component
public class UrlNormalizer {

    private static final String X_HOST = "x.com/";

    public String normalize(ListingType listingType, String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException("URL must not be blank");
        }

        String value = rawUrl.trim();
        return listingType == ListingType.X_PROFILE
                ? normalizeXProfile(value)
                : normalizeWebsite(value);
    }

    private String normalizeWebsite(String value) {
        String withScheme = value.matches("(?i)^https?://.*") ? value : "https://" + value;

        URI uri;
        try {
            uri = new URI(withScheme);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + value);
        }

        String host = uri.getHost();
        if (host == null || !host.contains(".")) {
            throw new IllegalArgumentException("Invalid URL: " + value);
        }

        host = host.toLowerCase(Locale.ROOT);
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }

        String path = uri.getRawPath() == null ? "" : uri.getRawPath().replaceAll("/+$", "");
        StringBuilder canonical = new StringBuilder(host).append(path);

        if (host.equals("play.google.com")) {
            String appId = extractParam(uri.getRawQuery(), "id");
            if (appId != null) {
                canonical.append("?id=").append(appId);
            }
        }

        return canonical.toString();
    }

    private String normalizeXProfile(String value) {
        String handle = value;

        if (handle.contains("/")) {
            String[] parts = handle.replaceFirst("(?i)^https?://", "").split("[/?#]");
            if (parts.length < 2 || parts[1].isBlank()) {
                throw new IllegalArgumentException("Invalid X profile: " + value);
            }
            handle = parts[1];
        }

        handle = handle.replaceFirst("^@", "").toLowerCase(Locale.ROOT);
        if (!handle.matches("^[a-z0-9_]{1,15}$")) {
            throw new IllegalArgumentException("Invalid X profile: " + value);
        }

        return X_HOST + handle;
    }

    private String extractParam(String rawQuery, String name) {
        if (rawQuery == null) {
            return null;
        }
        for (String pair : rawQuery.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2 && keyValue[0].equals(name)) {
                return keyValue[1];
            }
        }
        return null;
    }
}
