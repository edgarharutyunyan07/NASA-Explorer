const API_BASE = '/api';

let currentMedia = [];
let favorites = [];
let watchlist = [];
let selectedMedia = null;
let authMode = 'login'; // 'login' or 'register'

// --- Init ---
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();

    document.getElementById('searchInput').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') searchMedia();
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

// --- Search ---
async function searchMedia() {
    const query = document.getElementById('searchInput').value.trim();
    if (!query) return;

    showLoading(true);
    try {
        const res = await fetch(`${API_BASE}/media/search?query=${encodeURIComponent(query)}`);
        if (!res.ok) throw new Error('Search failed');
        currentMedia = await res.json();
        renderMediaGrid(currentMedia, 'movieGrid');
        document.getElementById('resultsTitle').textContent =
            currentMedia.length > 0 ? `Results for "${query}"` : '';
        document.getElementById('noResults').classList.toggle('d-none', currentMedia.length > 0);
    } catch (err) {
        console.error(err);
        document.getElementById('resultsTitle').textContent = 'Error searching NASA media';
    } finally {
        showLoading(false);
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
        renderMediaGrid(favorites, 'favoritesGrid');
        document.getElementById('noFavorites').classList.toggle('d-none', favorites.length > 0);
        // Re-render search results to update heart icons
        if (currentMedia.length > 0) {
            renderMediaGrid(currentMedia, 'movieGrid');
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
        renderMediaGrid(watchlist, 'watchlistGrid', 'watchlist');
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
function renderMediaGrid(items, containerId, mode = 'favorites') {
    const container = document.getElementById(containerId);
    container.innerHTML = items.map(media => {
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

        return `
            <div class="movie-card" onclick="openDetail('${media.id}')">
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
            </div>
        `;
    }).join('');
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

// --- Utility ---
function showLoading(show) {
    document.getElementById('loadingOverlay').classList.toggle('d-none', !show);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
