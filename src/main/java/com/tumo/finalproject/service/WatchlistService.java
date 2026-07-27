package com.tumo.finalproject.service;

import com.tumo.finalproject.model.Movie;
import com.tumo.finalproject.model.WatchlistItem;
import com.tumo.finalproject.repository.WatchlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The "watch later" list: same operations as {@link FavoritesService}, stored in a
 * different table so the two lists stay independent.
 *
 * <p>Do {@code FavoritesService} first. This class is the same shape, so it is a
 * good check on whether the pattern actually clicked — try writing it without
 * looking back.
 */
@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;

    public WatchlistService(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    /**
     * Every movie on this user's watchlist.
     *
     * <h2>TODO — implement</h2>
     * Load {@code watchlistRepository.findByUsername(username)}, convert each row
     * with {@link #toMovie(WatchlistItem)}, and return the list.
     */
    public List<Movie> getWatchlist(String username) {
        // TODO: load this user's rows and convert each to a Movie.
        throw new UnsupportedOperationException("WatchlistService.getWatchlist not implemented");
    }

    /**
     * Adds a movie to this user's watchlist and returns it.
     *
     * <h2>TODO — implement</h2>
     * Save {@code toEntity(username, movie)} unless
     * {@code existsByUsernameAndTmdbId(username, movie.getId())} is already true,
     * then return {@code movie}. Adding the same movie twice must not fail.
     */
    public Movie addToWatchlist(String username, Movie movie) {
        // TODO: save the movie for this user unless it is already saved, then return it.
        throw new UnsupportedOperationException("WatchlistService.addToWatchlist not implemented");
    }

    /**
     * Removes a movie from this user's watchlist.
     *
     * <h2>TODO — implement</h2>
     * Return {@code watchlistRepository.deleteByUsernameAndTmdbId(username, tmdbId) > 0}
     * and annotate the method with {@code @Transactional} (import
     * {@code org.springframework.transaction.annotation.Transactional}) — derived
     * delete queries require it.
     *
     * @return true if an entry was actually removed
     */
    public boolean removeFromWatchlist(String username, int tmdbId) {
        // TODO: delete the row and report whether anything was removed.
        throw new UnsupportedOperationException("WatchlistService.removeFromWatchlist not implemented");
    }

    /**
     * Database row → API object.
     *
     * <h2>TODO — implement</h2>
     * Build a {@code new Movie(...)} from the entity, passing {@code w.getTmdbId()}
     * as the Movie's id — not {@code w.getId()}.
     */
    private Movie toMovie(WatchlistItem w) {
        // TODO: convert the entity into a Movie (use getTmdbId() as the Movie id).
        throw new UnsupportedOperationException("WatchlistService.toMovie not implemented");
    }

    /**
     * API object → database row.
     *
     * <h2>TODO — implement</h2>
     * Return a {@code new WatchlistItem(username, m.getId(), ...)} with all seven
     * constructor arguments filled in.
     */
    private WatchlistItem toEntity(String username, Movie m) {
        // TODO: convert the Movie into a WatchlistItem owned by this username.
        throw new UnsupportedOperationException("WatchlistService.toEntity not implemented");
    }
}
