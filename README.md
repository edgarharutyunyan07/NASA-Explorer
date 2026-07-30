# TUMO Final Project — NASA Explorer

A full-stack space exploration app: search NASA's image and video library, save your favorites and watchlist, create an account, and chat with an AI that recommends space discoveries.

**The frontend is finished. The backend is your job.**

---

## 🚀 Quick Start Setup

### 1. Environment Variables

Create a `.env` file in the project root by copying the example:

```bash
cp .env.example .env
```

Then add your API keys to `.env`:

```env
GROQ_API_KEY=your_actual_groq_api_key_here
```

**Get your Groq API key:** https://console.groq.com/keys

**⚠️ Important:** The `.env` file is in `.gitignore` and will NOT be committed. Never commit real API keys to GitHub!

Every Java class in this project is a skeleton: the class names, method signatures
and annotations are there, but the bodies are empty and marked `TODO`. Each `TODO`
explains what the method has to do and why. Work through them, and a working app
appears in the browser.

You can build the movie app exactly as described here, or keep the same
architecture and apply it to a theme of your own choosing — see
[Choose your project](#choose-your-project) below before you start.

---

## What you are building

```
Browser (done for you)          Your Java code                    The internet
─────────────────────           ────────────────                  ────────────
index.html                      Controller  ← HTTP requests
css/style.css        ──────►    Service     ← the actual logic  ──────►  TMDB API
js/app.js                       Repository  ← database access            Groq AI API
                                    │
                                    ▼
                                 H2 database (a file in ./data)
```

Requests always flow in one direction: **Controller → Service → Repository →
database**. Each layer talks only to the one below it. Keeping those jobs separate
is the main idea this project teaches.

---

## Choose your project

You have two options. Both teach the same thing and both are graded the same way.

### Option A — build the movie app

Follow this README as written. Query TMDB, keep the class names, use the frontend
as it is. Everything below applies to you directly.

Pick this if you want to concentrate on the Java and the architecture.

### Option B — pick your own theme

Keep the same architecture, swap the subject. Instead of movies, build the same
app around something you actually care about: recipes, video games, books, anime. The structure does not
change at all: search an external API, save results to two personal lists, log in,
chat with an AI about them.

Pick this if you want a project that is yours.

**You must find a working public API to query.** Before you commit to a theme,
prove the API works — that is part of the assignment. Check all five:

1. It is **free** and does not ask for a credit card.
2. You can **actually get a key** today.
3. It returns **JSON** over HTTPS.
4. It has a **search endpoint** that takes a text query and returns a list.
5. Each result carries **enough fields to fill a card**, at least a name, a description.

Test it before writing any Java. Paste the URL with your key into a browser, or
run `curl`. If you see the JSON you expected, you have an API. If you see 401, 403
or an empty result, you do not — keep looking.

Some starting points: [Spoonacular](https://spoonacular.com/food-api) (recipes),
[RAWG](https://rawg.io/apidocs) (games), [Open Library](https://openlibrary.org/developers/api)
(books), [PokéAPI](https://pokeapi.co) (no key needed),
[NASA](https://api.nasa.gov) (space). Do not assume these still work, do a research yourself.

**If you cannot find an API that passes all five tests, fall back to Option A and
build the movie app with TMDB.** A finished movie app beats a half-built one
blocked on an API that never worked. You have to test and decide by the end of Monday. 

### If you choose Option B, rename the classes

Names should describe what the code holds. A recipe app with a class called
`Movie` is confusing to read and confusing to grade, so rename every file to its
descriptive counterpart. For a recipe app:

| Movie version | Recipe version |
|---|---|
| `Movie` | `Recipe` |
| `TmdbService` | `SpoonacularService` — name it after the API you chose |
| `FavoriteMovie` | `FavoriteRecipe` |
| `WatchlistItem` | `SavedRecipe` — or `ToCookItem`, whatever fits |
| `FavoritesService` / `FavoriteRepository` | `FavoritesService` / `FavoriteRepository` — these stay |
| `MovieController` | `RecipeController` |
| `MovieChatService` | `RecipeChatService` |
| `WatchlistService` / `WatchlistRepository` | `SavedRecipeService` / `SavedRecipeRepository` |

Rename the fields too: `posterPath` → `imageUrl`, `voteAverage` → `rating`,
`releaseDate` → `readyInMinutes`, `tmdbId` → `spoonacularId`. Keep whatever your
API actually gives you and drop what it does not.

Use your IDE's rename refactoring (**Shift+F6** in IntelliJ) rather than editing
by hand — it updates every usage, the file name and the constructor in one go.
`User`, `ChatMessage`, `AuthController`, `UserService` and `UserRepository` keep
their names in every theme; they are not about movies.

**One thing to plan for:** the frontend is written for movies. `js/app.js` reads
`poster_path`, `vote_average` and `release_date`, calls `/api/movies/**`, and
`index.html` says "TUMO Movies". If you change your endpoint paths or field names,
you must update `js/app.js` and `index.html` to match — otherwise the page stays
blank while your backend works perfectly. Two honest ways to handle it:

- **Do it properly:** rename the paths and fields, then update `app.js`,
  `index.html` and the headings. Budget an hour, and use DevTools → Network to see
  what the browser is actually receiving. You may use AI for your advantage here, it is okay to ask Claude Code to refactor frontend for you.

---

## Setup

You need **Java 17 or newer**. Check with `java -version`.

### 1. Get two free API keys

| Key | Where | Used for |
|-----|-------|----------|
| TMDB | https://www.themoviedb.org/settings/api | Real movie data, posters, ratings |
| Groq | https://console.groq.com/keys | The AI chatbot |

Both are free. TMDB asks a few questions when you request a key — say the use is
educational.

**Option B students:** replace the TMDB row with your own API and rename the
property in `application.properties` and the `@Value` in your service to match
(`tmdb.api.key` → e.g. `spoonacular.api.key`). You still need the Groq key — the
chatbot is part of every theme. If your API needs no key at all, delete the
property and the constructor parameter.

### 2. Tell the app about your keys

Never paste a key into a source file — you would commit it to GitHub and it would
have to be revoked. Set them as environment variables instead.

**macOS / Linux:**
```bash
export TMDB_API_KEY=your_tmdb_key_here
export GROQ_API_KEY=your_groq_key_here
```

**Windows (PowerShell):**
```powershell
$env:TMDB_API_KEY="your_tmdb_key_here"
$env:GROQ_API_KEY="your_groq_key_here"
```

These last only for the current terminal window. Open a new terminal and you must
set them again — or add the lines to your shell profile (`~/.zshrc`) to make them
permanent.

### 3. Run it

```bash
./mvnw spring-boot:run     # macOS / Linux
mvnw.cmd spring-boot:run   # Windows
```

Then open **http://localhost:8080**

The page loads immediately, but every button fails until you implement things —
that is expected. Stop the app with `Ctrl+C`.

---

## Suggested order

### Start with login — the app is locked until you do

Run the app right now and you will see a sign-in screen and nothing else. That is
not a bug. When the page loads, `js/app.js` calls `GET /api/auth/me` to ask "who is
logged in?". Nobody is, so it shows the login card as a full-screen overlay. There
is no guest mode and no way to click past it.

So **authentication is step 1**, before movies, before search, before anything. It
is the door. Until it opens, you cannot see any other work you do.

Once you can log in, the overlay disappears and the rest of the app is visible —
still broken, but visible. The buttons will fail quietly (check the browser console
with F12) instead of hiding the whole page from you.

### The order

Later steps depend on earlier ones, and each step gives you something you can see
in the browser.

| # | Files | You will get |
|---|-------|--------------|
| 1 | `model/User.java`, `repository/UserRepository.java`, `service/UserService.java`, `controller/AuthController.java` | **Sign up and log in works.** The overlay opens and you can finally see the app |
| 2 | `model/Movie.java` | Nothing visible yet — but every step below needs it, so do it next |
| 3 | `service/TmdbService.java` + `MovieController.searchMovies` | **Search works.** Real movies with posters and ratings appear |
| 4 | `model/FavoriteMovie.java`, `repository/FavoriteRepository.java`, `service/FavoritesService.java`, `MovieController.currentUser` + the three favorites methods | **Saving favorites works.** The heart icon fills in and survives a restart |
| 5 | `model/WatchlistItem.java`, `repository/WatchlistRepository.java`, `service/WatchlistService.java`, the three watchlist methods in `MovieController` | **Watchlist works** — same shape as step 4, so try it without looking back |
| 6 | `model/ChatMessage.java`, `service/MovieChatService.java`, `controller/ChatController.java` | **The AI chatbot works** and its recommendations are saveable |

Inside a step, build from the bottom up: model → repository → service →
controller. That way each piece you write can already rely on the piece below it.

Do not skip ahead to the chatbot. `ChatController` needs both `MovieChatService`
and `TmdbService` working, so it is genuinely last.

**Option B students:** the same six steps in the same order, using your renamed
classes. Step 1 is identical for you — `User`, `UserRepository`, `UserService` and
`AuthController` keep their names and their code in every theme, so you can follow
step 1 exactly as written before you rename anything.

---

## How to tell it is working

Every unimplemented method throws `UnsupportedOperationException` with its own
name in the message. So a failure tells you exactly where you are:

```
java.lang.UnsupportedOperationException: TmdbService.searchMovies not implemented
```

That is not a bug — it is your to-do list talking. Look in the terminal running
the app whenever a button does nothing.

Two more tools worth knowing:

- **Browser DevTools** (F12) → Network tab. Click a button and watch the request:
  the URL, the status code, and the JSON that came back. A 401 means "you are not
  logged in", a 500 means your Java code threw an exception.
- **The database console** at http://localhost:8080/h2-console — set *JDBC URL* to
  `jdbc:h2:file:./data/tumo`, user `sa`, no password. You can see your actual
  tables and rows, which makes "did that save?" a question you can answer instead
  of guess.

---

## API endpoints

### Option A — the exact contract

`js/app.js` already calls these URLs. **Do not rename them.** Change a path and the
frontend silently stops finding your endpoint.

| Method | Endpoint | Description | Login required |
|--------|----------|-------------|----------------|
| GET | `/api/movies/search?query={q}` | Search TMDB | no |
| GET | `/api/movies/favorites` | List favorites | yes |
| POST | `/api/movies/favorites` | Add a favorite | yes |
| DELETE | `/api/movies/favorites/{id}` | Remove a favorite | yes |
| GET | `/api/movies/watchlist` | List the watchlist | yes |
| POST | `/api/movies/watchlist` | Add to the watchlist | yes |
| DELETE | `/api/movies/watchlist/{id}` | Remove from the watchlist | yes |
| POST | `/api/chat` | AI recommendations | no |
| POST | `/api/auth/register` | Create an account | no |
| POST | `/api/auth/login` | Log in | no |
| POST | `/api/auth/logout` | Log out | no |
| GET | `/api/auth/me` | Who is logged in | no |

`{id}` is always a **TMDB** movie id, never your database primary key.

"Login required: no" for search and chat is about the API itself — the browser still
puts the login overlay in front of everything, so in practice you log in first
either way.

### Option B — your contract

Your paths change with your theme, and this table becomes **your** table. The shape
does not change: twelve endpoints, four groups, same methods, same status codes.
Only the noun moves.

Substitute your resource name for `movies` and your two list names for `favorites`
and `watchlist`. A recipe app:

| Movie version | Recipe version |
|---|---|
| `GET /api/movies/search?query={q}` | `GET /api/recipes/search?query={q}` |
| `GET/POST /api/movies/favorites` | `GET/POST /api/recipes/favorites` |
| `DELETE /api/movies/favorites/{id}` | `DELETE /api/recipes/favorites/{id}` |
| `GET/POST /api/movies/watchlist` | `GET/POST /api/recipes/to-cook` |
| `DELETE /api/movies/watchlist/{id}` | `DELETE /api/recipes/to-cook/{id}` |
| `POST /api/chat` | `POST /api/chat` — unchanged |
| everything under `/api/auth/**` | unchanged in every theme |

Three rules for Option B:

1. **The four `/api/auth/**` endpoints never change.** Logging in is logging in,
   whatever your app is about. Same paths, same request bodies, same status codes.
2. **`/api/chat` stays `/api/chat`.** Only the system prompt inside
   `MovieChatService` changes — from "recommend movies" to "recommend recipes".
3. **`{id}` is always your external API's id**, never your database primary key.
   Whatever TMDB's `id` was, yours is Spoonacular's or RAWG's or Open Library's.
   Keep that distinction — mixing the two up is the classic bug in this project,
   in any theme.

Then rewrite the table above for your own app and put it in your README. Every
path you invent must match what you wrote in `js/app.js`. If a button does nothing,
open DevTools → Network: a **404** means the browser is calling a URL your
controller does not answer, and that is a spelling mismatch between the two.

---

## Project structure

```
src/main/java/com/tumo/finalproject/
├── TumoFinalProjectApplication.java   # Entry point — already done
├── controller/                        # HTTP layer: read request, return status code
│   ├── MovieController.java           #   /api/movies/**
│   ├── AuthController.java            #   /api/auth/**
│   └── ChatController.java            #   /api/chat
├── service/                           # The logic: API calls, rules, conversions
│   ├── TmdbService.java               #   calls the TMDB API
│   ├── MovieChatService.java          #   calls the Groq AI API
│   ├── UserService.java               #   registration, BCrypt passwords
│   ├── FavoritesService.java          #   favorites logic
│   └── WatchlistService.java          #   watchlist logic
├── repository/                        # Database access (Spring writes the SQL)
│   ├── UserRepository.java
│   ├── FavoriteRepository.java
│   └── WatchlistRepository.java
└── model/                             # The data
    ├── Movie.java                     #   sent to the browser (not a table)
    ├── ChatMessage.java               #   one chat message
    ├── User.java                      #   @Entity → users table
    ├── FavoriteMovie.java             #   @Entity → favorites table
    └── WatchlistItem.java             #   @Entity → watchlist table

src/main/resources/
├── application.properties             # Config and keys — already done
└── static/                            # The frontend — already done
    ├── index.html
    ├── css/style.css
    └── js/app.js
```

---

## Commands

```bash
./mvnw spring-boot:run                    # run the app
./mvnw clean package                      # build a jar
java -jar target/tumo-final-project-1.0.0.jar   # run that jar
```

---

## Troubleshooting

**"No property 'username' found for type 'User'"** — a repository declares
`findByUsername` but the entity has no `username` field yet. Add the fields to the
model before the query methods that use them.

**Posters and ratings are blank** — the `@JsonProperty` annotations are missing
from `Movie`. The browser reads `poster_path` and `vote_average`, not
`posterPath` and `voteAverage`.

**Search returns 401 from TMDB** — your `TMDB_API_KEY` is not set in the terminal
you launched the app from. Restart the app after exporting it.

**The chatbot says it is getting too many requests** — free Groq keys are rate
limited. Wait a minute and try again.

**Deleting a favorite throws a transaction error** — `removeFavorite` needs
`@Transactional`.

**Port 8080 is already in use** — another copy of the app is still running. Stop
it, or change `server.port` in `application.properties`.

**Something is deeply broken in the database** — delete the `data/` folder. It is
rebuilt empty on the next start. You will lose your test accounts, which is fine.

---

## Stretch goals

Once everything works:
- Update the frontend to your style/design
- Sort or filter the saved lists (by rating, by year)
- Give the chatbot memory of the conversation — you already have `ChatMessage`
- Show trending movies on the homepage (TMDB has a `/trending/movie/week` endpoint)
- Add a "seen it" flag to watchlist entries
