package com.tumo.finalproject.repository;

import com.tumo.finalproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Database access for {@link User}.
 *
 * <p>Notice this is an {@code interface} with no implementation, and there is no
 * {@code @Repository} annotation or {@code class} anywhere. That is not a mistake:
 * Spring Data JPA generates the implementing class at startup. By extending
 * {@code JpaRepository<User, Long>} you already get {@code save}, {@code findById},
 * {@code findAll}, {@code deleteById}, {@code count} and more, for free.
 *
 * <p>The trick you are about to use is <b>derived query methods</b>: declare a
 * method whose <i>name</i> describes the query, and Spring writes the SQL. The
 * name must match your entity's field names exactly — {@code findByUsername}
 * works only because {@link User} has a field called {@code username}.
 *
 * <h2>TODO — declare these two methods (no bodies, just signatures)</h2>
 * <pre>
 *   Optional&lt;User&gt; findByUsername(String username);
 *       Look up one user by name. Returns Optional.empty() when nobody matches,
 *       which is safer than returning null. Import java.util.Optional.
 *
 *   boolean existsByUsername(String username);
 *       True if that username is already taken. Cheaper than loading the whole
 *       row just to check.
 * </pre>
 * Add them only after you have declared the {@code username} field in
 * {@link User} — otherwise the app fails at startup with
 * "No property 'username' found".
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
