package com.tumo.finalproject.service;

import com.tumo.finalproject.model.NasaMedia;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Searches NASA's official photo archive so our app can look up real space
 * photos.
 *
 * <p>{@code @Service} marks this class as a Spring-managed component, which is why
 * {@code MediaController} can ask for a {@code NasaService} in its constructor and
 * simply receive one. That is <b>dependency injection</b>: you never write
 * {@code new NasaService(...)} yourself.
 *
 * <p>We go through the Openverse API (openverse.org) rather than calling
 * {@code images-api.nasa.gov} directly, because {@code nasa.gov} is unreachable
 * from some networks (school/office content filters block the whole domain).
 * Openverse re-indexes NASA's official Flickr feed under {@code source=nasa},
 * so results are still genuine NASA photos, just served from a different host.
 * No API key is required for this volume of traffic.
 *
 * <p>The API you are calling:
 * <pre>
 *   GET https://api.openverse.org/v1/images/?q=apollo+11&amp;source=nasa
 * </pre>
 * Paste that in a browser to see the JSON you have to parse:
 * <pre>
 *   {
 *     "result_count": 240,
 *     "results": [
 *       { "id": "...", "title": "...", "indexed_on": "...",
 *         "thumbnail": "https://...", "url": "https://..." }
 *     ]
 *   }
 * </pre>
 * Unlike NASA's own API this is a flat array — no nested {@code data}/{@code links}
 * to reach through. Openverse only indexes images, not video, so every result's
 * media type is reported as {@code "image"}.
 */
@Service
public class NasaService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public NasaService() {
        this.objectMapper = new ObjectMapper();

        this.webClient = WebClient.builder()
                .baseUrl("https://api.openverse.org")
                .build();
    }

    /**
     * Searches NASA's official Flickr archive (via Openverse) and returns every
     * photo it found, or an empty list if there were no matches.
     *
     * @param query what the user typed, e.g. "apollo 11"
     */
    public List<NasaMedia> searchMedia(String query) {
        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/images/")
                        .queryParam("q", query)
                        .queryParam("source", "nasa")
                        .queryParam("page_size", 20)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return parseMedia(response);
    }

    /**
     * Returns NASA's single best match for a topic, or {@code null} if there is
     * none. The chatbot uses this to turn a recommended topic like
     * "Hubble Deep Field" into a real item the user can save.
     *
     * <h2>TODO — implement</h2>
     * Reuse {@link #searchMedia(String)}; do not duplicate the HTTP code. Call
     * it, then return the first element of the list — or {@code null} when the
     * list is empty. Never call {@code get(0)} without checking first, or you get
     * an {@code IndexOutOfBoundsException}.
     */
    public NasaMedia searchOne(String topic) {
        List<NasaMedia> results = searchMedia(topic);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Converts Openverse's raw JSON into a list of {@link NasaMedia} objects.
     *
     * <p>This method is {@code private} on purpose: it is an internal helper, not
     * something controllers should call. Keeping the JSON details in here means the
     * rest of the app only ever deals with clean {@code NasaMedia} objects.
     */
    private List<NasaMedia> parseMedia(String json) {
        List<NasaMedia> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.get("results");
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    NasaMedia media = new NasaMedia();
                    media.setId(item.has("id") ? item.get("id").asString() : "");
                    media.setTitle(item.has("title") ? item.get("title").asString() : "");
                    media.setDescription(item.has("attribution") ? item.get("attribution").asString() : "");
                    media.setMediaType("image");
                    media.setDateCreated(item.has("indexed_on") ? item.get("indexed_on").asString() : "");

                    if (item.has("thumbnail")) {
                        media.setThumbnailUrl(item.get("thumbnail").asString());
                    } else if (item.has("url")) {
                        media.setThumbnailUrl(item.get("url").asString());
                    }

                    results.add(media);
                }
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Openverse response", e);
        }
    }
}
