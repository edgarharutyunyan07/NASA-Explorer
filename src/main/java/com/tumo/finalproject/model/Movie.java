package com.tumo.finalproject.model;

/**
 * A movie as the API sends it to the browser.
 *
 * <p>This is a plain data class (a DTO) — NOT a database entity. It carries TMDB
 * search results, favorites/watchlist responses, and the movies the chatbot
 * recommends. Start with this class: almost everything else depends on it.
 *
 * <h2>TODO 1 — declare the fields (all private)</h2>
 * <pre>
 *   int    id            the movie's TMDB id
 *   String title
 *   String overview      the plot summary
 *   double voteAverage   TMDB rating, 0.0 to 10.0
 *   String releaseDate   e.g. "1999-03-31"
 *   String posterPath    e.g. "/abc123.jpg", or null when there is no poster
 * </pre>
 *
 * <h2>TODO 2 — annotate the three multi-word fields</h2>
 * TMDB's JSON and our frontend both use snake_case; Java uses camelCase. Jackson
 * (the library that converts between Java objects and JSON) bridges the two, but
 * only if you tell it the JSON name. Import
 * {@code com.fasterxml.jackson.annotation.JsonProperty} and add:
 * <pre>
 *   &#64;JsonProperty("vote_average")   above voteAverage
 *   &#64;JsonProperty("release_date")   above releaseDate
 *   &#64;JsonProperty("poster_path")    above posterPath
 * </pre>
 * Skip these and the page will show "N/A" ratings and blank posters, because
 * {@code js/app.js} reads {@code movie.vote_average} and {@code movie.poster_path}.
 *
 * <h2>TODO 3 — add two constructors</h2>
 * <ul>
 *   <li>A no-argument constructor. Jackson needs it to build a Movie from JSON.</li>
 *   <li>A constructor taking all six fields, in the order listed above.</li>
 * </ul>
 *
 * <h2>TODO 4 — add a getter and a setter for every field</h2>
 * ({@code getId}/{@code setId}, {@code getTitle}/{@code setTitle}, and so on.)
 * Your IDE can generate them: right-click → Generate → Getter and Setter.
 * Jackson builds the JSON response from the getters, so a missing getter means a
 * missing field in the browser.
 */
public class Movie {

    // TODO: fields, constructors, getters and setters go here.
}
