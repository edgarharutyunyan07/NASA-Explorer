package com.tumo.finalproject.controller;

import com.tumo.finalproject.model.NasaMedia;
import com.tumo.finalproject.service.NasaChatService;
import com.tumo.finalproject.service.NasaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The chatbot endpoint: {@code POST /api/chat}.
 *
 * <p>This is the most interesting endpoint in the project because it combines two
 * services. The language model is good at conversation but only gives you
 * <i>topics</i> — plain text, no thumbnail, no NASA id, so nothing the user could
 * save. NASA's library has all of that but cannot hold a conversation. So this
 * controller asks the model what to recommend, then looks each topic up in the
 * NASA library to get a real, saveable {@link NasaMedia}.
 *
 * <p>Leave this one until {@link NasaChatService} and {@link NasaService} both work.
 *
 * <p>The response shape {@code js/app.js} expects:
 * <pre>
 *   { "response": "Sure! You might enjoy ...", "recommendations": [ {media}, {media} ] }
 * </pre>
 * Keep both key names exactly as written — the frontend reads {@code data.response}
 * and {@code data.recommendations}.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final NasaChatService nasaChatService;
    private final NasaService nasaService;

    public ChatController(NasaChatService nasaChatService, NasaService nasaService) {
        this.nasaChatService = nasaChatService;
        this.nasaService = nasaService;
    }

    /**
     * Answers a chat message with friendly text plus real, saveable media.
     *
     * <p>The request body is {@code {"message": "something about the moon landing"}}.
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
     *   NasaChatService.ChatResult result = nasaChatService.chat(message);
     * </pre>
     * {@code result.reply()} is the text to show; {@code result.topics()} is the list
     * of search topics it recommended.
     *
     * <p><b>Step 3: turn each topic into a real item.</b> Loop over
     * {@code result.topics()} and for each one call {@code nasaService.searchOne(topic)},
     * collecting the results into a {@code List<NasaMedia>}. Three things to get right:
     * <ul>
     *   <li><b>Skip nulls.</b> {@code searchOne} returns null when NASA has nothing
     *       matching the topic — models do occasionally invent things.</li>
     *   <li><b>Skip duplicates.</b> Keep a {@code Set<String> seenIds} and only add an
     *       item when {@code seenIds.add(media.getId())} returns true. ({@code add}
     *       returns false if the value was already in the set — a neat one-line
     *       duplicate check.) Two different topics can resolve to the same item.</li>
     *   <li><b>Do not let one bad topic kill the whole reply.</b> Wrap the
     *       {@code searchOne} call in {@code try/catch (Exception ignored)} and carry
     *       on with the next topic. The user still gets the conversation even if a
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
        String message = request.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }

        NasaChatService.ChatResult result = nasaChatService.chat(message);

        List<NasaMedia> recommendations = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        for (String topic : result.topics()) {
            try {
                NasaMedia media = nasaService.searchOne(topic);
                if (media != null && seenIds.add(media.getId())) {
                    recommendations.add(media);
                }
            } catch (Exception ignored) {
                // one bad topic should not break the whole reply
            }
        }

        return ResponseEntity.ok(Map.of(
                "response", result.reply(),
                "recommendations", recommendations
        ));
    }
}
