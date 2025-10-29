/**
 * Cart Management JavaScript
 * Xử lý các thao tác với giỏ hàng
 */

const CartManager = {
    /**
     * Lấy CSRF token từ meta tag hoặc cookie
     */
    getCsrfToken: function() {
        // Try to get from meta tag first
        const metaToken = document.querySelector('meta[name="_csrf"]');
        const metaHeader = document.querySelector('meta[name="_csrf_header"]');
        
        if (metaToken && metaHeader) {
            return {
                token: metaToken.getAttribute('content'),
                header: metaHeader.getAttribute('content')
            };
        }
        
        // Fallback: try to get from cookie
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (name === 'XSRF-TOKEN') {
                return {
                    token: decodeURIComponent(value),
                    header: 'X-XSRF-TOKEN'
                };
            }
        }
        
        return null;
    },

    /**
     * Tạo headers cho fetch request bao gồm CSRF token
     */
    getHeaders: function() {
        const headers = {
            'Content-Type': 'application/json',
        };
        
        const csrf = this.getCsrfToken();
        if (csrf) {
            headers[csrf.header] = csrf.token;
        }
        
        return headers;
    },

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    addToCart: function(variantId, quantity = 1) {
        if (!variantId) {
            this.showNotification('Vui lòng chọn kích thước sản phẩm', 'error');
            return;
        }

        const data = {
            variantId: variantId,
            quantity: quantity
        };

        fetch('/cart/add', {
            method: 'POST',
            headers: this.getHeaders(),
            body: JSON.stringify(data)
        })
        .then(response => {
            // Kiểm tra nếu response là redirect (401/302) hoặc không phải JSON
            const contentType = response.headers.get('content-type');
            if (response.status === 401 || response.redirected) {
                // Người dùng chưa đăng nhập, redirect đến trang login
                this.showNotification('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng', 'warning');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
                throw new Error('Unauthorized');
            }
            
            if (!contentType || !contentType.includes('application/json')) {
                throw new Error('Response is not JSON');
            }
            
            return response.json();
        })
        .then(data => {
            if (data.success) {
                this.showNotification(data.message, 'success');
                this.updateCartCount();
                
                // Nếu đang ở trang giỏ hàng, reload để hiển thị dữ liệu mới
                if (window.location.pathname === '/cart') {
                    setTimeout(() => {
                        window.location.reload();
                    }, 500);
                }
            } else {
                this.showNotification(data.message, 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            if (error.message !== 'Unauthorized') {
                this.showNotification('Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng', 'error');
            }
        });
    },

    /**
     * Hiển thị modal xác nhận
     */
    showConfirmModal: function(title, message, onConfirm) {
        // Xóa modal cũ nếu có
        const oldModal = document.getElementById('confirmModal');
        if (oldModal) {
            oldModal.remove();
        }

        // Tạo modal mới
        const modal = document.createElement('div');
        modal.id = 'confirmModal';
        modal.className = 'confirm-modal-overlay';
        modal.innerHTML = `
            <div class="confirm-modal">
                <div class="confirm-modal-header">
                    <i class="fas fa-exclamation-circle"></i>
                    <h3>${title}</h3>
                </div>
                <div class="confirm-modal-body">
                    <p>${message}</p>
                </div>
                <div class="confirm-modal-footer">
                    <button class="btn-cancel">
                        <i class="fas fa-times"></i> Hủy
                    </button>
                    <button class="btn-confirm">
                        <i class="fas fa-check"></i> Xác nhận
                    </button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        // Add event listeners
        const btnCancel = modal.querySelector('.btn-cancel');
        const btnConfirm = modal.querySelector('.btn-confirm');

        btnCancel.onclick = () => {
            this.closeConfirmModal();
        };

        btnConfirm.onclick = () => {
            this.closeConfirmModal();
            if (onConfirm) onConfirm();
        };

        // Click outside to close
        modal.onclick = (e) => {
            if (e.target === modal) {
                this.closeConfirmModal();
            }
        };

        // Show modal with animation
        setTimeout(() => {
            modal.classList.add('show');
        }, 10);
    },

    /**
     * Đóng modal xác nhận
     */
    closeConfirmModal: function() {
        const modal = document.getElementById('confirmModal');
        if (modal) {
            modal.classList.remove('show');
            setTimeout(() => {
                modal.remove();
            }, 300);
        }
    },

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    removeFromCart: function(itemId) {
        this.showConfirmModal(
            'Xác nhận xóa sản phẩm',
            'Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?',
            () => {
                this.performRemoveFromCart(itemId);
            }
        );
    },

    /**
     * Thực hiện xóa sản phẩm (sau khi xác nhận)
     */
    performRemoveFromCart: function(itemId) {

        fetch(`/cart/remove/${itemId}`, {
            method: 'DELETE',
            headers: this.getHeaders()
        })
        .then(response => {
            const contentType = response.headers.get('content-type');
            if (response.status === 401 || response.redirected) {
                this.showNotification('Vui lòng đăng nhập', 'warning');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
                throw new Error('Unauthorized');
            }
            
            if (!contentType || !contentType.includes('application/json')) {
                throw new Error('Response is not JSON');
            }
            
            return response.json();
        })
        .then(data => {
            if (data.success) {
                this.showNotification(data.message, 'success');
                this.updateCartCount();
                
                // Reload trang để hiển thị giỏ hàng mới
                setTimeout(() => {
                    window.location.reload();
                }, 500);
            } else {
                this.showNotification(data.message, 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            if (error.message !== 'Unauthorized') {
                this.showNotification('Có lỗi xảy ra khi xóa sản phẩm', 'error');
            }
        });
    },

    /**
     * Cập nhật số lượng sản phẩm
     */
    updateQuantity: function(itemId, quantity) {
        if (quantity < 1) {
            this.removeFromCart(itemId);
            return;
        }

        fetch(`/cart/update/${itemId}?quantity=${quantity}`, {
            method: 'PUT',
            headers: this.getHeaders()
        })
        .then(response => {
            const contentType = response.headers.get('content-type');
            if (response.status === 401 || response.redirected) {
                this.showNotification('Vui lòng đăng nhập', 'warning');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
                throw new Error('Unauthorized');
            }
            
            if (!contentType || !contentType.includes('application/json')) {
                throw new Error('Response is not JSON');
            }
            
            return response.json();
        })
        .then(data => {
            if (data.success) {
                this.showNotification(data.message, 'success');
                this.updateCartCount();
                
                // Reload trang để hiển thị giá mới
                setTimeout(() => {
                    window.location.reload();
                }, 500);
            } else {
                this.showNotification(data.message, 'error');
                // Reload để trả về số lượng cũ nếu có lỗi
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            if (error.message !== 'Unauthorized') {
                this.showNotification('Có lỗi xảy ra khi cập nhật số lượng', 'error');
            }
        });
    },

    /**
     * Xóa toàn bộ giỏ hàng
     */
    clearCart: function() {
        this.showConfirmModal(
            'Xác nhận xóa giỏ hàng',
            'Bạn có chắc chắn muốn xóa toàn bộ sản phẩm trong giỏ hàng?',
            () => {
                this.performClearCart();
            }
        );
    },

    /**
     * Thực hiện xóa toàn bộ giỏ hàng (sau khi xác nhận)
     */
    performClearCart: function() {

        fetch('/cart/clear', {
            method: 'DELETE',
            headers: this.getHeaders()
        })
        .then(response => {
            const contentType = response.headers.get('content-type');
            if (response.status === 401 || response.redirected) {
                this.showNotification('Vui lòng đăng nhập', 'warning');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
                throw new Error('Unauthorized');
            }
            
            if (!contentType || !contentType.includes('application/json')) {
                throw new Error('Response is not JSON');
            }
            
            return response.json();
        })
        .then(data => {
            if (data.success) {
                this.showNotification(data.message, 'success');
                this.updateCartCount();
                
                setTimeout(() => {
                    window.location.reload();
                }, 500);
            } else {
                this.showNotification(data.message, 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            if (error.message !== 'Unauthorized') {
                this.showNotification('Có lỗi xảy ra khi xóa giỏ hàng', 'error');
            }
        });
    },

    /**
     * Cập nhật số lượng hiển thị trên icon giỏ hàng
     */
    updateCartCount: function() {
        fetch('/cart/count')
            .then(response => response.json())
            .then(data => {
                // Update cart-count (if exists)
                const cartCountElement = document.querySelector('.cart-count');
                if (cartCountElement && data.count !== undefined) {
                    cartCountElement.textContent = data.count;
                    
                    // Hiển thị hoặc ẩn badge
                    if (data.count > 0) {
                        cartCountElement.style.display = 'inline-block';
                    } else {
                        cartCountElement.style.display = 'none';
                    }
                }
                
                // Update cart-badge (if exists) - for layout/main.html
                const cartBadgeElement = document.querySelector('.cart-badge');
                if (cartBadgeElement && data.count !== undefined) {
                    cartBadgeElement.textContent = data.count;
                    
                    // Hiển thị hoặc ẩn badge
                    if (data.count > 0) {
                        cartBadgeElement.style.display = 'inline-block';
                    } else {
                        cartBadgeElement.style.display = 'none';
                    }
                }
            })
            .catch(error => {
                console.error('Error updating cart count:', error);
            });
    },

    /**
     * Hiển thị thông báo
     */
    showNotification: function(message, type = 'info') {
        // Xóa thông báo cũ nếu có
        const oldNotification = document.querySelector('.cart-notification');
        if (oldNotification) {
            oldNotification.remove();
        }

        // Tạo thông báo mới
        const notification = document.createElement('div');
        notification.className = `cart-notification cart-notification-${type}`;
        notification.textContent = message;

        // Style cho notification
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            border-radius: 5px;
            color: white;
            font-weight: 500;
            z-index: 10000;
            animation: slideIn 0.3s ease-out;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        `;

        // Màu sắc theo loại
        if (type === 'success') {
            notification.style.backgroundColor = '#28a745';
        } else if (type === 'error') {
            notification.style.backgroundColor = '#dc3545';
        } else if (type === 'warning') {
            notification.style.backgroundColor = '#ffc107';
        } else {
            notification.style.backgroundColor = '#17a2b8';
        }

        document.body.appendChild(notification);

        // Tự động ẩn sau 3 giây
        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease-in';
            setTimeout(() => {
                notification.remove();
            }, 300);
        }, 3000);
    },

    /**
     * Khởi tạo các event handlers
     */
    init: function() {
        // Cập nhật số lượng giỏ hàng khi trang load
        this.updateCartCount();

        // Event delegation cho các nút trong giỏ hàng
        document.addEventListener('click', (e) => {
            // Nút xóa sản phẩm
            if (e.target.classList.contains('btn-remove-item')) {
                e.preventDefault();
                const itemId = e.target.dataset.itemId;
                this.removeFromCart(itemId);
            }

            // Nút xóa toàn bộ giỏ hàng
            if (e.target.classList.contains('btn-clear-cart')) {
                e.preventDefault();
                this.clearCart();
            }
        });

        // Event cho input số lượng
        document.addEventListener('change', (e) => {
            if (e.target.classList.contains('quantity-input')) {
                const itemId = e.target.dataset.itemId;
                const quantity = parseInt(e.target.value);
                this.updateQuantity(itemId, quantity);
            }
        });
    }
};

