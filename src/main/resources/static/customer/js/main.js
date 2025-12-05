document.addEventListener("DOMContentLoaded", function() {

    // Gợi ý tìm kiếm
    const searchInput = document.getElementById('search-input');
    const suggestionBox = document.getElementById('search-suggestions');
    let timeoutId;

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('vi-VN').format(amount) + '₫';
    };

    if (searchInput && suggestionBox) { // Kiểm tra tồn tại để tránh lỗi ở trang không có search
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

// MOBILE MENU
    const mobileMenuBtn = document.getElementById('mobile-menu-btn');
    const closeMenuBtn = document.getElementById('close-menu-btn');
    const mobileMenu = document.getElementById('mobile-menu');


    if (mobileMenuBtn && mobileMenu && closeMenuBtn) {
        mobileMenuBtn.addEventListener('click', () => {
            mobileMenu.classList.remove('hidden');
            mobileMenu.classList.add('flex');
            document.body.style.overflow = 'hidden';
        });
        closeMenuBtn.addEventListener('click', () => {
            mobileMenu.classList.add('hidden');
            mobileMenu.classList.remove('flex');
            document.body.style.overflow = 'auto';
        });

        mobileMenu.addEventListener('click', (e) => {
            if (e.target === mobileMenu) {
                mobileMenu.classList.add('hidden');
                mobileMenu.classList.remove('flex');
                document.body.style.overflow = 'auto';
            }
        });
    } else {
        console.error("Không tìm thấy nút Mobile Menu trong HTML!");
    }
    updateCartCount();
});

// Hàm gọi API cập nhật số lượng
function updateCartCount() {
    const cartBadge = document.getElementById('cart-qty');
    if (cartBadge) {
        fetch('/api/cart/count')
            .then(res => res.json())
            .then(count => {
                cartBadge.innerText = count;
                if (count > 0) {
                    cartBadge.classList.remove('hidden');
                }
            })
            .catch(err => console.log("Chưa đăng nhập hoặc lỗi giỏ hàng"));
    }
}