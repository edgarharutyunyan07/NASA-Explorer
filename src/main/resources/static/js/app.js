const API_BASE = '/api';

let currentMedia = [];
let favorites = [];
let watchlist = [];
let selectedMedia = null;
let authMode = 'login'; // 'login' or 'register'
let searchHistory = [];
let currentSearchQuery = '';
let currentSearchPage = 1;
let isLoadingMore = false;
let hasMoreResults = true;

// --- Init ---
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadSearchHistory();

    const searchInput = document.getElementById('searchInput');
    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') searchMedia();
    });
    searchInput.addEventListener('focus', showSearchHistory);
    searchInput.addEventListener('input', showSearchHistory);
    document.addEventListener('click', (e) => {
        if (e.target !== searchInput && !e.target.closest('.search-history')) {
            hideSearchHistory();
        }
    });

    document.getElementById('chatInput').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') sendChat();
    });

    document.getElementById('authPassword').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') submitAuth();
    });
    document.getElementById('authUsername').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') document.getElementById('authPassword').focus();
    });

    document.getElementById('btnBrowse').classList.add('active');

    // Infinite scroll detection
    document.addEventListener('scroll', () => {
        if (isLoadingMore || !hasMoreResults) return;
        const scrollPos = window.innerHeight + window.scrollY;
        const threshold = document.body.offsetHeight - 800;
        if (scrollPos > threshold && currentSearchQuery) {
            loadMoreResults();
        }
    });
});

// --- Auth ---
async function checkAuth() {
    try {
        const res = await fetch(`${API_BASE}/auth/me`);
        if (res.ok) {
            const data = await res.json();
            onLoggedIn(data.username);
        } else {
            showAuthOverlay();
        }
    } catch (err) {
        showAuthOverlay();
    }
}

function showAuthOverlay() {
    document.getElementById('authOverlay').classList.remove('d-none');
    document.getElementById('userArea').classList.add('d-none');
    document.getElementById('authUsername').focus();
}

function onLoggedIn(username) {
    document.getElementById('authOverlay').classList.add('d-none');
    document.getElementById('userArea').classList.remove('d-none');
    document.getElementById('usernameLabel').textContent = username;
    document.getElementById('authUsername').value = '';
    document.getElementById('authPassword').value = '';
    hideAuthError();
    loadFavorites();
    loadWatchlist();
}

function toggleAuthMode() {
    authMode = authMode === 'login' ? 'register' : 'login';
    const isLogin = authMode === 'login';
    document.getElementById('authSubtitle').textContent =
        isLogin ? 'Log in to see your saved photos and videos' : 'Create an account to save your favorites';
    document.getElementById('authSubmitBtn').textContent = isLogin ? 'Log In' : 'Sign Up';
    document.getElementById('authTogglePrompt').textContent =
        isLogin ? "Don't have an account?" : 'Already have an account?';
    document.getElementById('authToggleLink').textContent = isLogin ? 'Sign up' : 'Log in';
    document.getElementById('authPassword').setAttribute(
        'autocomplete', isLogin ? 'current-password' : 'new-password');
    hideAuthError();
}

