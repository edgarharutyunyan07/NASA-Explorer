package com.tumo.finalproject.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A NASA photo or video as the API sends it to the browser.
 *
 * <p>This is a plain data class (a DTO) — NOT a database entity. It carries NASA
 * Image and Video Library search results, favorites/watchlist responses, and the
 * media the chatbot recommends.
 */
public class NasaMedia {

    private String id;
    private String title;
    private String description;

    @JsonProperty("media_type")
    private String mediaType;

    @JsonProperty("date_created")
    private String dateCreated;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    public NasaMedia() {
    }

    public NasaMedia(String id, String title, String description, String mediaType,
                      String dateCreated, String thumbnailUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.mediaType = mediaType;
        this.dateCreated = dateCreated;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
