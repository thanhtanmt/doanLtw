// Common JavaScript functions for Shipper modals and pagination

// Modal functions
function showSuccessModal(title, message) {
	document.getElementById('successTitle').textContent = title;
	document.getElementById('successMessage').textContent = message;
	const modal = new bootstrap.Modal(document.getElementById('successModal'));
	modal.show();
}

function showErrorModal(title, message) {
	document.getElementById('errorTitle').textContent = title;
	document.getElementById('errorMessage').textContent = message;
	const modal = new bootstrap.Modal(document.getElementById('errorModal'));
	modal.show();
}

function showConfirmModal(title, message, onConfirm) {
	document.getElementById('confirmTitle').textContent = title;
	document.getElementById('confirmMessage').textContent = message;

	// Remove existing event listeners
	const confirmBtn = document.getElementById('confirmBtn');
	const newConfirmBtn = confirmBtn.cloneNode(true);
	confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);

	// Add new event listener
	newConfirmBtn.addEventListener('click', () => {
		try { if (typeof onConfirm === 'function') onConfirm(); } finally {
			const modalEl = document.getElementById('confirmModal');
			const instance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
			instance.hide();
		}
	});

	const modal = new bootstrap.Modal(document.getElementById('confirmModal'));
	modal.show();
}

// Order detail modal
async function viewOrderDetails(orderCode) {
	try {
		const response = await fetch(`/api/shipper/orders/${orderCode}`);
		const data = await response.json();

		if (data.success) {
			const order = data.data;
			currentOrderCode = orderCode;

			const content = `
                <div class="row">
                    <div class="col-md-6">
                        <h6><i class="fas fa-receipt me-2"></i>Thông tin đơn hàng</h6>
                        <table class="table table-sm">
                            <tr><td><strong>Mã đơn hàng:</strong></td><td>#${order.orderCode}</td></tr>
                            <tr><td><strong>Trạng thái:</strong></td><td><span class="badge bg-warning">${getStatusText(order.status)}</span></td></tr>
                            <tr><td><strong>Phương thức thanh toán:</strong></td><td>${getPaymentMethodText(order.paymentMethod)}</td></tr>
                            <tr><td><strong>Tổng tiền:</strong></td><td>${formatCurrency(order.totalAmount)}</td></tr>
                            ${order.paymentMethod === 'COD' ? `<tr><td><strong>Thu hộ:</strong></td><td>${formatCurrency(order.codAmount)}</td></tr>` : ''}
                            <tr><td><strong>Ngày tạo:</strong></td><td>${formatDateTime(order.createdAt)}</td></tr>
                        </table>
                    </div>
                    <div class="col-md-6">
                        <h6><i class="fas fa-user me-2"></i>Thông tin khách hàng</h6>
                        <table class="table table-sm">
                            <tr><td><strong>Họ tên:</strong></td><td>${order.shippingName}</td></tr>
                            <tr><td><strong>SĐT:</strong></td><td>${order.shippingPhone}</td></tr>
                            <tr><td><strong>Địa chỉ:</strong></td><td>${order.shippingAddress}</td></tr>
                        </table>
                    </div>
                </div>
                ${order.orderDetails && order.orderDetails.length > 0 ? `
				<div class="mt-3">
				  <h6><i class="fas fa-shopping-cart me-2"></i>Chi tiết hóa đơn</h6>
				  <div class="table-responsive">
				    <table class="table table-sm" style="width: 100%;">
				      <thead>
				        <tr>
				          <th>Sản phẩm</th>
				          <th>Số lượng</th>
				          <th>Đơn giá</th>
				          <th>Thành tiền</th>
				        </tr>
				      </thead>
				      <tbody>
				        ${order.orderDetails.map(detail => `
				          <tr>
				            <td>${detail.productName}</td>
				            <td>${detail.quantity}</td>
				            <td>${formatCurrency(detail.unitPrice)}</td>
				            <td>${formatCurrency(detail.totalPrice)}</td>
				          </tr>
				        `).join('')}

				        <!-- ✅ Các dòng tổng hợp đã căn trái -->
				        <tr class="border-top" style="border-top: 2px solid #e5e5e5;">
				          <td style="text-align:left; padding-left:0.5rem;"><strong>Tạm tính:</strong></td>
				          <td colspan="2"></td>
				          <td class="text-end">${formatCurrency(order.orderDetails.reduce((s, d) => s + (d.totalPrice || 0), 0))}</td>
				        </tr>
				        <tr>
				          <td style="text-align:left; padding-left:0.5rem;">Giảm giá:</td>
				          <td colspan="2"></td>
				          <td class="text-end">${formatCurrency(0)}</td>
				        </tr>
				        <tr>
				          <td style="text-align:left; padding-left:0.5rem;">Phí vận chuyển:</td>
				          <td colspan="2"></td>
				          <td class="text-end">${formatCurrency(0)}</td>
				        </tr>
				        <tr class="table-active fw-bold" style="background-color:#f5f5f5;">
				          <td style="text-align:left; padding-left:0.5rem;"><strong>Tổng tiền:</strong></td>
				          <td colspan="2"></td>
				          <td class="text-end fw-bold">${formatCurrency(order.totalAmount)}</td>
				        </tr>
				      </tbody>
				    </table>
				  </div>
				</div>


                ` : ''}
            `;

			document.getElementById('orderDetailContent').innerHTML = content;
			const modal = new bootstrap.Modal(document.getElementById('orderDetailModal'));
			modal.show();
			// Show/Hide assign button depending on page and status
			const assignBtn = document.getElementById('assignOrderBtn');
			if (assignBtn) {
				const onDeliveredPage = window.location.pathname.startsWith('/shipper/shipping'); // "Hàng đã giao"
				const canAssign = (order.status === 'PENDING' || order.status === 'CONFIRMED') && !onDeliveredPage;
				assignBtn.style.display = canAssign ? 'inline-block' : 'none';
			}
		} else {
			showErrorModal('Lỗi tải chi tiết', data.message);
		}
	} catch (error) {
		console.error('Lỗi khi xem chi tiết:', error);
		showErrorModal('Lỗi hệ thống', 'Không thể tải chi tiết đơn hàng');
	}
}

