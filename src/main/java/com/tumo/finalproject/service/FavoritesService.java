package com.tumo.finalproject.service;

import com.tumo.finalproject.model.FavoriteMedia;
import com.tumo.finalproject.model.NasaMedia;
import com.tumo.finalproject.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Saving, listing and removing a user's favorite NASA photos and videos.
 *
 * <p>This class sits between the controller and the database and does one job the
 * others cannot: it <b>translates between two shapes of the same idea</b>.
 * {@link FavoriteMedia} is the database shape (has a primary key, has a username);
 * {@link NasaMedia} is the shape the browser understands. The controller only ever
 * sees {@code NasaMedia}, the repository only ever sees {@code FavoriteMedia}, and
 * the two conversion helpers at the bottom of this file are the border between
 * them.
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
     * Every item this user has favorited.
     *
     * <h2>TODO — implement</h2>
     * Fetch the user's rows, convert each one, and return the list. A stream makes
     * this a single expression:
     * <pre>
     *   return favoriteRepository.findByUsername(username).stream()
     *           .map(this::toMedia)
     *           .toList();
     * </pre>
     * {@code this::toMedia} is a <b>method reference</b> — shorthand for the lambda
     * {@code f -> toMedia(f)}. Read it as "for each row, run it through toMedia".
     * Returns an empty list for a user with no favorites, which is exactly what the
     * frontend expects.
     */
    public List<NasaMedia> getFavorites(String username) {
        return favoriteRepository.findByUsername(username).stream()
                .map(this::toMedia)
                .toList();
    }

    /**
     * Adds an item to this user's favorites and returns it.
     *
     * <h2>TODO — implement</h2>
     * <ol>
     *   <li>First check {@code favoriteRepository.existsByUsernameAndNasaId(username,
     *       media.getId())}. Only save when it is <i>not</i> already there.</li>
     *   <li>Save with {@code favoriteRepository.save(toEntity(username, media));}</li>
     *   <li>Return {@code media} either way.</li>
     * </ol>
     *
     * <p>Why the check, when the database already has a unique constraint? Because
     * that constraint would throw an exception the user would see as a 500 error.
     * Checking first lets clicking the heart twice be harmless — the operation is
     * <b>idempotent</b>: doing it again changes nothing and still succeeds.
     */
    public NasaMedia addFavorite(String username, NasaMedia media) {
        if (!favoriteRepository.existsByUsernameAndNasaId(username, media.getId())) {
            favoriteRepository.save(toEntity(username, media));
        }
        return media;
    }

    /**
     * Removes an item from this user's favorites.
     *
     * <h2>TODO — implement</h2>
     * <pre>
     *   return favoriteRepository.deleteByUsernameAndNasaId(username, nasaId) &gt; 0;
     * </pre>
     * The repository returns how many rows it deleted, so {@code > 0} means "there
     * was something to delete". {@code MediaController} turns {@code true} into HTTP
     * 200 and {@code false} into 404.
     *
     * <h2>TODO — and add one annotation</h2>
     * Put {@code @Transactional} on this method (import
     * {@code org.springframework.transaction.annotation.Transactional}). Spring Data
     * refuses to run a derived {@code delete...} query outside a transaction, so
     * without it you get an error at runtime. A transaction means the whole
     * operation either completes or is rolled back entirely — never half-done.
     *
     * @param nasaId the item's NASA id, not the database primary key
     * @return true if a favorite was actually removed
     */
    @Transactional
    public boolean removeFavorite(String username, String nasaId) {
        return favoriteRepository.deleteByUsernameAndNasaId(username, nasaId) > 0;
    }

    /**
     * Database row → API object.
     *
     * <h2>TODO — implement</h2>
     * Build a {@code new NasaMedia(...)} from the entity's getters, passing
     * {@code f.getNasaId()} as the NasaMedia's {@code id}, then title, description,
     * mediaType, dateCreated and thumbnailUrl in that order.
     *
     * <p>Read that first argument carefully. The browser needs the <b>NASA</b> id,
     * because that is what it sends back when the user clicks remove — not
     * {@code f.getId()}, which is only meaningful inside our own table. Mixing these
     * two up is the classic bug in this project.
     */
    private NasaMedia toMedia(FavoriteMedia f) {
        return new NasaMedia(f.getNasaId(), f.getTitle(), f.getDescription(),
                f.getMediaType(), f.getDateCreated(), f.getThumbnailUrl());
    }

    /**
     * API object → database row.
     *
     * <h2>TODO — implement</h2>
     * Return {@code new FavoriteMedia(username, m.getId(), m.getTitle(), ...)},
     * filling all seven constructor arguments. Here {@code m.getId()} — the NASA id —
     * becomes the entity's {@code nasaId}. You do not set the entity's {@code id}:
     * the database generates it when the row is inserted.
     */
    private FavoriteMedia toEntity(String username, NasaMedia m) {
        return new FavoriteMedia(username, m.getId(), m.getTitle(), m.getDescription(),
                m.getMediaType(), m.getDateCreated(), m.getThumbnailUrl());
    }
}
