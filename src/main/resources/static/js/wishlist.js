async function loadWishlist(page = 0, size = 20) {
  window._wishPage = page;
  window._wishSize = size;
  const res = await fetch('/wishlist/api/items?page=' + page + '&size=' + size);
  const msg = document.getElementById('message');
  const container = document.getElementById('wishlist-container');
  if (!res.ok) {
    try { msg.textContent = await res.text(); } catch(e){ msg.textContent = 'Unable to load wishlist'; }
    container.innerHTML = '';
    return;
  }
  msg.textContent = '';
  const body = await res.json();
  const items = body.content || body;
  container.innerHTML = '';
  if (!items || items.length === 0) {
    container.innerHTML = '<p>No items in your wishlist yet.</p>';
    return;
  }
  items.forEach(item => {
    const p = document.createElement('div');
    const title = item.productName || 'Product';
    const pid = item.productId || (item.product && item.product.id) || item.id;
    p.innerHTML = `<div style="padding:8px;border-bottom:1px solid #eee;">
      <a href="/products/${pid}">${escapeHtml(title)}</a>
      <button data-id="${pid}" style="margin-left:12px;">Remove</button>
    </div>`;
    container.appendChild(p);
  });
  container.querySelectorAll('button[data-id]').forEach(b => b.addEventListener('click', e => {
    const id = e.currentTarget.getAttribute('data-id');
    removeFromWishlist(id);
  }));

  // pagination controls
  try {
    const pageInfo = document.getElementById('wish-page-info');
    const prev = document.getElementById('wish-prev');
    const next = document.getElementById('wish-next');
    const current = body.number || 0;
    const totalPages = body.totalPages || 1;
    pageInfo.textContent = `Page ${current+1} of ${totalPages}`;
    if (prev) prev.style.display = current > 0 ? 'inline-block' : 'none';
    if (next) next.style.display = (current+1) < totalPages ? 'inline-block' : 'none';
  } catch(e) {}
}

async function removeFromWishlist(productId) {
  const res = await fetch('/wishlist/api/remove', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ productId })
  });
  if (res.ok) {
    loadWishlist(window._wishPage || 0);
  } else {
    try { const j = await res.json(); alert(j.message || 'Failed to remove'); } catch(e){ alert('Failed to remove'); }
  }
}

function escapeHtml(s){ return String(s).replace(/[&<>"']/g, c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }

// Expose for inline initialization
window.loadWishlist = loadWishlist;
