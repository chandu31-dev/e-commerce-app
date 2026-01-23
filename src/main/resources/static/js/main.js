// API Base URL
const API_BASE = '';

// Get JWT Token from localStorage or cookie
function getToken() {
    // First try to get from localStorage (set after login)
    let token = localStorage.getItem('JWT_TOKEN');
    if (token) {
        return token;
    }
    
    // Fallback to cookie (for backward compatibility)
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'JWT_TOKEN') {
            return value;
        }
    }
    return null;
}

// Make authenticated API request
async function apiRequest(url, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    const response = await fetch(url, {
        ...options,
        headers
    });
    
    return response;
}

// Add to Cart
async function addToCart(productId, quantity = 1) {
    try {
        const formData = new URLSearchParams();
        formData.append('productId', productId);
        formData.append('quantity', quantity);
        
        const token = getToken();
        const headers = {
            'Content-Type': 'application/x-www-form-urlencoded'
        };
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(`${API_BASE}/cart/api/add`, {
            method: 'POST',
            headers: headers,
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            showMessage('Product added to cart!', 'success');
            updateCartCount();
        } else {
            if (result.message.includes('login')) {
                window.location.href = '/login';
            } else {
                showMessage(result.message || 'Error adding to cart', 'error');
            }
        }
    } catch (error) {
        showMessage('Error adding to cart', 'error');
    }
}

// Add to Wishlist
async function addToWishlist(productId) {
    try {
        const btn = document.querySelector(`.wishlist-btn[data-pid="${productId}"]`);
        const isIn = btn && btn.classList.contains('in-wishlist');

        const formData = new URLSearchParams();
        formData.append('productId', productId);

        const token = getToken();
        const headers = {
            'Content-Type': 'application/x-www-form-urlencoded'
        };
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const url = isIn ? `${API_BASE}/wishlist/api/remove` : `${API_BASE}/wishlist/api/add`;
        const response = await fetch(url, { method: 'POST', headers, body: formData });
        const json = await response.json().catch(()=>null);

        if (response.ok) {
            // toggle button UI
            if (btn) {
                if (isIn) {
                    btn.textContent = '♡ Wishlist';
                    btn.classList.remove('in-wishlist');
                } else {
                    btn.textContent = '♥ In Wishlist';
                    btn.classList.add('in-wishlist');
                }
            }
            showMessage(isIn ? 'Removed from wishlist' : 'Added to wishlist', 'success');
        } else {
            if (json && json.message && json.message.includes('login')) {
                window.location.href = '/login';
            } else {
                showMessage((json && json.message) || 'Failed to update wishlist', 'error');
            }
        }
    } catch (error) {
        showMessage('Error updating wishlist', 'error');
    }
}

// Update Cart Item Quantity
async function updateCartQuantity(cartItemId, quantity) {
    try {
        const formData = new URLSearchParams();
        formData.append('quantity', quantity);
        
        const token = getToken();
        const headers = {
            'Content-Type': 'application/x-www-form-urlencoded'
        };
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(`${API_BASE}/cart/api/update/${cartItemId}`, {
            method: 'PUT',
            headers: headers,
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            location.reload();
        } else {
            showMessage(result.message || 'Error updating cart', 'error');
        }
    } catch (error) {
        showMessage('Error updating cart', 'error');
    }
}

// Remove from Cart
async function removeFromCart(cartItemId) {
    if (!confirm('Are you sure you want to remove this item from cart?')) {
        return;
    }
    
    try {
        const token = getToken();
        const headers = {};
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(`${API_BASE}/cart/api/remove/${cartItemId}`, {
            method: 'DELETE',
            headers: headers
        });
        
        const result = await response.json();
        
        if (result.success) {
            showMessage('Item removed from cart', 'success');
            location.reload();
        } else {
            showMessage(result.message || 'Error removing item', 'error');
        }
    } catch (error) {
        showMessage('Error removing item', 'error');
    }
}

// Place Order
async function placeOrder() {
    try {
        const token = getToken();
        console.log('Token exists:', !!token);
        
        if (!token) {
            showMessage('Please login to place an order', 'error');
            window.location.href = '/login';
            return;
        }

        console.log('Fetching addresses from /api/addresses');
        
        // First, fetch user's addresses
        const addressResponse = await fetch(`${API_BASE}/api/addresses`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        console.log('Address response status:', addressResponse.status);
        console.log('Address response ok:', addressResponse.ok);

        if (!addressResponse.ok) {
            if (addressResponse.status === 401) {
                showMessage('Please login to place an order', 'error');
                window.location.href = '/login';
            } else {
                showMessage('Error loading addresses. Status: ' + addressResponse.status, 'error');
            }
            return;
        }

        const addresses = await addressResponse.json();
        console.log('Addresses received:', addresses);
        console.log('Number of addresses:', addresses ? addresses.length : 0);

        if (!addresses || addresses.length === 0) {
            console.log('No addresses found');
            showMessage('Please add a delivery address first', 'error');
            setTimeout(() => {
                window.location.href = '/addresses';
            }, 1500);
            return;
        }

        console.log('Showing address modal with', addresses.length, 'addresses');
        // Show address selection modal
        displayAddressModal(addresses);
    } catch (error) {
        console.error('Error in placeOrder:', error);
        showMessage('Error loading addresses: ' + error.message, 'error');
    }
}

// Display Address Selection Modal
function displayAddressModal(addresses) {
    const addressList = document.getElementById('addressList');
    addressList.innerHTML = '';

    addresses.forEach(address => {
        const isDefault = address.isDefault ? ' (Default)' : '';
        const addressDiv = document.createElement('div');
        addressDiv.style.cssText = 'padding: 1rem; border: 2px solid #dee2e6; margin-bottom: 0.5rem; border-radius: 4px; cursor: pointer; transition: all 0.3s;';
        addressDiv.className = 'address-option';
        addressDiv.innerHTML = `
            <input type="radio" name="selectedAddress" value="${address.id}" style="margin-right: 0.5rem;">
            <label style="cursor: pointer;">
                <strong>${address.label}</strong>${isDefault}<br/>
                ${address.address}<br/>
                <small>Phone: ${address.phone}</small>
            </label>
        `;
        addressDiv.onclick = () => {
            document.querySelector(`input[value="${address.id}"]`).checked = true;
            // Highlight selected
            document.querySelectorAll('.address-option').forEach(el => {
                el.style.borderColor = '#dee2e6';
                el.style.background = 'white';
            });
            addressDiv.style.borderColor = '#007bff';
            addressDiv.style.background = '#f0f8ff';
        };
        addressList.appendChild(addressDiv);

        // Auto-select default address
        if (address.isDefault) {
            addressDiv.click();
        }
    });

    document.getElementById('addressModal').style.display = 'block';
}

// Close Address Modal
function closeAddressModal() {
    document.getElementById('addressModal').style.display = 'none';
}

// Confirm Order with Selected Address
async function confirmOrderWithAddress() {
    const selectedAddressInput = document.querySelector('input[name="selectedAddress"]:checked');
    
    if (!selectedAddressInput) {
        showMessage('Please select a delivery address', 'error');
        return;
    }

    const addressId = selectedAddressInput.value;

    if (!confirm('Proceed to payment?')) {
        return;
    }

    closeAddressModal();

    try {
        const token = getToken();
        const headers = {
            'Content-Type': 'application/json'
        };
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const couponCode = document.getElementById('couponInput') ? document.getElementById('couponInput').value : null;

        // Compute cart total on client
        let amount = 0;
        try {
            const cartResp = await fetch(`${API_BASE}/cart/api/items`, { headers });
            if (cartResp.ok) {
                const cartItems = await cartResp.json();
                amount = (cartItems || []).reduce((s, ci) => {
                    const unit = (ci.product && (ci.product.price || ci.product.unitPrice)) || ci.unitPrice || 0;
                    return s + (parseFloat(unit || 0) * (ci.quantity || 1));
                }, 0);
            }
        } catch (e) {
            // ignore - fallback
        }

        const payload = { addressId: parseInt(addressId), amount: amount, currency: 'INR' };
        if (couponCode) payload.couponCode = couponCode;

        const response = await fetch(`${API_BASE}/api/internal-payments/initiate`, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const err = await response.json().catch(()=>null);
            showMessage((err && err.message) || 'Error initiating payment', 'error');
            return;
        }

        const location = response.headers.get('Location') || response.headers.get('location');
        let paymentId = null;
        if (location) {
            const parts = location.split('/');
            paymentId = parts[parts.length-1];
        } else {
            const respJson = await response.json().catch(()=>null);
            if (respJson && respJson.paymentId) paymentId = respJson.paymentId;
        }

        if (!paymentId) {
            showMessage('Could not initiate payment', 'error');
            return;
        }

        showMessage('Payment initiated. Redirecting to payment page...', 'success');
        setTimeout(() => { window.location.href = '/payments?paymentId=' + paymentId; }, 800);
    } catch (error) {
        showMessage('Error initiating payment', 'error');
    }
}

// Show Message
function showMessage(message, type = 'success') {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message message-${type}`;
    messageDiv.textContent = message;
    
    document.body.insertBefore(messageDiv, document.body.firstChild);
    
    setTimeout(() => {
        messageDiv.remove();
    }, 3000);
}

// Update Cart Count
async function updateCartCount() {
    try {
        const token = getToken();
        const headers = {};
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(`${API_BASE}/cart/api/items`, {
            headers: headers
        });
        
        const items = await response.json();
        const count = items.length;
        
        const cartCountElement = document.getElementById('cart-count');
        if (cartCountElement) {
            cartCountElement.textContent = count;
            cartCountElement.style.display = count > 0 ? 'inline' : 'none';
        }
    } catch (error) {
        // Ignore errors
    }
}

// Add Wishlist nav item dynamically (so header across templates shows it)
function addWishlistNavItem() {
    try {
        const nav = document.querySelector('.nav-links');
        if (!nav) return;
        // don't add twice
        if (document.getElementById('nav-wishlist-link')) return;

        const li = document.createElement('li');
        li.id = 'nav-wishlist-link';
        li.style = '';
        const a = document.createElement('a');
        a.href = '/wishlist';
        a.innerHTML = `Wishlist (<span id="wishlist-count">0</span>)`;
        li.appendChild(a);

        // Try to place after Cart link if present
        const cartLink = Array.from(nav.querySelectorAll('a')).find(el => /\/cart/.test(el.getAttribute('href')));
        if (cartLink && cartLink.parentElement) {
            cartLink.parentElement.insertAdjacentElement('afterend', li);
        } else {
            nav.appendChild(li);
        }
    } catch (e) {
        // ignore
    }
}

// Update Wishlist Count in nav
async function updateWishlistCount() {
    try {
        const token = getToken();
        if (!token) return; // only for logged-in users
        const headers = { 'Authorization': `Bearer ${token}` };
        const res = await fetch(`${API_BASE}/wishlist/api/ids`, { headers });
        if (!res.ok) return;
        const ids = await res.json();
        const count = Array.isArray(ids) ? ids.length : 0;
        const el = document.getElementById('wishlist-count');
        if (el) {
            el.textContent = count;
            el.style.display = count > 0 ? 'inline' : 'none';
        }
    } catch (e) {
        // ignore
    }
}

// Admin Functions
async function createProduct() {
    const name = document.getElementById('product-name').value;
    const description = document.getElementById('product-description').value;
    const category = document.getElementById('product-category').value;
    const price = document.getElementById('product-price').value;
    const imageURL = document.getElementById('product-image').value;
    const stock = document.getElementById('product-stock').value;
    
    try {
        const formData = new URLSearchParams();
        formData.append('name', name);
        formData.append('description', description);
        formData.append('category', category);
        formData.append('price', price);
        formData.append('imageURL', imageURL);
        formData.append('stock', stock);
        
        const token = getToken();
        const headers = {
            'Content-Type': 'application/x-www-form-urlencoded'
        };
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(`${API_BASE}/admin/api/products`, {
            method: 'POST',
            headers: headers,
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            showMessage('Product created successfully!', 'success');
            document.getElementById('product-form').reset();
            setTimeout(() => location.reload(), 1500);
        } else {
            showMessage(result.message || 'Error creating product', 'error');
        }
    } catch (error) {
        showMessage('Error creating product', 'error');
    }
}

async function deleteProduct(productId) {
    if (!confirm('Are you sure you want to delete this product?')) {
        return;
    }
    
    try {
        const token = getToken();
        const headers = {};
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(`${API_BASE}/admin/api/products/${productId}`, {
            method: 'DELETE',
            headers: headers
        });
        
        const result = await response.json();
        
        if (result.success) {
            showMessage('Product deleted successfully!', 'success');
            setTimeout(() => location.reload(), 1500);
        } else {
            showMessage(result.message || 'Error deleting product', 'error');
        }
    } catch (error) {
        showMessage('Error deleting product', 'error');
    }
}

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    addWishlistNavItem();
    updateCartCount();
    updateWishlistCount();
});

// Load wishlist ids for current user and mark buttons
async function markWishlistButtons() {
    try {
        const token = getToken();
        const headers = {};
        if (token) headers['Authorization'] = `Bearer ${token}`;
        const res = await fetch(`${API_BASE}/wishlist/api/ids`, { headers });
        if (!res.ok) return;
        const ids = await res.json();
        if (!Array.isArray(ids)) return;
        document.querySelectorAll('.wishlist-btn').forEach(btn => {
            const pid = btn.getAttribute('data-pid');
            if (!pid) return;
            if (ids.includes(Number(pid))) {
                btn.textContent = '♥ In Wishlist';
                btn.classList.add('in-wishlist');
            } else {
                btn.textContent = '♡ Wishlist';
                btn.classList.remove('in-wishlist');
            }
        });
    } catch (e) {
        // ignore
    }
}

// Run marking after DOM ready
document.addEventListener('DOMContentLoaded', function() { markWishlistButtons(); });

// Logout function
function logout() {
    localStorage.removeItem('JWT_TOKEN');
    window.location.href = '/logout';
}

