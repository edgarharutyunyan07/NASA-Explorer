package com.tumo.finalproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A registered user. This is a JPA <b>entity</b>: one Java object here equals one
 * row in the {@code users} database table, and Hibernate creates that table for
 * you at startup from the annotations below.
 *
 * <p>We never store the real password — only a BCrypt <i>hash</i> of it. See
 * {@code UserService} for where the hashing happens.
 *
 * <p>The class-level annotations and the {@code id} field are done for you as an
 * example. Notice {@code @Table(name = "users")}: "user" is a reserved word in
 * many databases, so the plural avoids an SQL syntax error.
 *
 * <h2>TODO 1 — declare the remaining fields (all private)</h2>
 * <pre>
 *   String username
 *   String passwordHash
 * </pre>
 *
 * <h2>TODO 2 — annotate them with column rules</h2>
 * Import {@code jakarta.persistence.Column} and add:
 * <pre>
 *   &#64;Column(unique = true, nullable = false)   above username
 *   &#64;Column(nullable = false)                  above passwordHash
 * </pre>
 * {@code unique = true} makes the <i>database itself</i> reject a duplicate
 * username, even if a bug slips past your Java checks.
 *
 * <h2>TODO 3 — add two constructors</h2>
 * <ul>
 *   <li>A no-argument constructor. <b>JPA requires this</b> — Hibernate creates
 *       empty objects and then fills in the fields when reading rows.</li>
 *   <li>A constructor taking {@code (String username, String passwordHash)}.
 *       Do not set {@code id} here; the database assigns it.</li>
 * </ul>
 *
 * <h2>TODO 4 — add getters and setters for all three fields</h2>
 * ({@code getId}/{@code setId}, {@code getUsername}/{@code setUsername},
 * {@code getPasswordHash}/{@code setPasswordHash}.)
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * The primary key. {@code GenerationType.IDENTITY} lets the database generate
     * the value, so new users get 1, 2, 3, ... automatically.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    public User() {
    }

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
