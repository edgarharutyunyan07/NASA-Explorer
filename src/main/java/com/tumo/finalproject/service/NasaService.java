package com.tumo.finalproject.service;

import com.tumo.finalproject.model.NasaMedia;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Talks to NASA's Image and Video Library so our app can search real space
 * photos and videos.
 *
 * <p>{@code @Service} marks this class as a Spring-managed component, which is why
 * {@code MediaController} can ask for a {@code NasaService} in its constructor and
 * simply receive one. That is <b>dependency injection</b>: you never write
 * {@code new NasaService(...)} yourself.
 *
 * <p>Unlike most APIs you will meet, this one needs <b>no API key at all</b> —
 * it is fully public. That also means there is nothing to read from
 * {@code application.properties} here.
 *
 * <p>The API you are calling:
 * <pre>
 *   GET https://images-api.nasa.gov/search?q=apollo+11&amp;media_type=image,video
 * </pre>
 * Paste that in a browser to see the JSON you have to parse. Unlike a flat
 * "results" array, NASA nests things one level deeper:
 * <pre>
 *   {
 *     "collection": {
 *       "items": [
 *         {
 *           "data":  [ { "nasa_id": "...", "title": "...", "description": "...",
 *                        "date_created": "...", "media_type": "image" } ],
 *           "links": [ { "href": "https://...thumb.jpg", "rendition": "thumb" } ]
 *         }
 *       ]
 *     }
 *   }
 * </pre>
 * Each element of {@code items} carries its metadata in a one-element
 * {@code data} array, and its preview image in a {@code links} array — you have
 * to reach through both.
 */
@Service
public class NasaService {

    /**
     * Sends the HTTP requests. {@code WebClient} is Spring's modern HTTP client.
     * TODO: build this in the constructor.
     */
    private WebClient webClient;

    /**
     * Turns JSON text into objects we can navigate.
     * TODO: create this in the constructor.
     */
    private ObjectMapper objectMapper;

    /**
     * Spring calls this constructor at startup.
     *
     * <h2>TODO — initialise the two fields above</h2>
     * <pre>
     *   this.objectMapper = new ObjectMapper();
     *
     *   this.webClient = WebClient.builder()
     *           .baseUrl("https://images-api.nasa.gov")
     *           .build();
     * </pre>
     * Setting a {@code baseUrl} once means every request below only needs the path
     * ({@code "/search"}) instead of the whole URL.
     */
    public NasaService() {
        // TODO: initialise objectMapper and webClient here.
    }

    /**
     * Searches NASA's library and returns every photo/video it found, or an
     * empty list if there were no matches.
     *
     * <h2>TODO — implement in two steps</h2>
     * <b>Step 1: fetch the JSON.</b> Perform a GET and block until the response
     * arrives (blocking keeps things simple while you are learning):
     * <pre>
     *   String response = webClient.get()
     *           .uri(uriBuilder -&gt; uriBuilder
     *                   .path("/search")
     *                   .queryParam("q", query)
     *                   .queryParam("media_type", "image,video")
     *                   .build())
     *           .retrieve()
     *           .bodyToMono(String.class)
     *           .block();
     * </pre>
     * Use {@code queryParam} rather than gluing the URL together with {@code +}:
     * it URL-encodes the value for you, so a search containing a space or an
     * {@code &amp;} still works.
     *
     * <p><b>Step 2:</b> hand the text to {@link #parseMedia(String)} and return
     * the result.
     *
     * @param query what the user typed, e.g. "apollo 11"
     */
    public List<NasaMedia> searchMedia(String query) {
        // TODO: call NASA /search, then return parseMedia(response).
        throw new UnsupportedOperationException("NasaService.searchMedia not implemented");
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
        // TODO: search for the topic and return the first result, or null.
        throw new UnsupportedOperationException("NasaService.searchOne not implemented");
    }

    /**
     * Converts NASA's raw JSON into a list of {@link NasaMedia} objects.
     *
     * <p>This method is {@code private} on purpose: it is an internal helper, not
     * something controllers should call. Keeping the JSON details in here means the
     * rest of the app only ever deals with clean {@code NasaMedia} objects.
     *
     * <h2>TODO — implement</h2>
     * <ol>
     *   <li>Create an empty {@code List<NasaMedia>} to collect results into.</li>
     *   <li>Wrap the parsing in {@code try/catch}, because malformed JSON throws.</li>
     *   <li>{@code JsonNode root = objectMapper.readTree(json);} then
     *       {@code JsonNode items = root.get("collection").get("items");}
     *       (import {@code tools.jackson.databind.JsonNode}).</li>
     *   <li>Check {@code items != null && items.isArray()} before looping — an
     *       error response from NASA has no {@code items} field at all, and
     *       calling a method on null would crash with a
     *       {@code NullPointerException}.</li>
     *   <li>For each {@code JsonNode item} in {@code items}:
     *       <pre>
     *   JsonNode data = item.get("data").get(0);
     *   NasaMedia media = new NasaMedia();
     *   media.setId(data.get("nasa_id").asString());
     *   media.setTitle(data.has("title") ? data.get("title").asString() : "");
     *   media.setDescription(data.has("description") ? data.get("description").asString() : "");
     *   media.setMediaType(data.has("media_type") ? data.get("media_type").asString() : "image");
     *   media.setDateCreated(data.has("date_created") ? data.get("date_created").asString() : "");
     *       </pre>
     *       The {@code has("...") ? ... : default} check matters: not every item
     *       has every field.</li>
     *   <li>The thumbnail lives one level up from {@code data}, in
     *       {@code item.get("links")} — an array that can be missing entirely:
     *       <pre>
     *   JsonNode links = item.get("links");
     *   if (links != null &amp;&amp; links.isArray() &amp;&amp; !links.isEmpty()) {
     *       media.setThumbnailUrl(links.get(0).get("href").asString());
     *   }
     *       </pre></li>
     *   <li>Add {@code media} to the list, and return the list.</li>
     *   <li>In the {@code catch}, throw
     *       {@code new RuntimeException("Failed to parse NASA response", e)}.
     *       Passing {@code e} as the cause keeps the original stack trace, which
     *       you will want when debugging.</li>
     * </ol>
     */
    private List<NasaMedia> parseMedia(String json) {
        // TODO: read the "collection.items" array and build one NasaMedia per element.
        throw new UnsupportedOperationException("NasaService.parseMedia not implemented");
    }
}
