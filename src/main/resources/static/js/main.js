// API Base URL
const API_BASE = '';

// Get JWT Token from Cookie
function getToken() {
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
    if (!confirm('Are you sure you want to place this order?')) {
        return;
    }
    
    try {
        const token = getToken();
        const headers = {
            'Content-Type': 'application/json'
        };
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(`${API_BASE}/orders/api/place`, {
            method: 'POST',
            headers: headers
        });
        
        const result = await response.json();
        
        if (result.success) {
            showMessage('Order placed successfully!', 'success');
            setTimeout(() => {
                window.location.href = `/orders/${result.orderId}`;
            }, 1500);
        } else {
            showMessage(result.message || 'Error placing order', 'error');
        }
    } catch (error) {
        showMessage('Error placing order', 'error');
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
    updateCartCount();
});

