document.addEventListener("DOMContentLoaded", function() {

    // ==================================================
    // 1. XỬ LÝ GỢI Ý TÌM KIẾM (LIVE SEARCH)
    // ==================================================
    const searchInput = document.getElementById('search-input');
    const suggestionBox = document.getElementById('search-suggestions');
    let timeoutId;

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('vi-VN').format(amount) + '₫';
    };

    if (searchInput && suggestionBox) {
        searchInput.addEventListener('input', function() {
            const keyword = this.value.trim();
            clearTimeout(timeoutId);

            if (keyword.length < 2) {
                suggestionBox.classList.add('hidden');
                suggestionBox.innerHTML = '';
                return;
            }

            timeoutId = setTimeout(() => {
                fetch(`/api/products/suggest?keyword=${encodeURIComponent(keyword)}`)
                    .then(res => res.json())
                    .then(data => {
                        suggestionBox.innerHTML = '';
                        if (data.length > 0) {
                            data.forEach(product => {
                                const item = document.createElement('a');
                                item.href = `/product/${product.id}`;
                                item.className = "flex items-center gap-4 p-3 hover:bg-gray-50 border-b border-gray-100 transition last:border-none cursor-pointer";
                                item.innerHTML = `
                                    <img src="${product.image}" class="w-12 h-12 object-contain mix-blend-multiply bg-gray-50 rounded border border-gray-200">
                                    <div>
                                        <h4 class="font-bold text-sm text-gray-800 line-clamp-1">${product.name}</h4>
                                        <p class="text-xs text-red-600 font-bold">${formatCurrency(product.price)}</p>
                                    </div>
                                `;
                                suggestionBox.appendChild(item);
                            });

                            const viewAll = document.createElement('a');
                            viewAll.href = `/search?keyword=${keyword}`;
                            viewAll.className = "block p-3 text-center text-xs font-bold text-orange-500 hover:bg-orange-50 uppercase tracking-wide border-t border-gray-100";
                            viewAll.innerText = `Xem tất cả kết quả cho "${keyword}"`;
                            suggestionBox.appendChild(viewAll);

                            suggestionBox.classList.remove('hidden');
                        } else {
                            suggestionBox.innerHTML = `<div class="p-4 text-center text-sm text-gray-500">Không tìm thấy sản phẩm nào.</div>`;
                            suggestionBox.classList.remove('hidden');
                        }
                    })
                    .catch(err => console.error("Lỗi tìm kiếm:", err));
            }, 300);
        });

        document.addEventListener('click', function(e) {
            if (!searchInput.contains(e.target) && !suggestionBox.contains(e.target)) {
                suggestionBox.classList.add('hidden');
            }
        });

        searchInput.addEventListener('focus', function(){
            if(suggestionBox.innerHTML.trim() !== "") {
                suggestionBox.classList.remove('hidden');
            }
        });
    }

    // ==================================================
    // 2. XỬ LÝ MENU MOBILE
    // ==================================================
    const menuContainer = document.getElementById("mobile-menu-container");
    const drawer = document.getElementById("mobile-menu-drawer");
    const overlay = document.getElementById("mobile-menu-overlay");

    const openBtn = document.getElementById("mobile-menu-btn");
    const closeBtn = document.getElementById("close-menu-btn");

    if (openBtn && closeBtn && menuContainer) {

        openBtn.addEventListener("click", () => {
            menuContainer.classList.remove("hidden");
            // Timeout nhỏ để CSS transition hoạt động
            setTimeout(() => {
                overlay.classList.remove("opacity-0");
                drawer.classList.remove("-translate-x-full");
            }, 10);
            document.body.style.overflow = 'hidden'; // Khóa cuộn trang
        });

        // Hàm đóng menu nội bộ
        const closeMenu = () => {
            overlay.classList.add("opacity-0");
            drawer.classList.add("-translate-x-full");
            setTimeout(() => {
                menuContainer.classList.add("hidden");
                document.body.style.overflow = 'auto'; // Mở khóa cuộn
            }, 300);
        };

        closeBtn.addEventListener("click", closeMenu);
        overlay.addEventListener("click", closeMenu);
    }

    // ==================================================
    // 3. GỌI CẬP NHẬT GIỎ HÀNG KHI LOAD TRANG
    // ==================================================
    updateCartCount();

}); // <--- ĐÓNG SỰ KIỆN DOMContentLoaded TẠI ĐÂY LÀ CHUẨN NHẤT


// ==================================================
// 4. CÁC HÀM GLOBAL (Định nghĩa bên ngoài để HTML gọi được nếu cần)
// ==================================================

// Hàm mở submenu (Hỗ trợ) - Dùng cho onclick trong HTML
function toggleMobileSubmenu() {
    const submenu = document.getElementById('mobile-submenu');
    const arrow = document.getElementById('mobile-arrow'); // Nếu có icon mũi tên
    if (submenu) {
        submenu.classList.toggle('hidden');
        if(arrow) arrow.classList.toggle('rotate-180');
    }
}

// Hàm gọi API cập nhật số lượng giỏ hàng
function updateCartCount() {
    const cartBadge = document.getElementById('cart-qty');
    if (cartBadge) {
        fetch('/api/cart/count')
            .then(res => res.json())
            .then(count => {
                cartBadge.innerText = count;
                // Tùy chọn: Nếu > 0 mới hiện, còn không thì ẩn
                // if (count > 0) cartBadge.classList.remove('hidden');
            })
            .catch(err => console.log("Chưa đăng nhập hoặc lỗi giỏ hàng"));
    }
}