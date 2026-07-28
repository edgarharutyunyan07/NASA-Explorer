package com.tumo.finalproject.service;

import com.tumo.finalproject.model.NasaMedia;
import com.tumo.finalproject.model.WatchlistItem;
import com.tumo.finalproject.repository.WatchlistRepository;
import org.springframework.stereotype.Service;

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
        // TODO: load this user's rows and convert each to a NasaMedia.
        throw new UnsupportedOperationException("WatchlistService.getWatchlist not implemented");
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
        // TODO: save the item for this user unless it is already saved, then return it.
        throw new UnsupportedOperationException("WatchlistService.addToWatchlist not implemented");
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
    public boolean removeFromWatchlist(String username, String nasaId) {
        // TODO: delete the row and report whether anything was removed.
        throw new UnsupportedOperationException("WatchlistService.removeFromWatchlist not implemented");
    }

    /**
     * Database row → API object.
     *
     * <h2>TODO — implement</h2>
     * Build a {@code new NasaMedia(...)} from the entity, passing
     * {@code w.getNasaId()} as the NasaMedia's id — not {@code w.getId()}.
     */
    private NasaMedia toMedia(WatchlistItem w) {
        // TODO: convert the entity into a NasaMedia (use getNasaId() as the NasaMedia id).
        throw new UnsupportedOperationException("WatchlistService.toMedia not implemented");
    }

    /**
     * API object → database row.
     *
     * <h2>TODO — implement</h2>
     * Return a {@code new WatchlistItem(username, m.getId(), ...)} with all seven
     * constructor arguments filled in.
     */
    private WatchlistItem toEntity(String username, NasaMedia m) {
        // TODO: convert the NasaMedia into a WatchlistItem owned by this username.
        throw new UnsupportedOperationException("WatchlistService.toEntity not implemented");
    }
}
