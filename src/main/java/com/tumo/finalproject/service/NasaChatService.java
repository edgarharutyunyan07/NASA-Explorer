package com.tumo.finalproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The AI recommendation chatbot. Sends the user's message to a Large Language
 * Model and turns the answer into (a) friendly text to display and (b) a list of
 * NASA search topics the app can look up in the image/video library.
 *
 * <p>We call <a href="https://console.groq.com">Groq</a>, which is free to start
 * and speaks the same request format as most chat APIs:
 * <pre>
 *   POST https://api.groq.com/openai/v1/chat/completions
 *   Authorization: Bearer YOUR_KEY
 *   { "model": "...", "messages": [ {"role": "...", "content": "..."} ] }
 * </pre>
 *
 * <p><b>The idea that makes this work:</b> a language model normally replies in
 * prose, which is lovely for humans and useless for code. So we <i>instruct</i> it
 * to reply as JSON with a fixed shape, and we also pass
 * {@code response_format: {"type": "json_object"}} to enforce it. Then we can read
 * the reply text and the recommended topics as separate values.
 */
@Service
public class NasaChatService {

    /**
     * Which model to use. Different models trade speed against quality; this one
     * is fast and free on Groq. Keeping it in a constant means one edit to swap it.
     */
    private static final String MODEL = "llama-3.3-70b-versatile";

    /** TODO: build this in the constructor. */
    private WebClient webClient;

    /** TODO: create this in the constructor. */
    private ObjectMapper objectMapper;

    private final String apiKey;

