package com.tumo.finalproject.service;

import com.tumo.finalproject.model.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Talks to The Movie Database (TMDB) so our app can search real movies.
 *
 * <p>{@code @Service} marks this class as a Spring-managed component, which is why
 * {@code MovieController} can ask for a {@code TmdbService} in its constructor and
 * simply receive one. That is <b>dependency injection</b>: you never write
 * {@code new TmdbService(...)} yourself.
 *
 * <p>The API you are calling:
 * <pre>
 *   GET https://api.themoviedb.org/3/search/movie?api_key=YOUR_KEY&amp;query=batman
 * </pre>
 * Paste that in a browser (with your own key) to see the JSON you have to parse.
 * The part we care about is the {@code "results"} array; each element has
 * {@code id}, {@code title}, {@code overview}, {@code vote_average},
 * {@code release_date} and {@code poster_path}.
 */
@Service
public class TmdbService {

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

    /** Your personal TMDB key, read from {@code application.properties}. */
    private final String apiKey;

    /**
     * Spring calls this constructor at startup. {@code @Value} pulls
     * {@code tmdb.api.key} out of {@code application.properties} — which in turn
     * reads it from your {@code TMDB_API_KEY} environment variable — and passes it
     * in, so the secret never has to be written in the source code.
     *
     * <h2>TODO — initialise the two fields above</h2>
     * <pre>
     *   this.objectMapper = new ObjectMapper();
     *
     *   this.webClient = WebClient.builder()
     *           .baseUrl("https://api.themoviedb.org/3")
     *           .build();
     * </pre>
     * Setting a {@code baseUrl} once means every request below only needs the path
     * ({@code "/search/movie"}) instead of the whole URL.
     */
    public TmdbService(@Value("${tmdb.api.key}") String apiKey) {
        this.apiKey = apiKey;
        // TODO: initialise objectMapper and webClient here.
    }

    /**
     * Searches TMDB and returns every movie it found, or an empty list if there
     * were no matches.
     *
     * <h2>TODO — implement in two steps</h2>
     * <b>Step 1: fetch the JSON.</b> Perform a GET and block until the response
     * arrives (blocking keeps things simple while you are learning):
     * <pre>
     *   String response = webClient.get()
     *           .uri(uriBuilder -&gt; uriBuilder
     *                   .path("/search/movie")
     *                   .queryParam("api_key", apiKey)
     *                   .queryParam("query", query)
     *                   .build())
     *           .retrieve()
     *           .bodyToMono(String.class)
     *           .block();
     * </pre>
     * Use {@code queryParam} rather than gluing the URL together with {@code +}:
     * it URL-encodes the value for you, so a search for "Spider-Man 2" or a title
     * containing "&amp;" still works.
     *
     * <p><b>Step 2:</b> hand the text to {@link #parseMovies(String)} and return
     * the result.
     *
     * @param query what the user typed, e.g. "batman"
     */
    public List<Movie> searchMovies(String query) {
        // TODO: call TMDB /search/movie, then return parseMovies(response).
        throw new UnsupportedOperationException("TmdbService.searchMovies not implemented");
    }

    /**
     * Returns TMDB's single best match for a title, or {@code null} if there is
     * none. The chatbot uses this to turn a recommended title like
     * "Blade Runner 2049" into a real movie the user can save.
     *
     * <h2>TODO — implement</h2>
     * Reuse {@link #searchMovies(String)}; do not duplicate the HTTP code. Call
     * it, then return the first element of the list — or {@code null} when the
     * list is empty. Never call {@code get(0)} without checking first, or you get
     * an {@code IndexOutOfBoundsException}.
     */
    public Movie searchOne(String title) {
        // TODO: search for the title and return the first result, or null.
        throw new UnsupportedOperationException("TmdbService.searchOne not implemented");
    }

    /**
     * Converts TMDB's raw JSON into a list of {@link Movie} objects.
     *
     * <p>This method is {@code private} on purpose: it is an internal helper, not
     * something controllers should call. Keeping the JSON details in here means the
     * rest of the app only ever deals with clean {@code Movie} objects.
     *
     * <h2>TODO — implement</h2>
     * <ol>
     *   <li>Create an empty {@code List<Movie>} to collect results into.</li>
     *   <li>Wrap the parsing in {@code try/catch}, because malformed JSON throws.</li>
     *   <li>{@code JsonNode root = objectMapper.readTree(json);} then
     *       {@code JsonNode results = root.get("results");}
     *       (import {@code tools.jackson.databind.JsonNode}).</li>
     *   <li>Check {@code results != null && results.isArray()} before looping —
     *       an error response from TMDB has no {@code results} field at all, and
     *       calling a method on null would crash with a
     *       {@code NullPointerException}.</li>
     *   <li>For each {@code JsonNode node} in {@code results}, create a
     *       {@code new Movie()} and fill it with the setters you wrote:
     *       <pre>
     *   movie.setId(node.get("id").asInt());
     *   movie.setTitle(node.has("title") ? node.get("title").asString() : "");
     *   ... same pattern for overview, vote_average (asDouble),
     *       release_date (asString) and poster_path ...
     *       </pre>
     *       The {@code node.has("...") ? ... : default} check matters: not every
     *       movie in TMDB has a poster or a release date.</li>
     *   <li>Add each movie to the list, and return the list.</li>
     *   <li>In the {@code catch}, throw
     *       {@code new RuntimeException("Failed to parse TMDB response", e)}.
     *       Passing {@code e} as the cause keeps the original stack trace, which
     *       you will want when debugging.</li>
     * </ol>
     */
    private List<Movie> parseMovies(String json) {
        // TODO: read the "results" array and build one Movie per element.
        throw new UnsupportedOperationException("TmdbService.parseMovies not implemented");
    }
}
