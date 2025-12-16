document.addEventListener('DOMContentLoaded', () => {
    const notificationLinks = document.querySelectorAll('.notification-link');

    notificationLinks.forEach(link => {
        link.addEventListener('click', (event) => {
            event.preventDefault(); // Ngăn chuyển hướng mặc định của thẻ <a>

            const id = link.getAttribute('data-id');
            const targetUrl = link.getAttribute('data-url');

            // Chuẩn hóa URL (xử lý 'null' từ DB hoặc Thymeleaf)
            const finalUrl = (targetUrl === 'null' || targetUrl === '') ? null : targetUrl;

            // 1. Gọi API đánh dấu đã đọc
            fetch('/admin/notifications/mark-read/' + id, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then(response => {
                    if (response.ok) {
                        // Cập nhật UI ngay lập tức
                        link.classList.remove('notification-unread');

                        // 2. Chuyển hướng nếu URL tồn tại
                        if (finalUrl) {
                            window.location.href = finalUrl;
                        }
                    }
                })
                .catch(error => {
                    console.error('Lỗi Fetch API:', error);
                    // Dù có lỗi API, vẫn cho phép chuyển hướng để không làm mất chức năng chính
                    if (finalUrl) {
                        window.location.href = finalUrl;
                    }
                });
        });
    });
});

// Hàm đánh dấu tất cả đã đọc
function markAllAsRead() {
    if (confirm('Bạn có chắc chắn muốn đánh dấu tất cả thông báo là đã đọc không?')) {
        fetch('/admin/notifications/mark-all-read', { method: 'POST' })
            .then(response => {
                if (response.ok) {
                    window.location.reload();
                }
            });
    }
}