async function submitAuth() {
    const username = document.getElementById('authUsername').value.trim();
    const password = document.getElementById('authPassword').value;
    if (!username || !password) {
        showAuthError('Please enter a username and password.');
        return;
    }

    const endpoint = authMode === 'login' ? 'login' : 'register';
    const btn = document.getElementById('authSubmitBtn');
    btn.disabled = true;

    try {
        const res = await fetch(`${API_BASE}/auth/${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json().catch(() => ({}));
        if (res.ok) {
            onLoggedIn(data.username);
        } else {
            showAuthError(data.error || 'Something went wrong. Please try again.');
        }
    } catch (err) {
        showAuthError('Could not reach the server. Please try again.');
    } finally {
        btn.disabled = false;
    }
}

async function logout() {
    try {
        await fetch(`${API_BASE}/auth/logout`, { method: 'POST' });
    } catch (err) {
        // ignore — clear client state regardless
    }
    favorites = [];
    watchlist = [];
    currentMedia = [];
    document.getElementById('movieGrid').innerHTML = '';
    document.getElementById('favoritesGrid').innerHTML = '';
    document.getElementById('watchlistGrid').innerHTML = '';
    updateFavCount();
    updateWatchCount();
    authMode = 'login';
    toggleAuthMode();      // reset labels then...
    toggleAuthMode();      // ...back to login state
    showAuthOverlay();
}

function showAuthError(msg) {
    const el = document.getElementById('authError');
    el.textContent = msg;
    el.classList.remove('d-none');
}

function hideAuthError() {
    document.getElementById('authError').classList.add('d-none');
}

// --- Navigation ---
function showSection(section) {
    document.getElementById('browseSection').classList.toggle('d-none', section !== 'browse');
    document.getElementById('favoritesSection').classList.toggle('d-none', section !== 'favorites');
    document.getElementById('watchlistSection').classList.toggle('d-none', section !== 'watchlist');
    document.getElementById('btnBrowse').classList.toggle('active', section === 'browse');
    document.getElementById('btnFavorites').classList.toggle('active', section === 'favorites');
    document.getElementById('btnWatchlist').classList.toggle('active', section === 'watchlist');

    if (section === 'favorites') {
        loadFavorites();
    } else if (section === 'watchlist') {
        loadWatchlist();
    }
}

// --- Search History ---
function loadSearchHistory() {
    const stored = localStorage.getItem('nasaSearchHistory');
    searchHistory = stored ? JSON.parse(stored) : [];
}

function saveSearchHistory(query) {
    searchHistory = searchHistory.filter(q => q !== query);
    searchHistory.unshift(query);
    searchHistory = searchHistory.slice(0, 8);
    localStorage.setItem('nasaSearchHistory', JSON.stringify(searchHistory));
}

function showSearchHistory() {
    const input = document.getElementById('searchInput').value.trim().toLowerCase();
    const historyEl = document.getElementById('searchHistory');
    const filtered = searchHistory.filter(q => q.toLowerCase().includes(input) && q !== input);

    if (filtered.length === 0) {
        historyEl.classList.remove('show');
        return;
    }

    historyEl.innerHTML = filtered.map(q => `
        <div class="search-history-item" onclick="selectFromHistory('${escapeHtml(q)}'); return false;">
            <i class="bi bi-clock-history"></i>
            ${escapeHtml(q)}
        </div>
    `).join('');
    historyEl.classList.add('show');
}

function hideSearchHistory() {
    document.getElementById('searchHistory').classList.remove('show');
}

function selectFromHistory(query) {
    document.getElementById('searchInput').value = query;
    hideSearchHistory();
    searchMedia();
}

// --- Search ---
async function searchMedia(directQuery = null) {
    const query = directQuery || document.getElementById('searchInput').value.trim();
    if (!query) return;

    if (!directQuery) {
        saveSearchHistory(query);
    } else {
        document.getElementById('searchInput').value = query;
        saveSearchHistory(query);
    }

    currentSearchQuery = query;
    currentSearchPage = 1;
    hasMoreResults = true;
    hideSearchHistory();

    showLoading(true);
    try {
        const res = await fetch(`${API_BASE}/media/search?query=${encodeURIComponent(query)}`);
        if (!res.ok) throw new Error('Search failed');
        currentMedia = await res.json();
        renderMediaGrid(currentMedia, 'movieGrid', 'favorites', true);
        document.getElementById('resultsTitle').textContent =
            currentMedia.length > 0 ? `Results for "${query}"` : '';
        document.getElementById('noResults').classList.toggle('d-none', currentMedia.length > 0);
        hasMoreResults = currentMedia.length >= 50;

        // Smooth scroll to results if searching from featured card
        if (directQuery) {
            setTimeout(() => {
                document.getElementById('resultsTitle').scrollIntoView({ behavior: 'smooth', block: 'start' });
            }, 100);
        }
    } catch (err) {
        console.error(err);
        document.getElementById('resultsTitle').textContent = 'Error searching NASA media';
    } finally {
        showLoading(false);
    }
}

async function loadMoreResults() {
    if (isLoadingMore || !currentSearchQuery || !hasMoreResults) return;
    isLoadingMore = true;
    currentSearchPage++;

    try {
        const res = await fetch(`${API_BASE}/media/search?query=${encodeURIComponent(currentSearchQuery)}&page=${currentSearchPage}`);
        if (!res.ok) throw new Error('Search failed');
        const newMedia = await res.json();
        if (newMedia.length === 0) {
            hasMoreResults = false;
            return;
        }
        currentMedia = [...currentMedia, ...newMedia];
        const container = document.getElementById('movieGrid');
        newMedia.forEach((media, idx) => {
            container.appendChild(createMediaCard(media, 'favorites', idx + 20));
        });
        hasMoreResults = newMedia.length >= 50;
    } catch (err) {
        console.error('Failed to load more results', err);
    } finally {
        isLoadingMore = false;
    }
}

// --- Favorites ---
async function loadFavorites() {
    try {
        const res = await fetch(`${API_BASE}/media/favorites`);
        if (res.status === 401) {
            showAuthOverlay();
            return;
        }
        favorites = await res.json();
        updateFavCount();
        renderMediaGrid(favorites, 'favoritesGrid', 'favorites', true);
        document.getElementById('noFavorites').classList.toggle('d-none', favorites.length > 0);
        // Re-render search results to update heart icons
        if (currentMedia.length > 0) {
            renderMediaGrid(currentMedia, 'movieGrid', 'favorites', true);
        }
    } catch (err) {
        console.error('Failed to load favorites', err);
    }
}

async function addFavorite(media) {
    try {
        await fetch(`${API_BASE}/media/favorites`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(media)
        });
        await loadFavorites();
    } catch (err) {
        console.error('Failed to add favorite', err);
    }
}

async function removeFavorite(id) {
    try {
        await fetch(`${API_BASE}/media/favorites/${encodeURIComponent(id)}`, { method: 'DELETE' });
        await loadFavorites();
    } catch (err) {
        console.error('Failed to remove favorite', err);
    }
}

function isFavorite(id) {
    return favorites.some(m => m.id === id);
}

function updateFavCount() {
    const badge = document.getElementById('favCount');
    if (favorites.length > 0) {
        badge.textContent = favorites.length;
        badge.classList.remove('d-none');
    } else {
        badge.classList.add('d-none');
    }
}

// --- Watchlist (save for later) ---
async function loadWatchlist() {
    try {
        const res = await fetch(`${API_BASE}/media/watchlist`);
        if (res.status === 401) {
            showAuthOverlay();
            return;
        }
        watchlist = await res.json();
        updateWatchCount();
        renderMediaGrid(watchlist, 'watchlistGrid', 'watchlist', true);
        document.getElementById('noWatchlist').classList.toggle('d-none', watchlist.length > 0);
    } catch (err) {
        console.error('Failed to load watchlist', err);
    }
}

async function addToWatchlist(media) {
    try {
        await fetch(`${API_BASE}/media/watchlist`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(media)
        });
        await loadWatchlist();
    } catch (err) {
        console.error('Failed to add to watchlist', err);
    }
}

async function removeFromWatchlist(id) {
    try {
        await fetch(`${API_BASE}/media/watchlist/${encodeURIComponent(id)}`, { method: 'DELETE' });
        await loadWatchlist();
    } catch (err) {
        console.error('Failed to remove from watchlist', err);
    }
}

function isInWatchlist(id) {
    return watchlist.some(m => m.id === id);
}

function updateWatchCount() {
    const badge = document.getElementById('watchCount');
    if (watchlist.length > 0) {
        badge.textContent = watchlist.length;
        badge.classList.remove('d-none');
    } else {
        badge.classList.add('d-none');
    }
}

// --- Rendering ---
function renderMediaGrid(items, containerId, mode = 'favorites', clearFirst = true) {
    const container = document.getElementById(containerId);
    if (clearFirst) container.innerHTML = '';

    items.forEach((media, idx) => {
        const card = createMediaCard(media, mode, idx);
        container.appendChild(card);
    });
}

function createMediaCard(media, mode = 'favorites', delayIdx = 0) {
    const thumbnailUrl = media.thumbnail_url || null;
    const year = media.date_created ? media.date_created.substring(0, 4) : '';
    const isVideo = media.media_type === 'video';

    let actionBtn;
    if (mode === 'watchlist') {
        actionBtn = `
            <button class="fav-btn active"
                    onclick="event.stopPropagation(); removeFromWatchlist('${media.id}')"
                    title="Remove from watchlist">
                <i class="bi bi-bookmark-x-fill"></i>
            </button>`;
    } else {
        const isFav = isFavorite(media.id);
        actionBtn = `
            <button class="fav-btn ${isFav ? 'active' : ''}"
                    onclick="event.stopPropagation(); toggleFavorite('${media.id}')"
                    title="${isFav ? 'Remove from favorites' : 'Add to favorites'}">
                <i class="bi ${isFav ? 'bi-heart-fill' : 'bi-heart'}"></i>
            </button>`;
    }

    const cardDiv = document.createElement('div');
    cardDiv.className = 'movie-card reveal';
    cardDiv.onclick = () => openDetail(media.id);
    cardDiv.style.animationDelay = `${delayIdx * 0.05}s`;
    cardDiv.innerHTML = `
        ${thumbnailUrl
            ? `<img class="poster" src="${thumbnailUrl}" alt="${escapeHtml(media.title)}" loading="lazy">`
            : `<div class="no-poster"><i class="bi bi-image"></i></div>`
        }
        ${isVideo ? '<span class="media-badge"><i class="bi bi-play-fill"></i> Video</span>' : ''}
        <div class="card-body">
            <div class="card-title" title="${escapeHtml(media.title)}">${escapeHtml(media.title)}</div>
            <div class="card-meta">
                <span class="text-muted">${year}</span>
                ${actionBtn}
            </div>
        </div>
    `;
    return cardDiv;
}

// --- Media Detail Modal ---
function openDetail(mediaId) {
    const media = findMedia(mediaId);
    if (!media) return;

    selectedMedia = media;
    const thumbnailUrl = media.thumbnail_url || '';
    const isFav = isFavorite(media.id);

    document.getElementById('modalPoster').src = thumbnailUrl;
    document.getElementById('modalPoster').style.display = thumbnailUrl ? 'block' : 'none';
    document.getElementById('modalTitle').textContent = media.title;
    document.getElementById('modalRating').textContent = media.media_type === 'video' ? 'Video' : 'Image';
    document.getElementById('modalDate').textContent = media.date_created ? media.date_created.substring(0, 10) : 'Unknown';
    document.getElementById('modalOverview').textContent = media.description || 'No description available.';
    document.getElementById('modalNasaLink').href = `https://images.nasa.gov/details/${encodeURIComponent(media.id)}`;

    const favBtn = document.getElementById('modalFavBtn');
    favBtn.innerHTML = isFav
        ? '<i class="bi bi-heart-fill"></i> Remove from Favorites'
        : '<i class="bi bi-heart"></i> Add to Favorites';
    favBtn.className = isFav ? 'btn btn-outline-danger btn-lg' : 'btn btn-danger btn-lg';

    new bootstrap.Modal(document.getElementById('movieModal')).show();
}

async function toggleFavoriteFromModal() {
    if (!selectedMedia) return;
    await toggleFavorite(selectedMedia.id);

    const isFav = isFavorite(selectedMedia.id);
    const favBtn = document.getElementById('modalFavBtn');
    favBtn.innerHTML = isFav
        ? '<i class="bi bi-heart-fill"></i> Remove from Favorites'
        : '<i class="bi bi-heart"></i> Add to Favorites';
    favBtn.className = isFav ? 'btn btn-outline-danger btn-lg' : 'btn btn-danger btn-lg';
}

async function toggleFavorite(mediaId) {
    if (isFavorite(mediaId)) {
        await removeFavorite(mediaId);
    } else {
        const media = findMedia(mediaId);
        if (media) await addFavorite(media);
    }
}

function findMedia(id) {
    return currentMedia.find(m => m.id === id)
        || favorites.find(m => m.id === id)
        || watchlist.find(m => m.id === id);
}

// --- Chat ---
function toggleChat() {
    const sidebar = document.getElementById('chatSidebar');
    sidebar.classList.toggle('open');
    document.getElementById('btnChat').classList.toggle('active', sidebar.classList.contains('open'));
    if (sidebar.classList.contains('open')) {
        document.getElementById('chatInput').focus();
    }
}

async function sendChat() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();
    if (!message) return;

    appendChatMsg('user', message);
    input.value = '';
    input.disabled = true;
    document.getElementById('chatSendBtn').disabled = true;

    try {
        const res = await fetch(`${API_BASE}/chat`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message })
        });
        const data = await res.json();
        appendChatMsg('bot', data.response || data.error || 'No response');
        if (Array.isArray(data.recommendations) && data.recommendations.length > 0) {
            appendChatRecommendations(data.recommendations);
        }
    } catch (err) {
        appendChatMsg('bot', 'Sorry, something went wrong. Please try again.');
    } finally {
        input.disabled = false;
        document.getElementById('chatSendBtn').disabled = false;
        input.focus();
    }
}

