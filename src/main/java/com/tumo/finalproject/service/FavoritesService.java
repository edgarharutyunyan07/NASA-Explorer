package com.tumo.finalproject.service;

import com.tumo.finalproject.model.FavoriteMovie;
import com.tumo.finalproject.model.Movie;
import com.tumo.finalproject.repository.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Saving, listing and removing a user's favorite movies.
 *
 * <p>This class sits between the controller and the database and does one job the
 * others cannot: it <b>translates between two shapes of the same idea</b>.
 * {@link FavoriteMovie} is the database shape (has a primary key, has a username);
 * {@link Movie} is the shape the browser understands. The controller only ever sees
 * {@code Movie}, the repository only ever sees {@code FavoriteMovie}, and the two
 * conversion helpers at the bottom of this file are the border between them.
 *
 * <p>That separation is why you could swap H2 for PostgreSQL, or add a column to the
 * table, without touching the controller or a single line of JavaScript.
 */
@Service
public class FavoritesService {

    private final FavoriteRepository favoriteRepository;

    public FavoritesService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * Every movie this user has favorited.
     *
     * <h2>TODO — implement</h2>
     * Fetch the user's rows, convert each one, and return the list. A stream makes
     * this a single expression:
     * <pre>
     *   return favoriteRepository.findByUsername(username).stream()
     *           .map(this::toMovie)
     *           .toList();
     * </pre>
     * {@code this::toMovie} is a <b>method reference</b> — shorthand for the lambda
     * {@code f -> toMovie(f)}. Read it as "for each row, run it through toMovie".
     * Returns an empty list for a user with no favorites, which is exactly what the
     * frontend expects.
     */
    public List<Movie> getFavorites(String username) {
        // TODO: load this user's rows and convert each to a Movie.
        throw new UnsupportedOperationException("FavoritesService.getFavorites not implemented");
    }

    /**
     * Adds a movie to this user's favorites and returns it.
     *
     * <h2>TODO — implement</h2>
     * <ol>
     *   <li>First check {@code favoriteRepository.existsByUsernameAndTmdbId(username,
     *       movie.getId())}. Only save when it is <i>not</i> already there.</li>
     *   <li>Save with {@code favoriteRepository.save(toEntity(username, movie));}</li>
     *   <li>Return {@code movie} either way.</li>
     * </ol>
     *
     * <p>Why the check, when the database already has a unique constraint? Because
     * that constraint would throw an exception the user would see as a 500 error.
     * Checking first lets clicking the heart twice be harmless — the operation is
     * <b>idempotent</b>: doing it again changes nothing and still succeeds.
     */
    public Movie addFavorite(String username, Movie movie) {
        // TODO: save the movie for this user unless it is already saved, then return it.
        throw new UnsupportedOperationException("FavoritesService.addFavorite not implemented");
    }

    /**
     * Removes a movie from this user's favorites.
     *
     * <h2>TODO — implement</h2>
     * <pre>
     *   return favoriteRepository.deleteByUsernameAndTmdbId(username, tmdbId) &gt; 0;
     * </pre>
     * The repository returns how many rows it deleted, so {@code > 0} means "there
     * was something to delete". {@code MovieController} turns {@code true} into HTTP
     * 200 and {@code false} into 404.
     *
     * <h2>TODO — and add one annotation</h2>
     * Put {@code @Transactional} on this method (import
     * {@code org.springframework.transaction.annotation.Transactional}). Spring Data
     * refuses to run a derived {@code delete...} query outside a transaction, so
     * without it you get an error at runtime. A transaction means the whole
     * operation either completes or is rolled back entirely — never half-done.
     *
     * @param tmdbId the movie's TMDB id, not the database primary key
     * @return true if a favorite was actually removed
     */
    public boolean removeFavorite(String username, int tmdbId) {
        // TODO: delete the row and report whether anything was removed.
        throw new UnsupportedOperationException("FavoritesService.removeFavorite not implemented");
    }

    /**
     * Database row → API object.
     *
     * <h2>TODO — implement</h2>
     * Build a {@code new Movie(...)} from the entity's getters, passing
     * {@code f.getTmdbId()} as the Movie's {@code id}, then title, overview,
     * voteAverage, releaseDate and posterPath in that order.
     *
     * <p>Read that first argument carefully. The browser needs the <b>TMDB</b> id,
     * because that is what it sends back when the user clicks remove — not
     * {@code f.getId()}, which is only meaningful inside our own table. Mixing these
     * two up is the classic bug in this project.
     */
    private Movie toMovie(FavoriteMovie f) {
        // TODO: convert the entity into a Movie (use getTmdbId() as the Movie id).
        throw new UnsupportedOperationException("FavoritesService.toMovie not implemented");
    }

    /**
     * API object → database row.
     *
     * <h2>TODO — implement</h2>
     * Return {@code new FavoriteMovie(username, m.getId(), m.getTitle(), ...)},
     * filling all seven constructor arguments. Here {@code m.getId()} — the TMDB id —
     * becomes the entity's {@code tmdbId}. You do not set the entity's {@code id}:
     * the database generates it when the row is inserted.
     */
    private FavoriteMovie toEntity(String username, Movie m) {
        // TODO: convert the Movie into a FavoriteMovie owned by this username.
        throw new UnsupportedOperationException("FavoritesService.toEntity not implemented");
    }
}
