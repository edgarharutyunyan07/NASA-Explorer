package com.tumo.finalproject.repository;

import com.tumo.finalproject.model.FavoriteMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database access for {@link FavoriteMedia}.
 *
 * <p>Extending {@code JpaRepository<FavoriteMedia, Long>} already gives you
 * {@code save}, {@code findAll}, {@code deleteById} and friends. What it cannot
 * guess is how <i>we</i> want to slice the data — by user, and by NASA id — so we
 * declare those as derived query methods. Spring reads the method name and writes
 * the SQL; the names must match the field names in {@link FavoriteMedia}.
 *
 * <h2>TODO — declare these three methods (no bodies, just signatures)</h2>
 * <pre>
 *   List&lt;FavoriteMedia&gt; findByUsername(String username);
 *       Every favorite belonging to one user. Import java.util.List.
 *
 *   boolean existsByUsernameAndNasaId(String username, String nasaId);
 *       Has this user already favorited this item? Note how "And" in the name
 *       becomes AND in the SQL WHERE clause.
 *
 *   long deleteByUsernameAndNasaId(String username, String nasaId);
 *       Delete that user's favorite and return how many rows were removed —
 *       0 means there was nothing to delete, which is how the service layer
 *       decides between HTTP 200 and 404.
 * </pre>
 * Add them only after the fields exist in {@link FavoriteMedia}, or the app fails
 * at startup with "No property 'username' found".
 */
public interface FavoriteRepository extends JpaRepository<FavoriteMedia, Long> {

    List<FavoriteMedia> findByUsername(String username);

    boolean existsByUsernameAndNasaId(String username, String nasaId);

    long deleteByUsernameAndNasaId(String username, String nasaId);
}
