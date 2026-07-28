package com.tumo.finalproject.controller;

import com.tumo.finalproject.model.NasaMedia;
import com.tumo.finalproject.service.FavoritesService;
import com.tumo.finalproject.service.NasaService;
import com.tumo.finalproject.service.WatchlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP endpoints for searching NASA media and managing the two saved lists.
 *
 * <p>A controller is the front door of the application. Its job is narrow and worth
 * remembering: <b>read the request, check the caller is allowed, delegate the real
 * work to a service, and choose the status code.</b> No business logic and no
 * database access belong here.
 *
 * <p>Two annotations do the wiring:
 * <ul>
 *   <li>{@code @RestController} — every method's return value is converted to JSON
 *       and written to the response body (rather than naming an HTML page).</li>
 *   <li>{@code @RequestMapping("/api/media")} — a prefix for every path below, so
 *       {@code @GetMapping("/search")} really answers {@code /api/media/search}.</li>
 * </ul>
 *
 * <p>The method signatures are given to you because {@code js/app.js} already calls
 * these exact URLs — change a path and the frontend breaks. Your job is the bodies.
 *
 * <p>Note the id type here is {@code String}, not {@code int}: NASA ids look like
 * {@code "as11-40-5875"}, not plain numbers.
 *
 * <h2>About {@link HttpSession}</h2>
 * HTTP is stateless: two requests know nothing about each other. A session bridges
 * that. When a user logs in, {@code AuthController} stores their name in the session
 * and the browser gets a cookie holding a session id; on every later request Spring
 * uses that cookie to hand you the same session back. So
 * {@code session.getAttribute("username")} tells you who is calling — and if it is
 * {@code null}, nobody is logged in.
 *
 * <h2>The pattern every method below follows</h2>
 * <pre>
 *   String username = currentUser(session);
 *   if (username == null) {
 *       return ResponseEntity.status(401).build();   // 401 = not authenticated
 *   }
 *   return ResponseEntity.ok(someService.doTheWork(username, ...));
 * </pre>
 * Never trust a username sent in the request body — an attacker could put anyone's
 * name there and read their list. The session is the only trustworthy source.
 *
 * <h2>Status codes you need here</h2>
 * <pre>
 *   200 OK          it worked                    ResponseEntity.ok(body)
 *   400 Bad Request the request was malformed    ResponseEntity.badRequest().build()
 *   401 Unauthorized nobody is logged in         ResponseEntity.status(401).build()
 *   404 Not Found   nothing matched to delete    ResponseEntity.notFound().build()
 * </pre>
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final NasaService nasaService;
    private final FavoritesService favoritesService;
    private final WatchlistService watchlistService;

    /**
     * Spring passes in all three services automatically because each is annotated
     * {@code @Service}. This is <b>constructor injection</b>, and the fields are
     * {@code final} so they can never be reassigned or left null.
     */
    public MediaController(NasaService nasaService, FavoritesService favoritesService,
                           WatchlistService watchlistService) {
        this.nasaService = nasaService;
        this.favoritesService = favoritesService;
        this.watchlistService = watchlistService;
    }

    /**
     * {@code GET /api/media/search?query=apollo}
     *
     * <p>{@code @RequestParam} pulls the {@code query} value out of the URL's query
     * string and hands it to you as a String.
     *
     * <h2>TODO — implement</h2>
     * <ol>
     *   <li>If {@code query} is null or blank, return
     *       {@code ResponseEntity.badRequest().build()} — do not waste an API call on
     *       an empty search.</li>
     *   <li>Otherwise return {@code ResponseEntity.ok(nasaService.searchMedia(query))}.</li>
     * </ol>
     * Note that searching does not require login: browsing is open to everyone, only
     * saving is not.
     */
    @GetMapping("/search")
    public ResponseEntity<List<NasaMedia>> searchMedia(@RequestParam String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(nasaService.searchMedia(query));
    }

    /**
     * {@code GET /api/media/favorites} — the logged-in user's favorites.
     *
     * <h2>TODO — implement</h2>
     * Follow the pattern in the class comment: get the username from the session,
     * return 401 if it is null, otherwise
     * {@code ResponseEntity.ok(favoritesService.getFavorites(username))}.
     */
    @GetMapping("/favorites")
    public ResponseEntity<List<NasaMedia>> getFavorites(HttpSession session) {
        String username = currentUser(session);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(favoritesService.getFavorites(username));
    }

    /**
     * {@code POST /api/media/favorites} — save an item.
     *
     * <p>{@code @RequestBody NasaMedia media} is where Jackson turns the JSON the
     * browser sent into a real {@code NasaMedia} object. If your
     * {@code @JsonProperty} annotations in {@link NasaMedia} are missing, the
     * snake_case fields silently arrive as null and your favorites end up with no
     * thumbnail.
     *
     * <h2>TODO — implement</h2>
     * Require a logged-in user (401 otherwise), then return
     * {@code ResponseEntity.ok(favoritesService.addFavorite(username, media))}.
     */
    @PostMapping("/favorites")
    public ResponseEntity<NasaMedia> addFavorite(@RequestBody NasaMedia media, HttpSession session) {
        String username = currentUser(session);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(favoritesService.addFavorite(username, media));
    }

    /**
     * {@code DELETE /api/media/favorites/as11-40-5875} — remove an item.
     *
     * <p>{@code @PathVariable} captures the {@code {id}} segment from the URL. Here
     * the id is a <b>NASA</b> id, matching what {@code toMedia} put in the response.
     *
     * <h2>TODO — implement</h2>
     * <ol>
     *   <li>Require a logged-in user, 401 otherwise.</li>
     *   <li>{@code boolean removed = favoritesService.removeFavorite(username, id);}</li>
     *   <li>Return {@code ResponseEntity.ok().build()} when {@code removed} is true,
     *       otherwise {@code ResponseEntity.notFound().build()}. Reporting 404 for
     *       "there was nothing to delete" is more honest than pretending it worked.</li>
     * </ol>
     */
    @DeleteMapping("/favorites/{id}")
    public ResponseEntity<Void> removeFavorite(@PathVariable String id, HttpSession session) {
        String username = currentUser(session);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        boolean removed = favoritesService.removeFavorite(username, id);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * {@code GET /api/media/watchlist} — the logged-in user's watchlist.
     *
     * <h2>TODO — implement</h2>
     * Same as {@link #getFavorites(HttpSession)}, but using {@code watchlistService}.
     */
    @GetMapping("/watchlist")
    public ResponseEntity<List<NasaMedia>> getWatchlist(HttpSession session) {
        String username = currentUser(session);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(watchlistService.getWatchlist(username));
    }

    /**
     * {@code POST /api/media/watchlist} — save an item for later.
     *
     * <h2>TODO — implement</h2>
     * Same as {@link #addFavorite(NasaMedia, HttpSession)}, calling
     * {@code watchlistService.addToWatchlist(username, media)}.
     */
    @PostMapping("/watchlist")
    public ResponseEntity<NasaMedia> addToWatchlist(@RequestBody NasaMedia media, HttpSession session) {
        String username = currentUser(session);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(watchlistService.addToWatchlist(username, media));
    }

    /**
     * {@code DELETE /api/media/watchlist/as11-40-5875} — take an item off the watchlist.
     *
     * <h2>TODO — implement</h2>
     * Same as {@link #removeFavorite(String, HttpSession)}, calling
     * {@code watchlistService.removeFromWatchlist(username, id)}.
     */
    @DeleteMapping("/watchlist/{id}")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable String id, HttpSession session) {
        String username = currentUser(session);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        boolean removed = watchlistService.removeFromWatchlist(username, id);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Who is calling, or {@code null} if nobody is logged in.
     *
     * <h2>TODO — implement (do this one first, everything above uses it)</h2>
     * <pre>
     *   return (String) session.getAttribute("username");
     * </pre>
     * The cast is needed because {@code getAttribute} returns {@code Object} — a
     * session can hold anything. The string {@code "username"} must match exactly
     * what {@code AuthController} stores, or every request will look logged out.
     *
     * <p>One tiny private helper spares you six copies of the same line, and means
     * the day you change how login works, you change it in one place.
     */
    private String currentUser(HttpSession session) {
        return (String) session.getAttribute("username");
    }
}
