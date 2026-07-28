package com.tumo.finalproject.repository;

import com.tumo.finalproject.model.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database access for {@link WatchlistItem}. The mirror image of
 * {@link FavoriteRepository}, pointing at a different entity and table.
 *
 * <h2>TODO — declare these three methods (no bodies, just signatures)</h2>
 * <pre>
 *   List&lt;WatchlistItem&gt; findByUsername(String username);
 *       Every watchlist entry belonging to one user. Import java.util.List.
 *
 *   boolean existsByUsernameAndNasaId(String username, String nasaId);
 *       Is this item already on that user's watchlist?
 *
 *   long deleteByUsernameAndNasaId(String username, String nasaId);
 *       Delete it, returning the number of rows removed (0 = nothing matched).
 * </pre>
 * Add them only after the fields exist in {@link WatchlistItem}, or the app fails
 * at startup with "No property 'username' found".
 */
public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {

    List<WatchlistItem> findByUsername(String username);

    boolean existsByUsernameAndNasaId(String username, String nasaId);

    long deleteByUsernameAndNasaId(String username, String nasaId);
}