    /**
     * <h2>TODO — initialise the fields above</h2>
     * <pre>
     *   this.objectMapper = new ObjectMapper();
     *
     *   this.webClient = WebClient.builder()
     *           .baseUrl("https://api.groq.com/openai/v1")
     *           .defaultHeader("Content-Type", "application/json")
     *           .defaultHeader("Authorization", "Bearer " + apiKey)
     *           .build();
     * </pre>
     * A {@code defaultHeader} is sent with every request from this client, so you
     * only write the Authorization header once. Note the required {@code "Bearer "}
     * prefix — a very common source of 401 errors.
     */
    public NasaChatService(@Value("${groq.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();

        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    /**
     * The assistant's conversational reply, plus the search topics it recommended.
     *
     * <p>This is a Java {@code record}: a short way to declare an immutable data
     * carrier. The compiler generates the constructor, the accessors
     * ({@code reply()}, {@code topics()}), {@code equals}, {@code hashCode} and
     * {@code toString}. Perfect for returning two related values from one method —
     * far better than an {@code Object[]} or a {@code Map}. It is written for you;
     * leave it as it is.
     */
    public record ChatResult(String reply, List<String> topics) {
    }

    /**
     * Asks the model for space-imagery recommendations.
     *
     * <h2>TODO — implement in three steps</h2>
     *
     * <p><b>Step 1: write the system prompt.</b> A "system" message sets the rules
     * before the user speaks. Yours must state the model's job <i>and</i> the exact
     * JSON shape you expect back. Build a String saying, in your own words:
     * <pre>
     *   You are a friendly space and astronomy assistant. Recommend NASA photos
     *   or videos to look up based on the user's interests (missions, planets,
     *   astronauts, historic events, telescopes, etc). Respond ONLY with a JSON
     *   object of this exact shape: {"reply": string, "topics": [string]}.
     *   'reply' is your friendly, concise message; for each recommendation
     *   mention what it is and why it's worth seeing, and end by asking if the
     *   user would like to save any of them. 'topics' lists short search phrases
     *   for the items you mentioned (e.g. "Apollo 11 moonwalk", "Hubble Deep
     *   Field", "Perseverance rover landing") — use an empty array if none.
     * </pre>
     * Remember to escape the quotes inside a Java String literal: {@code \"reply\"}.
     * This prompt is the part worth experimenting with — change the wording, restart,
     * and watch the chatbot's personality change.
     *
     * <p><b>Step 2: send the request.</b> Assemble the body and POST it:
     * <pre>
     *   Map&lt;String, Object&gt; requestBody = Map.of(
     *           "model", MODEL,
     *           "messages", List.of(
     *                   Map.of("role", "system", "content", systemPrompt),
     *                   Map.of("role", "user",   "content", userMessage)
     *           ),
     *           "response_format", Map.of("type", "json_object")
     *   );
     *
     *   String response = webClient.post()
     *           .uri("/chat/completions")
     *           .bodyValue(requestBody)
     *           .retrieve()
     *           .bodyToMono(String.class)
     *           .block();
     * </pre>
     * (Import {@code java.util.Map}. Spring converts the Map to JSON for you.)
     *
     * <p><b>Step 3:</b> {@code return extractResult(response);}
     *
     * <h2>Stretch goal — handle failure gracefully</h2>
     * Free API keys run out of quota. Between {@code .retrieve()} and
     * {@code .bodyToMono(...)} you can intercept bad status codes and, instead of
     * letting the exception reach the user as a blank 500 page, substitute a small
     * JSON error object that {@link #extractResult(String)} knows how to turn into
     * a friendly sentence:
     * <pre>
     *   .onStatus(status -&gt; status.value() == 429,
     *             res -&gt; Mono.error(new RuntimeException("rate_limit")))
     *   .onStatus(status -&gt; status.is4xxClientError() || status.is5xxServerError(),
     *             res -&gt; Mono.error(new RuntimeException("api_error")))
     *   .bodyToMono(String.class)
     *   .onErrorResume(e -&gt; Mono.just("{\"error\":\"rate_limit\"}"))
     * </pre>
     * (Import {@code reactor.core.publisher.Mono}. HTTP 429 means "too many
     * requests".)
     *
     * @param userMessage what the user typed in the chat box
     */
    public ChatResult chat(String userMessage) {
        String systemPrompt = "You are a friendly space and astronomy assistant. Recommend NASA "
                + "photos or videos to look up based on the user's interests (missions, planets, "
                + "astronauts, historic events, telescopes, etc). Respond ONLY with a JSON object "
                + "of this exact shape: {\"reply\": string, \"topics\": [string]}. 'reply' is your "
                + "friendly, concise message; for each recommendation mention what it is and why "
                + "it's worth seeing, and end by asking if the user would like to save any of them. "
                + "'topics' lists short search phrases for the items you mentioned (e.g. \"Apollo 11 "
                + "moonwalk\", \"Hubble Deep Field\", \"Perseverance rover landing\") - use an empty "
                + "array if none.";

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "response_format", Map.of("type", "json_object")
        );

        String response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.value() == 429,
                        res -> Mono.error(new RuntimeException("rate_limit")))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        res -> Mono.error(new RuntimeException("api_error")))
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("{\"error\":\"rate_limit\"}"))
                .block();

        return extractResult(response);
    }

    /**
     * Digs the useful values out of the API's response.
     *
     * <p>You have to unwrap <b>two layers of JSON</b>, which is the confusing part.
     * The outer layer is the chat API's own envelope; the model's actual answer is a
     * <i>string</i> sitting inside it, and that string is itself JSON:
     * <pre>
     *   {
     *     "choices": [ { "message": { "content": "{\"reply\": \"...\", \"topics\": [...]}" } } ]
     *   }
     * </pre>
     *
     * <h2>TODO — implement</h2>
     * <ol>
     *   <li>Wrap everything in {@code try/catch}; the model can return text that is
     *       not valid JSON, and you do not want that to crash the app.</li>
     *   <li>{@code JsonNode root = objectMapper.readTree(json);}
     *       (import {@code tools.jackson.databind.JsonNode}).</li>
     *   <li>Read the {@code "choices"} array. If it is null, not an array, or empty,
     *       return a polite {@code new ChatResult("Sorry, I couldn't generate a
     *       response.", List.of())} instead of crashing.</li>
     *   <li>Pull out the inner text:
     *       {@code String content = choices.get(0).get("message").get("content").asString();}</li>
     *   <li>Parse that text as JSON too:
     *       {@code JsonNode parsed = objectMapper.readTree(content);}</li>
     *   <li>Take {@code parsed.get("reply").asString()} as the reply — falling back to
     *       the raw {@code content} if there is no {@code "reply"} field, so the user
     *       still sees something useful.</li>
     *   <li>Loop over {@code parsed.get("topics")} and collect every non-blank
     *       string into a {@code List<String>}.</li>
     *   <li>{@code return new ChatResult(reply, topics);}</li>
     *   <li>In the {@code catch}, return a {@code ChatResult} whose reply explains
     *       the problem and whose topic list is {@code List.of()}.</li>
     * </ol>
     *
     * <p>If you did the stretch goal above, also check near the top for
     * {@code root.has("error")} and return a friendly message such as "I'm getting
     * too many requests right now — please wait a moment and try again."
     */
    private ChatResult extractResult(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            if (root.has("error")) {
                return new ChatResult(
                        "I'm getting too many requests right now — please wait a moment and try again.",
                        List.of());
            }

            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                return new ChatResult("Sorry, I couldn't generate a response.", List.of());
            }

            String content = choices.get(0).get("message").get("content").asString();
            JsonNode parsed = objectMapper.readTree(content);

            String reply = parsed.has("reply") ? parsed.get("reply").asString() : content;

            List<String> topics = new ArrayList<>();
            JsonNode topicsNode = parsed.get("topics");
            if (topicsNode != null && topicsNode.isArray()) {
                for (JsonNode topic : topicsNode) {
                    String value = topic.asString();
                    if (value != null && !value.isBlank()) {
                        topics.add(value);
                    }
                }
            }

            return new ChatResult(reply, topics);
        } catch (Exception e) {
            return new ChatResult("Sorry, something went wrong reading the AI's response.", List.of());
        }
    }
}