function appendChatRecommendations(items) {
    const container = document.getElementById('chatMessages');
    const wrap = document.createElement('div');
    wrap.className = 'chat-recs';

    items.forEach(media => {
        const year = media.date_created ? media.date_created.substring(0, 4) : '';
        const chip = document.createElement('div');
        chip.className = 'chat-rec';

        const label = document.createElement('span');
        label.className = 'chat-rec-title';
        label.textContent = year ? `${media.title} (${year})` : media.title;

        const btn = document.createElement('button');
        btn.className = 'chat-rec-btn';
        setSaveButtonState(btn, isInWatchlist(media.id));
        btn.onclick = () => saveFromChat(media, btn);

        chip.appendChild(label);
        chip.appendChild(btn);
        wrap.appendChild(chip);
    });

    container.appendChild(wrap);
    container.scrollTop = container.scrollHeight;
}

function setSaveButtonState(btn, saved) {
    btn.classList.toggle('saved', saved);
    btn.innerHTML = saved
        ? '<i class="bi bi-check-lg"></i> Saved'
        : '<i class="bi bi-bookmark-plus"></i> Save for later';
}

async function saveFromChat(media, btn) {
    if (isInWatchlist(media.id)) return;
    btn.disabled = true;
    await addToWatchlist(media);
    setSaveButtonState(btn, true);
    btn.disabled = false;
}

