package com.tumo.finalproject.controller;

import com.tumo.finalproject.model.Movie;
import com.tumo.finalproject.service.MovieChatService;
import com.tumo.finalproject.service.TmdbService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * The chatbot endpoint: {@code POST /api/chat}.
 *
 * <p>This is the most interesting endpoint in the project because it combines two
 * services. The language model is good at conversation but only gives you
 * <i>titles</i> — plain text, no poster, no TMDB id, so nothing the user could save.
 * TMDB has all of that but cannot hold a conversation. So this controller asks the
 * model what to recommend, then looks each title up on TMDB to get a real, saveable
 * {@link Movie}.
 *
 * <p>Leave this one until {@link MovieChatService} and {@link TmdbService} both work.
 *
 * <p>The response shape {@code js/app.js} expects:
 * <pre>
 *   { "response": "Sure! You might enjoy ...", "recommendations": [ {movie}, {movie} ] }
 * </pre>
 * Keep both key names exactly as written — the frontend reads {@code data.response}
 * and {@code data.recommendations}.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final MovieChatService movieChatService;
    private final TmdbService tmdbService;

    public ChatController(MovieChatService movieChatService, TmdbService tmdbService) {
        this.movieChatService = movieChatService;
        this.tmdbService = tmdbService;
    }

    /**
     * Answers a chat message with friendly text plus real, saveable movies.
     *
     * <p>The request body is {@code {"message": "something funny for a rainy day"}}.
     * Taking it as a {@code Map<String, String>} works fine for a single field; a
     * record like {@code AuthRequest} would be the tidier choice as it grows.
     *
     * <h2>TODO — implement in four steps</h2>
     *
     * <p><b>Step 1: validate.</b> Read {@code request.get("message")}. If it is null
     * or blank, return
     * {@code ResponseEntity.badRequest().body(Map.of("error", "Message is required"))}.
     *
     * <p><b>Step 2: ask the model.</b>
     * <pre>
     *   MovieChatService.ChatResult result = movieChatService.chat(message);
     * </pre>
     * {@code result.reply()} is the text to show; {@code result.titles()} is the list
     * of movie titles it recommended.
     *
     * <p><b>Step 3: turn each title into a real movie.</b> Loop over
     * {@code result.titles()} and for each one call {@code tmdbService.searchOne(title)},
     * collecting the results into a {@code List<Movie>}. Three things to get right:
     * <ul>
     *   <li><b>Skip nulls.</b> {@code searchOne} returns null when TMDB has never
     *       heard of the title — models do occasionally invent films.</li>
     *   <li><b>Skip duplicates.</b> Keep a {@code Set<Integer> seenIds} and only add a
     *       movie when {@code seenIds.add(movie.getId())} returns true. ({@code add}
     *       returns false if the value was already in the set — a neat one-line
     *       duplicate check.) Two different titles can resolve to the same film.</li>
     *   <li><b>Do not let one bad title kill the whole reply.</b> Wrap the
     *       {@code searchOne} call in {@code try/catch (Exception ignored)} and carry
     *       on with the next title. The user still gets the conversation even if a
     *       lookup fails.</li>
     * </ul>
     *
     * <p><b>Step 4: return both parts.</b>
     * <pre>
     *   return ResponseEntity.ok(Map.of(
     *           "response", result.reply(),
     *           "recommendations", recommendations
     *   ));
     * </pre>
     * (Imports you will need: {@code java.util.List}, {@code java.util.ArrayList},
     * {@code java.util.Set}, {@code java.util.HashSet}.)
     *
     * <p>Careful: {@code Map.of} rejects null values with a
     * {@code NullPointerException}, so make sure {@code result.reply()} is never null.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        // TODO: validate the message, ask the chat service, resolve titles via TMDB, return both.
        throw new UnsupportedOperationException("ChatController.chat not implemented");
    }
}
