package com.tumo.finalproject.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A NASA photo or video one user has saved to their favorites — one row in the
 * {@code favorites} table.
 *
 * <p>Why not just store a {@link NasaMedia}? Because a favorite needs two extra
 * things a NasaMedia does not have: a database primary key, and the
 * {@code username} of the person who saved it. {@code FavoriteMedia} is the
 * database shape; {@link NasaMedia} is the shape the browser sees.
 * {@code FavoritesService} converts between them.
 *
 * <h2>TODO 1 — declare the remaining fields (all private)</h2>
 * <pre>
 *   String username     who saved this item
 *   String nasaId       the item's NASA id (not our primary key!)
 *   String title
 *   String description
 *   String mediaType
 *   String dateCreated
 *   String thumbnailUrl
 * </pre>
 *
 * <h2>TODO 2 — annotate the fields</h2>
 * Import {@code jakarta.persistence.Column} and add:
 * <pre>
 *   &#64;Column(nullable = false)     above username and above nasaId
 *   &#64;Column(length = 2000)        above description
 * </pre>
 * A NASA caption easily exceeds the 255-character default, so without
 * {@code length = 2000} saving a long description fails at runtime.
 *
 * <h2>TODO 3 — stop the same item being favorited twice</h2>
 * Import {@code jakarta.persistence.UniqueConstraint} and extend the
 * {@code @Table} annotation below so the <i>pair</i> (username, nasaId) must be
 * unique:
 * <pre>
 *   &#64;Table(name = "favorites",
 *          uniqueConstraints = &#64;UniqueConstraint(columnNames = {"username", "nasaId"}))
 * </pre>
 * Two different users may both favorite the same photo; one user may not
 * favorite it twice. Add this only after TODO 1, or Hibernate will fail at
 * startup naming a column that does not exist yet.
 *
 * <h2>TODO 4 — add two constructors</h2>
 * <ul>
 *   <li>A no-argument constructor (required by JPA).</li>
 *   <li>A constructor taking all seven fields, in the order listed above.</li>
 * </ul>
 *
 * <h2>TODO 5 — add getters and setters for every field, including {@code id}</h2>
 */
@Entity
@Table(name = "favorites")
public class FavoriteMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: remaining fields, constructors, getters and setters go here.
}
