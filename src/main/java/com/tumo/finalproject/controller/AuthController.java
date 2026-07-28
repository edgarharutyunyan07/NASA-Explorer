package com.tumo.finalproject.controller;

import com.tumo.finalproject.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Register, log in, log out, and "who am I?".
 *
 * <p>Everything here revolves around one line — {@code session.setAttribute("username", ...)}.
 * That is what "being logged in" actually means in this app: a name stored in the
 * server-side session, which the browser's session cookie points at. Log out and
 * that stored name disappears, so the next request is anonymous again.
 *
 * <p>Note what this class does <b>not</b> do: it never touches the database and never
 * hashes anything. {@link UserService} owns that. The controller's job is HTTP —
 * status codes, request bodies, and the session.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * {@code POST /api/auth/register} — create an account and log straight in.
     *
     * <h2>TODO — implement</h2>
     * <ol>
     *   <li>Wrap the work in {@code try/catch (IllegalArgumentException e)}, because
     *       {@code UserService.register} throws that for a blank name, a short
     *       password, or a name already taken.</li>
     *   <li>In the {@code try}: call {@code userService.register(req.username(), req.password())},
     *       then log the new user in with
     *       {@code session.setAttribute("username", req.username().trim())} and return
     *       {@code ResponseEntity.ok(Map.of("username", req.username().trim()))}.
     *       (Import {@code java.util.Map}.)</li>
     *   <li>In the {@code catch}: return
     *       {@code ResponseEntity.badRequest().body(Map.of("error", e.getMessage()))} —
     *       HTTP 400 with the message from the exception, which {@code js/app.js}
     *       shows to the user. This is how a validation rule you wrote in the service
     *       becomes red text on the screen.</li>
     * </ol>
     *
     * <p>Note {@code .trim()} in both places: store the same cleaned-up name the
     * service stored, or the session will not match the database rows.
     *
     * <p>{@code ResponseEntity<?>} uses a wildcard because the two branches return
     * different body shapes — a username on success, an error on failure.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req, HttpSession session) {
        try {
            userService.register(req.username(), req.password());
            session.setAttribute("username", req.username().trim());
            return ResponseEntity.ok(Map.of("username", req.username().trim()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * {@code POST /api/auth/login} — check credentials and start a session.
     *
     * <h2>TODO — implement</h2>
     * <ol>
     *   <li>If {@code userService.authenticate(req.username(), req.password())} is
     *       true: store {@code req.username().trim()} in the session under
     *       {@code "username"} and return
     *       {@code ResponseEntity.ok(Map.of("username", req.username().trim()))}.</li>
     *   <li>Otherwise return {@code ResponseEntity.status(401).body(Map.of("error",
     *       "Invalid username or password"))}.</li>
     * </ol>
     *
     * <p>Keep that error message vague on purpose. "No such user" versus "wrong
     * password" tells an attacker which usernames are real — one message for both
     * cases gives nothing away.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req, HttpSession session) {
        if (userService.authenticate(req.username(), req.password())) {
            session.setAttribute("username", req.username().trim());
            return ResponseEntity.ok(Map.of("username", req.username().trim()));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
    }

    /**
     * {@code POST /api/auth/logout} — end the session.
     *
     * <h2>TODO — implement</h2>
     * <pre>
     *   session.invalidate();
     *   return ResponseEntity.ok().build();
     * </pre>
     * {@code invalidate()} throws the whole session away rather than just removing
     * the username attribute — the thorough choice, since it leaves nothing behind
     * for anyone reusing that session id.
     *
     * <p>{@code ResponseEntity<Void>} means "a status code, no body". There is
     * nothing useful to send back.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    /**
     * {@code GET /api/auth/me} — who is logged in?
     *
     * <p>The frontend calls this the moment the page loads to decide whether to show
     * "Sign in" or the user's name, because a session cookie can outlive a browser
     * refresh. Without this endpoint a logged-in user would appear logged out after
     * every reload.
     *
     * <h2>TODO — implement</h2>
     * <ol>
     *   <li>{@code String username = (String) session.getAttribute("username");}</li>
     *   <li>If it is null, return {@code ResponseEntity.status(401).build()} — not an
     *       error, just "nobody is logged in", which is exactly what {@code app.js}
     *       checks for.</li>
     *   <li>Otherwise return {@code ResponseEntity.ok(Map.of("username", username))}.</li>
     * </ol>
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(Map.of("username", username));
    }

    /**
     * The JSON body of a register or login request:
     * {@code {"username": "ana", "password": "secret"}}.
     *
     * <p>A {@code record} is ideal for this. Two lines replace a class with two
     * fields, a constructor and two getters, and Jackson knows how to fill it in from
     * JSON. Access the values as {@code req.username()} and {@code req.password()} —
     * no {@code get} prefix.
     *
     * <p>It is declared inside the controller (a <b>nested type</b>) because nothing
     * else in the app needs it. This one is written for you; leave it as it is.
     */
    public record AuthRequest(String username, String password) {
    }
}
