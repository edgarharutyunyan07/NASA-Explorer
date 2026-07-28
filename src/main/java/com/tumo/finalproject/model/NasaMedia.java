package com.tumo.finalproject.model;

/**
 * A NASA photo or video as the API sends it to the browser.
 *
 * <p>This is a plain data class (a DTO) — NOT a database entity. It carries NASA
 * Image and Video Library search results, favorites/watchlist responses, and the
 * media the chatbot recommends. Start with this class: almost everything else
 * depends on it.
 *
 * <h2>TODO 1 — declare the fields (all private)</h2>
 * <pre>
 *   String id            NASA's own id for this item, e.g. "as11-40-5875"
 *                         (a String, not a number — NASA ids are not numeric)
 *   String title
 *   String description   the caption / write-up for the photo or video
 *   String mediaType     "image" or "video"
 *   String dateCreated   e.g. "1969-07-21T00:00:00Z"
 *   String thumbnailUrl  a full https:// link to a preview image, or null if
 *                         NASA did not provide one for this item
 * </pre>
 *
 * <h2>TODO 2 — annotate the three multi-word fields</h2>
 * NASA's JSON and our frontend both use snake_case; Java uses camelCase. Jackson
 * (the library that converts between Java objects and JSON) bridges the two, but
 * only if you tell it the JSON name. Import
 * {@code com.fasterxml.jackson.annotation.JsonProperty} and add:
 * <pre>
 *   &#64;JsonProperty("media_type")     above mediaType
 *   &#64;JsonProperty("date_created")   above dateCreated
 *   &#64;JsonProperty("thumbnail_url")  above thumbnailUrl
 * </pre>
 * Skip these and the page will show blank thumbnails and a missing media-type
 * badge, because {@code js/app.js} reads {@code media.thumbnail_url} and
 * {@code media.media_type}.
 *
 * <h2>TODO 3 — add two constructors</h2>
 * <ul>
 *   <li>A no-argument constructor. Jackson needs it to build a NasaMedia from JSON.</li>
 *   <li>A constructor taking all six fields, in the order listed above.</li>
 * </ul>
 *
 * <h2>TODO 4 — add a getter and a setter for every field</h2>
 * ({@code getId}/{@code setId}, {@code getTitle}/{@code setTitle}, and so on.)
 * Your IDE can generate them: right-click → Generate → Getter and Setter.
 * Jackson builds the JSON response from the getters, so a missing getter means a
 * missing field in the browser.
 */
public class NasaMedia {

    // TODO: fields, constructors, getters and setters go here.
}
