package com.tumo.finalproject.service;

import com.tumo.finalproject.model.NasaMedia;
import com.tumo.finalproject.model.WatchlistItem;
import com.tumo.finalproject.repository.WatchlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The "look at later" list: same operations as {@link FavoritesService}, stored in
 * a different table so the two lists stay independent.
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
     * Every item on this user's watchlist.
     *
     * <h2>TODO — implement</h2>
     * Load {@code watchlistRepository.findByUsername(username)}, convert each row
     * with {@link #toMedia(WatchlistItem)}, and return the list.
     */
    public List<NasaMedia> getWatchlist(String username) {
        return watchlistRepository.findByUsername(username).stream()
                .map(this::toMedia)
                .toList();
    }

    /**
     * Adds an item to this user's watchlist and returns it.
     *
     * <h2>TODO — implement</h2>
     * Save {@code toEntity(username, media)} unless
     * {@code existsByUsernameAndNasaId(username, media.getId())} is already true,
     * then return {@code media}. Adding the same item twice must not fail.
     */
    public NasaMedia addToWatchlist(String username, NasaMedia media) {
        if (!watchlistRepository.existsByUsernameAndNasaId(username, media.getId())) {
            watchlistRepository.save(toEntity(username, media));
        }
        return media;
    }

    /**
     * Removes an item from this user's watchlist.
     *
     * <h2>TODO — implement</h2>
     * Return {@code watchlistRepository.deleteByUsernameAndNasaId(username, nasaId) > 0}
     * and annotate the method with {@code @Transactional} (import
     * {@code org.springframework.transaction.annotation.Transactional}) — derived
     * delete queries require it.
     *
     * @return true if an entry was actually removed
     */
    @Transactional
    public boolean removeFromWatchlist(String username, String nasaId) {
        return watchlistRepository.deleteByUsernameAndNasaId(username, nasaId) > 0;
    }

    /**
     * Database row → API object.
     *
     * <h2>TODO — implement</h2>
     * Build a {@code new NasaMedia(...)} from the entity, passing
     * {@code w.getNasaId()} as the NasaMedia's id — not {@code w.getId()}.
     */
    private NasaMedia toMedia(WatchlistItem w) {
        return new NasaMedia(w.getNasaId(), w.getTitle(), w.getDescription(),
                w.getMediaType(), w.getDateCreated(), w.getThumbnailUrl());
    }

    /**
     * API object → database row.
     *
     * <h2>TODO — implement</h2>
     * Return a {@code new WatchlistItem(username, m.getId(), ...)} with all seven
     * constructor arguments filled in.
     */
    private WatchlistItem toEntity(String username, NasaMedia m) {
        return new WatchlistItem(username, m.getId(), m.getTitle(), m.getDescription(),
                m.getMediaType(), m.getDateCreated(), m.getThumbnailUrl());
    }
}
