package com.tumo.finalproject.repository;

import com.tumo.finalproject.model.FavoriteMovie;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for {@link FavoriteMovie}.
 *
 * <p>Extending {@code JpaRepository<FavoriteMovie, Long>} already gives you
 * {@code save}, {@code findAll}, {@code deleteById} and friends. What it cannot
 * guess is how <i>we</i> want to slice the data — by user, and by TMDB id — so we
 * declare those as derived query methods. Spring reads the method name and writes
 * the SQL; the names must match the field names in {@link FavoriteMovie}.
 *
 * <h2>TODO — declare these three methods (no bodies, just signatures)</h2>
 * <pre>
 *   List&lt;FavoriteMovie&gt; findByUsername(String username);
 *       Every favorite belonging to one user. Import java.util.List.
 *
 *   boolean existsByUsernameAndTmdbId(String username, int tmdbId);
 *       Has this user already favorited this movie? Note how "And" in the name
 *       becomes AND in the SQL WHERE clause.
 *
 *   long deleteByUsernameAndTmdbId(String username, int tmdbId);
 *       Delete that user's favorite and return how many rows were removed —
 *       0 means there was nothing to delete, which is how the service layer
 *       decides between HTTP 200 and 404.
 * </pre>
 * Add them only after the fields exist in {@link FavoriteMovie}, or the app fails
 * at startup with "No property 'username' found".
 */
public interface FavoriteRepository extends JpaRepository<FavoriteMovie, Long> {

    // TODO: declare findByUsername, existsByUsernameAndTmdbId and deleteByUsernameAndTmdbId here.
}