// Thêm CSS animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }

    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }

    @keyframes fadeIn {
        from {
            opacity: 0;
        }
        to {
            opacity: 1;
        }
    }

    @keyframes modalSlideIn {
        from {
            transform: translateY(-20px) scale(0.95);
            opacity: 0;
        }
        to {
            transform: translateY(0) scale(1);
            opacity: 1;
        }
    }

    /* Confirm Modal Styles */
    .confirm-modal-overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10001;
        opacity: 0;
        transition: opacity 0.3s ease;
    }

    .confirm-modal-overlay.show {
        opacity: 1;
    }

    .confirm-modal {
        background: white;
        border-radius: 15px;
        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
        max-width: 450px;
        width: 90%;
        overflow: hidden;
        transform: translateY(-20px) scale(0.95);
        transition: transform 0.3s ease, opacity 0.3s ease;
    }

    .confirm-modal-overlay.show .confirm-modal {
        transform: translateY(0) scale(1);
        animation: modalSlideIn 0.3s ease;
    }

    .confirm-modal-header {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        padding: 25px;
        display: flex;
        align-items: center;
        gap: 15px;
    }

    .confirm-modal-header i {
        font-size: 32px;
        animation: pulse 1s ease-in-out infinite;
    }

    @keyframes pulse {
        0%, 100% {
            transform: scale(1);
        }
        50% {
            transform: scale(1.1);
        }
    }

    .confirm-modal-header h3 {
        font-size: 22px;
        font-weight: 600;
        margin: 0;
    }

    .confirm-modal-body {
        padding: 30px 25px;
    }

    .confirm-modal-body p {
        font-size: 16px;
        line-height: 1.6;
        color: #555;
        margin: 0;
    }

    .confirm-modal-footer {
        padding: 20px 25px;
        background: #f8f9fa;
        display: flex;
        gap: 15px;
        justify-content: flex-end;
    }

    .confirm-modal-footer button {
        padding: 12px 30px;
        border: none;
        border-radius: 8px;
        font-size: 15px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .btn-cancel {
        background: #e0e0e0;
        color: #555;
    }

    .btn-cancel:hover {
        background: #d0d0d0;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .btn-confirm {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
    }

    .btn-confirm:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
    }

    .btn-confirm:active,
    .btn-cancel:active {
        transform: translateY(0);
    }
`;
document.head.appendChild(style);

// Khởi tạo khi DOM đã load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => CartManager.init());
} else {
    CartManager.init();
}
