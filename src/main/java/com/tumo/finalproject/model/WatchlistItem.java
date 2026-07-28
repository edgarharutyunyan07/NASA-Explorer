package com.tumo.finalproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * A NASA photo or video one user saved to their "look at later" list — one row
 * in the {@code watchlist} table.
 *
 * <p>This is deliberately almost identical to {@link FavoriteMedia}: same fields,
 * different table, so the two lists stay independent.
 */
@Entity
@Table(name = "watchlist",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "nasaId"}))
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String nasaId;

    private String title;

    @Column(length = 2000)
    private String description;

    private String mediaType;
    private String dateCreated;
    private String thumbnailUrl;

    public WatchlistItem() {
    }

    public WatchlistItem(String username, String nasaId, String title, String description,
                          String mediaType, String dateCreated, String thumbnailUrl) {
        this.username = username;
        this.nasaId = nasaId;
        this.title = title;
        this.description = description;
        this.mediaType = mediaType;
        this.dateCreated = dateCreated;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNasaId() {
        return nasaId;
    }

    public void setNasaId(String nasaId) {
        this.nasaId = nasaId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
