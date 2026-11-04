/* Client-side search, mobile nav and theme toggle.
 *
 * No framework and no build step: the index is a few KB of JSON and the whole
 * interaction is a substring match. A search dependency would be larger than
 * the thing it searches. */
(function () {
  'use strict';

  /* ---------- theme ---------- */

  var root = document.documentElement;
  var KEY = 'rectify-theme';

  function systemPrefersDark() {
    return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  function currentTheme() {
    return root.dataset.theme || (systemPrefersDark() ? 'dark' : 'light');
  }

  var toggle = document.querySelector('.theme-toggle');
  if (toggle) {
    toggle.addEventListener('click', function () {
      var next = currentTheme() === 'dark' ? 'light' : 'dark';
      root.dataset.theme = next;
      try { localStorage.setItem(KEY, next); } catch (e) { /* private mode */ }
    });
  }

  /* ---------- mobile nav ---------- */

  var navToggle = document.querySelector('.nav-toggle');
  var sidebar = document.querySelector('.sidebar');
  if (navToggle && sidebar) {
    navToggle.addEventListener('click', function () {
      var open = sidebar.classList.toggle('open');
      navToggle.setAttribute('aria-expanded', String(open));
    });
  }

  /* ---------- table of contents highlight ---------- */

  var tocLinks = Array.prototype.slice.call(document.querySelectorAll('.toc a'));
  if (tocLinks.length && 'IntersectionObserver' in window) {
    var byId = {};
    tocLinks.forEach(function (a) { byId[a.getAttribute('href').slice(1)] = a; });
    var seen = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        var link = byId[entry.target.id];
        if (!link) return;
        if (entry.isIntersecting) {
          tocLinks.forEach(function (a) { a.classList.remove('active'); });
          link.classList.add('active');
        }
      });
    }, { rootMargin: '-80px 0px -72% 0px' });
    Object.keys(byId).forEach(function (id) {
      var el = document.getElementById(id);
      if (el) seen.observe(el);
    });
  }

  /* ---------- search ---------- */

  var input = document.getElementById('search-input');
  var panel = document.getElementById('search-results');
  if (!input || !panel) return;

  var index = null;
  var cursor = -1;

  // Both URLs below hang off the stylesheet's own href rather than an assumed absolute
  // path: it is the one element on the page that already knows where the site root is,
  // whether that is "/", "/repo-name/" or a pile of "../" in relative mode.
  function assetsBase() {
    var link = document.querySelector('link[rel="stylesheet"][href$="styles.css"]');
    return link ? link.getAttribute('href').replace(/styles\.css$/, '') : 'assets/';
  }

  function indexUrl() {
    return assetsBase() + 'search-index.json';
  }

  // Entries are stored root-relative ("guide/devices/") so one index serves every page.
  function pageUrl(slug) {
    return assetsBase().replace(/assets\/$/, '') + slug;
  }

  function load() {
    if (index) return Promise.resolve(index);
    return fetch(indexUrl())
      .then(function (r) { return r.json(); })
      .then(function (data) { index = data; return index; })
      .catch(function () { index = []; return index; });
  }

  function snippet(text, query) {
    var at = text.toLowerCase().indexOf(query);
    if (at < 0) return text.slice(0, 110) + '…';
    var from = Math.max(0, at - 36);
    return (from > 0 ? '…' : '') + text.slice(from, from + 130).trim() + '…';
  }

  function score(entry, query) {
    var title = entry.title.toLowerCase();
    if (title === query) return 0;
    if (title.indexOf(query) === 0) return 1;
    if (title.indexOf(query) >= 0) return 2;
    if (entry.text.toLowerCase().indexOf(query) >= 0) return 3;
    return -1;
  }

  function render(results, query) {
    cursor = -1;
    if (!results.length) {
      panel.innerHTML = '<p class="search-empty">Nothing matches “' + escapeHtml(query) + '”.</p>';
      panel.hidden = false;
      return;
    }
    panel.innerHTML = results.map(function (entry) {
      return '<a href="' + pageUrl(entry.url) + '">' +
        '<span class="r-section">' + escapeHtml(entry.section) + '</span>' +
        '<span class="r-title">' + escapeHtml(entry.title) + '</span>' +
        '<span class="r-snippet">' + escapeHtml(snippet(entry.text, query)) + '</span>' +
        '</a>';
    }).join('');
    panel.hidden = false;
  }

  function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, function (c) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
    });
  }

  function search() {
    var query = input.value.trim().toLowerCase();
    if (query.length < 2) { panel.hidden = true; return; }
    load().then(function (data) {
      var hits = data
        .map(function (e) { return { entry: e, rank: score(e, query) }; })
        .filter(function (h) { return h.rank >= 0; })
        .sort(function (a, b) { return a.rank - b.rank; })
        .slice(0, 8)
        .map(function (h) { return h.entry; });
      render(hits, query);
    });
  }

  input.addEventListener('input', search);
  input.addEventListener('focus', function () { if (input.value.trim().length >= 2) search(); });

  document.addEventListener('click', function (event) {
    if (!panel.contains(event.target) && event.target !== input) panel.hidden = true;
  });

  // "/" focuses search the way it does on GitHub — but not while the reader is
  // already typing somewhere, which would swallow the character.
  document.addEventListener('keydown', function (event) {
    var tag = (event.target.tagName || '').toLowerCase();
    var typing = tag === 'input' || tag === 'textarea' || event.target.isContentEditable;
    if (event.key === '/' && !typing) { event.preventDefault(); input.focus(); return; }
    if (event.key === 'Escape') { panel.hidden = true; input.blur(); return; }
    if (panel.hidden) return;

    var items = Array.prototype.slice.call(panel.querySelectorAll('a'));
    if (!items.length) return;
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault();
      cursor += event.key === 'ArrowDown' ? 1 : -1;
      if (cursor < 0) cursor = items.length - 1;
      if (cursor >= items.length) cursor = 0;
      items.forEach(function (a) { a.classList.remove('cursor'); });
      items[cursor].classList.add('cursor');
      items[cursor].scrollIntoView({ block: 'nearest' });
    } else if (event.key === 'Enter' && cursor >= 0) {
      event.preventDefault();
      window.location.href = items[cursor].getAttribute('href');
    }
  });
})();