// Utility functions
function formatCurrency(amount) {
	if (!amount) return '0đ';
	return new Intl.NumberFormat('vi-VN', {
		style: 'currency',
		currency: 'VND'
	}).format(amount).replace('₫', 'đ');
}

function formatDateTime(dateTimeString) {
	const date = new Date(dateTimeString);
	return date.toLocaleString('vi-VN');
}

function getStatusText(status) {
	const statusMap = {
		'PENDING': 'Chờ xử lý',
		'CONFIRMED': 'Đã xác nhận',
		'SHIPPING': 'Đang giao hàng',
		'DELIVERED': 'Đã giao hàng',
		'FAILED': 'Giao hàng thất bại',
		'CANCELLED': 'Đã hủy'
	};
	return statusMap[status] || status;
}

function getPaymentMethodText(method) {
	const methodMap = {
		'COD': 'Thu hộ (COD)',
		'BANK_TRANSFER': 'Chuyển khoản',
		'CREDIT_CARD': 'Thẻ tín dụng'
	};
	return methodMap[method] || method;
}

// Pagination functions
function createPagination(totalPages, currentPage, containerId) {
	const container = document.getElementById(containerId);

	if (totalPages <= 1) {
		container.innerHTML = '';
		return;
	}

	let paginationHTML = '<nav><ul class="pagination justify-content-center">';

	// Previous button
	paginationHTML += `<li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="changePage(${currentPage - 1})">Trước</a>
    </li>`;

	// Page numbers
	for (let i = 1; i <= totalPages; i++) {
		if (i === currentPage) {
			paginationHTML += `<li class="page-item active">
                <span class="page-link">${i}</span>
            </li>`;
		} else {
			paginationHTML += `<li class="page-item">
                <a class="page-link" href="#" onclick="changePage(${i})">${i}</a>
            </li>`;
		}
	}

	// Next button
	paginationHTML += `<li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="changePage(${currentPage + 1})">Sau</a>
    </li>`;

	paginationHTML += '</ul></nav>';
	container.innerHTML = paginationHTML;
}

function updatePaginationInfo(startIndex, endIndex, totalItems, containerId) {
	const container = document.getElementById(containerId);
	if (container) {
		container.textContent = `Hiển thị ${startIndex + 1}-${Math.min(endIndex, totalItems)} của ${totalItems} đơn hàng`;
	}
}

