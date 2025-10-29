/**
 * Wishlist Manager - Quản lý danh sách yêu thích
 */
const WishlistManager = {
    /**
     * Lấy CSRF token
     */
    getCsrfToken: function() {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content;
        return { token, header };
    },

    /**
     * Tạo headers cho request
     */
    getHeaders: function() {
        const headers = {
            'Content-Type': 'application/json'
        };
        
        const csrf = this.getCsrfToken();
        if (csrf.token && csrf.header) {
            headers[csrf.header] = csrf.token;
        }
        
        return headers;
    },

    /**
     * Toggle wishlist - Thêm/xóa sản phẩm
     * Đây là hàm chính để dùng cho nút trái tim
     */
    toggleWishlist: async function(productId, buttonElement) {
        try {
            const response = await fetch(`/wishlist/toggle/${productId}`, {
                method: 'POST',
                headers: this.getHeaders(),
                credentials: 'same-origin'
            });

            // Kiểm tra unauthorized
            if (response.status === 401 || response.redirected) {
                this.showNotification('Vui lòng đăng nhập để thêm sản phẩm yêu thích', 'warning');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
                return;
            }

            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                throw new Error('Response không phải JSON');
            }

            const data = await response.json();

            if (data.success) {
                this.showNotification(data.message, 'success');
                
                // Cập nhật icon trái tim
                if (buttonElement) {
                    const icon = buttonElement.querySelector('i');
                    const isInWishlist = data.data.inWishlist;
                    
                    if (isInWishlist) {
                        // Đã thêm vào wishlist
                        icon.classList.remove('far');
                        icon.classList.add('fas');
                        buttonElement.classList.add('active');
                    } else {
                        // Đã xóa khỏi wishlist
                        icon.classList.remove('fas');
                        icon.classList.add('far');
                        buttonElement.classList.remove('active');
                    }
                }
                
                // Cập nhật số lượng trong header
                this.updateWishlistCount(data.data.count);
                
                // Nếu đang ở trang wishlist, reload lại
                if (window.location.pathname === '/wishlist') {
                    setTimeout(() => window.location.reload(), 800);
                }
            } else {
                this.showNotification(data.message, 'error');
            }
        } catch (error) {
            console.error('Error toggling wishlist:', error);
            this.showNotification('Có lỗi xảy ra khi xử lý yêu cầu', 'error');
        }
    },

    /**
     * Kiểm tra sản phẩm có trong wishlist không
     */
    checkInWishlist: async function(productId) {
        try {
            const response = await fetch(`/wishlist/check/${productId}`);
            const data = await response.json();
            return data.inWishlist || false;
        } catch (error) {
            console.error('Error checking wishlist:', error);
            return false;
        }
    },

    /**
     * Cập nhật số lượng wishlist trong header
     */
    updateWishlistCount: function(count) {
        const badges = document.querySelectorAll('.wishlist-badge, .wishlist-count');
        badges.forEach(badge => {
            if (count !== undefined) {
                badge.textContent = count;
                badge.style.display = count > 0 ? 'inline-block' : 'none';
            }
        });
    },

    /**
     * Load số lượng wishlist
     */
    loadWishlistCount: async function() {
        try {
            const response = await fetch('/wishlist/count');
            const data = await response.json();
            this.updateWishlistCount(data.count);
        } catch (error) {
            console.error('Error loading wishlist count:', error);
        }
    },

    /**
     * Hiển thị thông báo
     */
    showNotification: function(message, type = 'info') {
        // Xóa notification cũ nếu có
        const oldNotif = document.querySelector('.wishlist-notification');
        if (oldNotif) {
            oldNotif.remove();
        }

        const notification = document.createElement('div');
        notification.className = `wishlist-notification wishlist-notification-${type}`;
        notification.textContent = message;

        // Style
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 25px;
            border-radius: 8px;
            color: white;
            font-weight: 500;
            z-index: 10000;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            animation: slideInRight 0.3s ease-out;
        `;

        // Màu theo type
        const colors = {
            success: '#28a745',
            error: '#dc3545',
            warning: '#ffc107',
            info: '#17a2b8'
        };
        notification.style.backgroundColor = colors[type] || colors.info;
        if (type === 'warning') {
            notification.style.color = '#000';
        }

        document.body.appendChild(notification);

        // Auto remove sau 3s
        setTimeout(() => {
            notification.style.animation = 'slideOutRight 0.3s ease-in';
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    },

    /**
     * Khởi tạo event listeners
     */
    init: function() {
        console.log('WishlistManager initialized');
        
        // Load wishlist count
        this.loadWishlistCount();

        // Event delegation cho tất cả nút wishlist
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.btn-wishlist, .wishlist-btn, .wishlist-icon');
            if (btn) {
                e.preventDefault();
                e.stopPropagation();
                
                const productId = btn.dataset.productId || btn.getAttribute('data-product-id');
                
                if (productId) {
                    this.toggleWishlist(productId, btn);
                } else {
                    console.error('Product ID not found on wishlist button', btn);
                }
            }
        });

        // Kiểm tra và cập nhật trạng thái các nút wishlist trên trang
        this.updateAllWishlistButtons();
    },

    /**
     * Cập nhật trạng thái tất cả nút wishlist trên trang
     */
    updateAllWishlistButtons: async function() {
        const buttons = document.querySelectorAll('.btn-wishlist, .wishlist-btn');
        
        for (const btn of buttons) {
            const productId = btn.dataset.productId || btn.getAttribute('data-product-id');
            if (productId) {
                const inWishlist = await this.checkInWishlist(productId);
                const icon = btn.querySelector('i');
                
                if (inWishlist) {
                    icon?.classList.remove('far');
                    icon?.classList.add('fas');
                    btn.classList.add('active');
                }
            }
        }
    }
};

// CSS cho animations
if (!document.querySelector('#wishlist-animations')) {
    const style = document.createElement('style');
    style.id = 'wishlist-animations';
    style.textContent = `
        @keyframes slideInRight {
            from {
                opacity: 0;
                transform: translateX(100%);
            }
            to {
                opacity: 1;
                transform: translateX(0);
            }
        }
        
        @keyframes slideOutRight {
            from {
                opacity: 1;
                transform: translateX(0);
            }
            to {
                opacity: 0;
                transform: translateX(100%);
            }
        }
    `;
    document.head.appendChild(style);
}

// Khởi tạo khi DOM ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => WishlistManager.init());
} else {
    WishlistManager.init();
}
