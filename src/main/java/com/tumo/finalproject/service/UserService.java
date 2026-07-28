package com.tumo.finalproject.service;

import com.tumo.finalproject.model.User;
import com.tumo.finalproject.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Registration and login.
 *
 * <p><b>Rule number one of authentication: never store passwords.</b> Store a
 * hash. A hash is a one-way transformation — easy to compute forwards, effectively
 * impossible to reverse — so if somebody steals your database they still cannot log
 * in as your users. To check a password later you hash the attempt and compare the
 * hashes; you never decrypt anything, because there is nothing to decrypt.
 *
 * <p>{@link BCryptPasswordEncoder} does this properly for you. Two details worth
 * knowing:
 * <ul>
 *   <li>It mixes in a random <b>salt</b> per password, so two users with the same
 *       password get different hashes and an attacker cannot spot the pattern.</li>
 *   <li>It is deliberately <b>slow</b>, which makes brute-force guessing expensive.
 *       This is why you must not use fast hashes like MD5 or SHA-1 for passwords.</li>
 * </ul>
 * Because the salt is stored inside the hash string, you must compare with
 * {@code passwordEncoder.matches(raw, storedHash)} — comparing with
 * {@code equals()} will never match.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    /**
     * Created once and reused. It holds no per-user state, so a single instance is
     * safe to share.
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Spring injects the repository — remember, you never implemented
     * {@link UserRepository}; Spring Data generated it at startup.
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user with a hashed password.
     *
     * <h2>TODO — implement, validating before you save</h2>
     * <ol>
     *   <li>Reject a {@code null} or blank username:
     *       {@code throw new IllegalArgumentException("Username is required");}</li>
     *   <li>Reject a {@code null} password, or one shorter than 4 characters:
     *       {@code "Password must be at least 4 characters"}. (Four is low for real
     *       life — it just keeps classroom testing quick.)</li>
     *   <li>{@code String trimmed = username.trim();} — so " ana" and "ana" cannot
     *       become two different accounts.</li>
     *   <li>If {@code userRepository.existsByUsername(trimmed)}, throw
     *       {@code "Username already taken"}.</li>
     *   <li>Save and return the new user:
     *       <pre>
     *   return userRepository.save(new User(trimmed, passwordEncoder.encode(rawPassword)));
     *       </pre>
     *       Note {@code encode(rawPassword)} — the raw password is used here and then
     *       forgotten. It is never stored and never logged.</li>
     * </ol>
     *
     * <p>Throwing {@code IllegalArgumentException} with a readable message is
     * intentional: {@code AuthController} catches it and turns the message into an
     * HTTP 400 the frontend displays to the user.
     *
     * @throws IllegalArgumentException if the input is invalid or the name is taken
     */
    public User register(String username, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (rawPassword == null || rawPassword.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
        String trimmed = username.trim();
        if (userRepository.existsByUsername(trimmed)) {
            throw new IllegalArgumentException("Username already taken");
        }
        return userRepository.save(new User(trimmed, passwordEncoder.encode(rawPassword)));
    }

    /**
     * Returns {@code true} when the username exists and the password matches the
     * stored hash.
     *
     * <h2>TODO — implement</h2>
     * <ol>
     *   <li>Return {@code false} immediately if either argument is {@code null}.</li>
     *   <li>Look the user up with {@code userRepository.findByUsername(username.trim())}.
     *       That returns an {@code Optional<User>}, which you can handle without an
     *       {@code if} at all:
     *       <pre>
     *   return userRepository.findByUsername(username.trim())
     *           .map(user -&gt; passwordEncoder.matches(rawPassword, user.getPasswordHash()))
     *           .orElse(false);
     *       </pre>
     *       {@code map} runs only when a user was found; {@code orElse(false)} covers
     *       the "no such user" case.</li>
     * </ol>
     *
     * <p>Notice this returns a plain {@code boolean} and the error message in
     * {@code AuthController} says "Invalid username or password" without saying
     * which. That is deliberate: telling an attacker "that username exists, wrong
     * password" hands them a list of valid accounts.
     */
    public boolean authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return false;
        }
        return userRepository.findByUsername(username.trim())
                .map(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()))
                .orElse(false);
    }
}