function appendChatMsg(role, text) {
    const container = document.getElementById('chatMessages');
    const div = document.createElement('div');
    div.className = `chat-msg ${role}`;
    const p = document.createElement('p');
    p.textContent = text;
    div.appendChild(p);
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

// --- Info Modal System ---
const infoContent = {
    'solar-system': {
        icon: '☄️',
        title: 'Our Solar System',
        description: 'Born approximately 4.6 billion years ago from a collapsing molecular cloud of gas and dust.',
        content: `<h4>Formation & Composition</h4>
            <p>Our solar system formed in the Orion Spur of the Milky Way galaxy. It consists of the Sun, eight planets, over 140 moons, millions of asteroids, and countless comets.</p>
            <h4>Key Facts</h4>
            <ul>
                <li>The Sun contains 99.86% of the solar system's mass</li>
                <li>Earth is the only known planet with life</li>
                <li>All planets orbit the Sun in roughly the same plane</li>
                <li>The solar system spans about 9.5 trillion miles in diameter</li>
            </ul>`,
        search: 'Solar System'
    },
    'galaxies': {
        icon: '🌌',
        title: 'Billions of Galaxies',
        description: 'Recent estimates suggest approximately 200 billion galaxies exist in the observable universe.',
        content: `<h4>Galaxy Types & Distribution</h4>
            <p>Galaxies come in three main types: spiral, elliptical, and irregular. They vary enormously in size, from dwarf galaxies with millions of stars to giants with trillions.</p>
            <h4>Amazing Statistics</h4>
            <ul>
                <li>Our Milky Way contains 100-200 billion stars</li>
                <li>Andromeda Galaxy is the closest large galaxy to us</li>
                <li>Galaxy clusters form the large-scale structure of the universe</li>
                <li>New galaxies continue to be discovered with improved telescopes</li>
            </ul>`,
        search: 'Galaxy'
    },
    'sun-size': {
        icon: '☀️',
        title: 'The Mighty Sun',
        description: 'One million Earths could fit inside the Sun, yet it contains 99.86% of our solar system\'s mass.',
        content: `<h4>Solar Power</h4>
            <p>The Sun is a massive sphere of hot plasma, powered by nuclear fusion in its core. It has been burning hydrogen for 4.6 billion years and will continue for another 5 billion years.</p>
            <h4>Solar Dimensions</h4>
            <ul>
                <li>Diameter: 865,000 miles (1.39 million km)</li>
                <li>Surface temperature: 27 million°F (10,000°C)</li>
                <li>Core temperature: 27 million°F (27 million°C)</li>
                <li>Mass: 2 octillion tons</li>
                <li>Every second, the Sun converts 620 million tons of hydrogen to helium</li>
            </ul>`,
        search: 'Sun'
    },
    'universe-age': {
        icon: '⏰',
        title: 'Age of the Universe',
        description: 'The universe is approximately 13.8 billion years old, dating from the Big Bang.',
        content: `<h4>Cosmic Timeline</h4>
            <p>Based on observations of the cosmic microwave background and the expansion rate of the universe, scientists have determined the universe began approximately 13.8 billion years ago.</p>
            <h4>Key Milestones</h4>
            <ul>
                <li>0 seconds: The Big Bang occurs</li>
                <li>First second: Fundamental forces separate</li>
                <li>3 minutes: Protons and neutrons form</li>
                <li>380,000 years: Atoms form, universe becomes transparent</li>
                <li>13.8 billion years: Today</li>
            </ul>`,
        search: 'Universe'
    },
    'andromeda': {
        icon: '🌠',
        title: 'Andromeda Galaxy',
        description: 'Light from Andromeda takes 2.2 million years to reach us, showing us the galaxy as it was millions of years ago.',
        content: `<h4>Our Galactic Neighbor</h4>
            <p>Andromeda is the closest major galaxy to the Milky Way and is on a collision course with us. In about 4.5 billion years, the two galaxies will merge to form a new elliptical galaxy.</p>
            <h4>Andromeda Facts</h4>
            <ul>
                <li>Contains about 1 trillion stars (more than the Milky Way)</li>
                <li>Distance: 2.5 million light-years away</li>
                <li>Visible to the naked eye from Earth</li>
                <li>Diameter: 2.2 million light-years</li>
                <li>The merged galaxy will be called Milkomeda</li>
            </ul>`,
        search: 'Andromeda'
    },
    'earth-orbit': {
        icon: '🚀',
        title: 'Earth\'s Orbital Velocity',
        description: 'Earth travels through space at approximately 67,000 mph (30 km/s) as it orbits the Sun.',
        content: `<h4>Motion Through Space</h4>
            <p>Earth's velocity changes slightly throughout the year due to its elliptical orbit. We travel faster in January when closest to the Sun and slower in July when farthest away.</p>
            <h4>Relative Velocities</h4>
            <ul>
                <li>Earth's orbital speed: 67,000 mph</li>
                <li>Solar system's speed through galaxy: 490,000 mph</li>
                <li>Milky Way's speed through space: 1.3 million mph</li>
                <li>Total: We're traveling at mind-bending speeds in multiple directions!</li>
            </ul>`,
        search: 'Earth orbit'
    },
    'vostok-1': {
        icon: '🛸',
        title: 'Vostok 1 - First Human in Space',
        description: 'On April 12, 1961, Yuri Gagarin became the first human to journey into space aboard Vostok 1.',
        content: `<h4>Historic Mission</h4>
            <p>Vostok 1 completed one orbit of Earth in approximately 108 minutes. Gagarin's successful mission paved the way for human space exploration and the Space Race between the Soviet Union and the United States.</p>
            <h4>Mission Details</h4>
            <ul>
                <li>Launch date: April 12, 1961</li>
                <li>Altitude: 187 miles (301 km)</li>
                <li>Duration: 108 minutes</li>
                <li>Orbits: 1 complete orbit</li>
                <li>Speed: 28,000 km/h (17,500 mph)</li>
                <li>Gagarin became an instant global hero</li>
            </ul>`,
        search: 'Vostok Gagarin'
    },
    'apollo-11': {
        icon: '🌙',
        title: 'Apollo 11 Moon Landing',
        description: 'On July 20-21, 1969, humans landed on the Moon for the first time during the Apollo 11 mission.',
        content: `<h4>Greatest Achievement</h4>
            <p>Apollo 11 remains humanity's greatest space exploration achievement. Neil Armstrong and Buzz Aldrin became the first humans to walk on the Moon, while Michael Collins orbited above.</p>
            <h4>Mission Highlights</h4>
            <ul>
                <li>Launch: July 16, 1969</li>
                <li>Moon landing: July 20, 1969 at 20:17 UTC</li>
                <li>Moonwalk duration: 2.5 hours</li>
                <li>Moon rocks collected: 47.5 pounds</li>
                <li>Neil Armstrong's famous words: "That's one small step for man, one giant leap for mankind"</li>
            </ul>`,
        search: 'Apollo 11 Moon'
    },
    'hubble': {
        icon: '🔭',
        title: 'Hubble Space Telescope',
        description: 'Launched in 1990, Hubble has revolutionized astronomy with its stunning observations of the universe.',
        content: `<h4>A Cosmic Eye</h4>
            <p>The Hubble Space Telescope orbits Earth at 17,500 mph, observing objects in space with incredible clarity. It has fundamentally changed our understanding of the universe.</p>
            <h4>Hubble Achievements</h4>
            <ul>
                <li>Determined the age of the universe (13.8 billion years)</li>
                <li>Discovered that the universe is expanding at an accelerating rate</li>
                <li>Taken over 1.5 million images</li>
                <li>Observed the deepest regions of space (Hubble Deep Field)</li>
                <li>Still operating after 30+ years in orbit</li>
            </ul>`,
        search: 'Hubble Space Telescope'
    },
    'iss': {
        icon: '🛰️',
        title: 'International Space Station',
        description: 'Construction began in 1998, and the ISS has hosted continuous human presence in space for over 20 years.',
        content: `<h4>Orbital Outpost</h4>
            <p>The ISS is a habitable artificial satellite in low Earth orbit. It serves as a space environment research laboratory and is a joint project of NASA, ESA, Roscosmos, JAXA, and CSA.</p>
            <h4>ISS Facts</h4>
            <ul>
                <li>Orbits Earth every 90 minutes</li>
                <li>Astronauts see 16 sunrises and sunsets per day</li>
                <li>Crew capacity: 6 people</li>
                <li>Size: 357 feet long (larger than a football field)</li>
                <li>Travels at 17,500 mph around Earth</li>
            </ul>`,
        search: 'International Space Station'
    },
    'jwst': {
        icon: '🔭',
        title: 'James Webb Space Telescope',
        description: 'Launched in December 2021, JWST observes the universe in infrared, peering back to the earliest galaxies.',
        content: `<h4>Next Generation Observatory</h4>
            <p>The James Webb Space Telescope is the most powerful space telescope ever built. It observes primarily in the infrared spectrum, allowing it to see through dust clouds and look back nearly to the Big Bang.</p>
            <h4>JWST Capabilities</h4>
            <ul>
                <li>Detects objects 13.6 billion years in the past</li>
                <li>Primary mirror: 6.5 meters in diameter (21 feet)</li>
                <li>Cost: $10 billion</li>
                <li>Operating at Lagrange Point 2, 930,000 miles from Earth</li>
                <li>Already making groundbreaking discoveries about the early universe</li>
            </ul>`,
        search: 'James Webb Space Telescope'
    },
    'artemis': {
        icon: '🚀',
        title: 'Artemis Missions',
        description: 'Artemis missions aim to return humans to the Moon and establish a sustainable presence on the lunar surface.',
        content: `<h4>Back to the Moon</h4>
            <p>NASA's Artemis program is designed to land the first woman and person of color on the Moon. The missions will also establish a lunar gateway station and prepare for eventual Mars exploration.</p>
            <h4>Artemis Timeline</h4>
            <ul>
                <li>Artemis I: Uncrewed lunar flyby (scheduled)</li>
                <li>Artemis II: Crewed lunar flyby (scheduled)</li>
                <li>Artemis III: Lunar landing (scheduled for late 2020s)</li>
                <li>Goal: Sustainable lunar base for research and exploration</li>
                <li>Stepping stone to human Mars missions</li>
            </ul>`,
        search: 'Artemis Moon'
    },
    'venus-day': {
        icon: '🌍',
        title: 'Venus\'s Unique Day-Year Cycle',
        description: 'Venus takes 243 Earth days to rotate once, but only 225 to orbit the Sun—making a day longer than a year!',
        content: `<h4>Backwards Planet</h4>
            <p>Venus is unique in our solar system. It rotates backwards (compared to most planets) and rotates so slowly that a day on Venus is longer than its year. Additionally, the sun rises in the west and sets in the east.</p>
            <h4>Venus Statistics</h4>
            <ul>
                <li>Rotation period: 243 Earth days</li>
                <li>Orbital period: 225 Earth days</li>
                <li>Surface temperature: 462°C (864°F)</li>
                <li>Atmospheric pressure: 92 times Earth's</li>
                <li>Atmosphere composition: 96% CO2, thick clouds of sulfuric acid</li>
            </ul>`,
        search: 'Venus planet'
    },
    'saturn-rings': {
        icon: '💫',
        title: 'Saturn\'s Floatability',
        description: 'Saturn is the only planet less dense than water—it would float in an ocean large enough to hold it!',
        content: `<h4>The Floating Giant</h4>
            <p>Saturn's density is only 0.687 g/cm³, less than water's 1 g/cm³. If you could find an ocean large enough, Saturn would float in it. This is because Saturn is a gas giant with relatively low mass for its enormous volume.</p>
            <h4>Saturn Facts</h4>
            <ul>
                <li>Density: 0.687 g/cm³ (less than water)</li>
                <li>Ring system: Extends 175,000 miles from the planet</li>
                <li>Moons: At least 80+ moons</li>
                <li>Day length: 10.7 hours</li>
                <li>Wind speeds: Up to 1,100 mph in the upper atmosphere</li>
            </ul>`,
        search: 'Saturn rings'
    },
    'neutron-stars': {
        icon: '⭐',
        title: 'Neutron Star Density',
        description: 'A teaspoon of neutron star material would weigh about one billion tons on Earth!',
        content: `<h4>Extreme Density</h4>
            <p>Neutron stars are the collapsed cores of massive stars that exploded as supernovas. They are so dense that a teaspoon of material would weigh as much as Mount Everest.</p>
            <h4>Neutron Star Properties</h4>
            <ul>
                <li>Typical diameter: 12-13 miles (20 km)</li>
                <li>Mass: 1.4-2 times the Sun's mass</li>
                <li>Density: 1 billion tons per teaspoon</li>
                <li>Spin rates: Can rotate 716 times per second</li>
                <li>Temperature: Surface can exceed 1 million Kelvin</li>
            </ul>`,
        search: 'Neutron star'
    },
    'voyager-1': {
        icon: '🚀',
        title: 'Voyager 1 - Humanity\'s Messenger',
        description: 'Launched in 1977, Voyager 1 remains humanity\'s most distant object, traveling through interstellar space.',
        content: `<h4>Journey to the Stars</h4>
            <p>Voyager 1 is the farthest human-made object from Earth, having traveled over 14 billion miles. It continues sending data back to Earth after more than 45 years of operation.</p>
            <h4>Voyager 1 Mission</h4>
            <ul>
                <li>Launch date: September 5, 1977</li>
                <li>Current distance: 14.5+ billion miles from Earth</li>
                <li>Speed: 37,000 mph relative to the Sun</li>
                <li>Carries a golden record with sounds and images from Earth</li>
                <li>Entered interstellar space in August 2012</li>
            </ul>`,
        search: 'Voyager'
    },
    'iss-sunrises': {
        icon: '🌅',
        title: 'ISS Sunrise Frequency',
        description: 'Astronauts on the ISS experience about 16 sunrises and sunsets every 24 hours.',
        content: `<h4>Rapid Celestial Dance</h4>
            <p>Because the ISS orbits Earth every 90 minutes at an altitude of 250 miles, astronauts experience a sunrise or sunset approximately every 45 minutes. This means they see 16 sunrises and 16 sunsets in a 24-hour period.</p>
            <h4>ISS Orbit Facts</h4>
            <ul>
                <li>Orbital period: 90 minutes</li>
                <li>Altitude: 250 miles (408 km)</li>
                <li>Speed: 17,500 mph</li>
                <li>Completes 16 orbits per day</li>
                <li>Each sunrise/sunset visible from ISS: 8 minutes of duration</li>
            </ul>`,
        search: 'International Space Station sunrise'
    },
    'moonwalk': {
        icon: '👨‍🚀',
        title: 'Apollo 11 Moonwalk',
        description: 'Apollo 11 astronauts spent just 2.5 hours on the lunar surface, yet changed human history forever.',
        content: `<h4>Historic Steps</h4>
            <p>Neil Armstrong and Buzz Aldrin spent 2 hours and 31 minutes exploring the Moon's surface during Apollo 11. Despite the short duration, they collected critical samples and placed scientific instruments that still transmit data today.</p>
            <h4>Moonwalk Details</h4>
            <ul>
                <li>Duration: 2 hours 31 minutes</li>
                <li>Distance covered: 250 meters (820 feet)</li>
                <li>Samples collected: 47.5 pounds of moon rocks</li>
                <li>Slowest walking speed: Due to bulky spacesuits (1 mph)</li>
                <li>Created the famous footprints and shadows in lunar dust</li>
            </ul>`,
        search: 'Apollo 11 moonwalk'
    },
    'exoplanets': {
        icon: '🌎',
        title: 'Exoplanet Discoveries',
        description: 'Over 5,000 confirmed exoplanets have been discovered, with thousands more waiting to be confirmed.',
        content: `<h4>Worlds Beyond Our Sun</h4>
            <p>Exoplanets are planets that orbit stars other than our Sun. The first exoplanet was discovered in 1992 around a pulsar, and in 1995 around a sun-like star. Since then, we've discovered thousands.</p>
            <h4>Exoplanet Facts</h4>
            <ul>
                <li>Over 5,000 confirmed exoplanets discovered</li>
                <li>Many more candidates awaiting confirmation</li>
                <li>Types include hot Jupiters, super-Earths, and terrestrial planets</li>
                <li>Some may be in the habitable zone where liquid water could exist</li>
                <li>Discovery methods: Transit, radial velocity, direct imaging</li>
            </ul>`,
        search: 'Exoplanet'
    },
    'light-speed': {
        icon: '💨',
        title: 'Interstellar Travel Challenge',
        description: 'It would take 70,000 years to reach the nearest star system at current spacecraft speeds.',
        content: `<h4>The Great Distances</h4>
            <p>Space is incomprehensibly vast. Even with our fastest spacecraft, traveling to other star systems would take tens of thousands of years. This illustrates the challenge of interstellar exploration.</p>
            <h4>Travel Times (at current speeds)</h4>
            <ul>
                <li>To Proxima Centauri: 73,000 years</li>
                <li>To Alpha Centauri: 140,000 years</li>
                <li>To Sirius: 540,000 years</li>
                <li>Voyager 1 speed: 37,000 mph (60,000 km/h)</li>
                <li>Light takes 4.37 years to reach Proxima Centauri from Earth</li>
            </ul>`,
        search: 'Star light year'
    }
};

function showInfoModal(id) {
    const info = infoContent[id];
    if (!info) return;

    const modal = new bootstrap.Modal(document.getElementById('infoModal'));
    document.getElementById('modalInfoIcon').textContent = info.icon;
    document.getElementById('modalInfoTitle').textContent = info.title;
    document.getElementById('modalInfoDescription').textContent = info.description;
    document.getElementById('modalInfoBody').innerHTML = info.content;
    document.getElementById('modalSearchBtn').dataset.search = info.search;

    modal.show();
}

function searchFromInfoModal() {
    const searchTerm = document.getElementById('modalSearchBtn').dataset.search;
    if (searchTerm) {
        const modal = bootstrap.Modal.getInstance(document.getElementById('infoModal'));
        modal.hide();
        searchMedia(searchTerm);
    }
}

// --- Utility ---
function showLoading(show) {
    document.getElementById('loadingOverlay').classList.toggle('d-none', !show);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// --- Starfield backdrop (purely decorative, self-contained) ---
function initStarfield() {
    const canvas = document.getElementById('starfield');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    let stars = [];
    let width, height;

    function resize() {
        width = canvas.width = window.innerWidth;
        height = canvas.height = window.innerHeight;
        const count = Math.round((width * height) / 9000);
        stars = Array.from({ length: count }, () => ({
            x: Math.random() * width,
            y: Math.random() * height,
            r: Math.random() * 1.2 + 0.3,
            phase: Math.random() * Math.PI * 2,
            speed: Math.random() * 0.015 + 0.005,
            hue: Math.random() < 0.15 ? '176, 216, 230' : '238, 242, 251' // occasional cyan tint
        }));
    }

    function drawStatic() {
        ctx.clearRect(0, 0, width, height);
        stars.forEach(s => {
            ctx.beginPath();
            ctx.fillStyle = `rgba(${s.hue}, 0.6)`;
            ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
            ctx.fill();
        });
    }

    function animate(t) {
        ctx.clearRect(0, 0, width, height);
        stars.forEach(s => {
            const twinkle = 0.4 + 0.6 * Math.abs(Math.sin(s.phase + t * s.speed));
            ctx.beginPath();
            ctx.fillStyle = `rgba(${s.hue}, ${twinkle.toFixed(2)})`;
            ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
            ctx.fill();
        });
        requestAnimationFrame(animate);
    }

    resize();
    window.addEventListener('resize', resize);

    if (reduceMotion) {
        drawStatic();
    } else {
        requestAnimationFrame(animate);
    }
}

document.addEventListener('DOMContentLoaded', initStarfield);

// --- Discover feed: populate the landing page before any search ---
const DISCOVER_TOPICS = [
    'Apollo 11', 'Hubble Space Telescope', 'James Webb Space Telescope',
    'Mars rover', 'International Space Station', 'Earth from space',
    'Saturn rings', 'Nebula', 'Black hole', 'Artemis moon mission',
    'Voyager', 'Milky Way'
];

const SPACE_FACTS = [
    "The footprints Apollo astronauts left on the Moon will likely stay visible for millions of years — there's no wind or water there to erode them.",
    "A day on Venus is longer than its year: it takes 243 Earth days to rotate once, but only 225 to orbit the Sun.",
    "Voyager 1, launched in 1977, is the most distant human-made object from Earth and has been travelling in interstellar space since 2012.",
    "The International Space Station orbits Earth roughly every 90 minutes, so astronauts on board see about 16 sunrises a day.",
    "A neutron star is so dense that a teaspoon of its material would weigh about a billion tons on Earth.",
    "The James Webb Space Telescope observes in infrared, letting it see through cosmic dust clouds that block visible light entirely.",
    "Saturn is the only planet in the solar system less dense than water — it would float, if you had a big enough bathtub.",
    "The Hubble Space Telescope has been observing the universe continuously since 1990, over three decades of images.",
    "One million Earths could fit inside the Sun.",
    "Neil Armstrong and Buzz Aldrin spent about two and a half hours outside the lunar module on the Apollo 11 moonwalk."
];

async function loadDiscoverFeed() {
    const titleEl = document.getElementById('resultsTitle');
    if (!titleEl) return;
    titleEl.textContent = 'Loading popular imagery…';

    const topics = [...DISCOVER_TOPICS].sort(() => Math.random() - 0.5).slice(0, 3);

    try {
        const results = await Promise.all(
            topics.map(topic =>
                fetch(`${API_BASE}/media/search?query=${encodeURIComponent(topic)}`)
                    .then(res => res.ok ? res.json() : [])
                    .catch(() => [])
            )
        );

        const seen = new Set();
        const merged = [];
        results.forEach(list => {
            list.slice(0, 6).forEach(item => {
                if (!seen.has(item.id)) {
                    seen.add(item.id);
                    merged.push(item);
                }
            });
        });
        merged.sort(() => Math.random() - 0.5);

        if (merged.length > 0 && currentMedia.length === 0) {
            currentMedia = merged;
            renderMediaGrid(currentMedia, 'movieGrid');
            titleEl.innerHTML = '<i class="bi bi-stars"></i> Popular right now';
            document.getElementById('noResults').classList.add('d-none');
        } else if (currentMedia.length === 0) {
            titleEl.textContent = "Search NASA's image and video library to get started";
        }
    } catch (err) {
        console.error('Failed to load discover feed', err);
        if (currentMedia.length === 0) {
            titleEl.textContent = "Search NASA's image and video library to get started";
        }
    }
}

function loadRandomFact() {
    const el = document.getElementById('spaceFact');
    if (!el) return;
    el.textContent = SPACE_FACTS[Math.floor(Math.random() * SPACE_FACTS.length)];
}

document.addEventListener('DOMContentLoaded', () => {
    loadRandomFact();
    loadDiscoverFeed();
});