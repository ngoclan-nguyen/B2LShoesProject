// --- 1. HÀM DỊCH THUẬT & LẤY LỖI (Dùng chung cho cả file) ---
function translateError(errorKey) {
    const messages = {
        'auth_required': 'Vui lòng đăng nhập để thực hiện thao tác này!',
        'access_denied': 'Bạn không có quyền truy cập chức năng này.',
        'invalid_csrf': 'Phiên làm việc hết hạn, vui lòng tải lại trang.',
        'bad_credentials': 'Sai tài khoản hoặc mật khẩu.',
        'User is disabled': 'Tài khoản đã bị vô hiệu hóa.',
        'out_of_stock': 'Sản phẩm này hiện đã hết hàng.',
        'size_not_found': 'Size này không còn tồn tại.'
    };
    return messages[errorKey] || errorKey || 'Có lỗi xảy ra.';
}

function getMessageFromXhr(xhr) {
    let key = xhr.responseJSON ? xhr.responseJSON.message : xhr.responseText;
    if (key) key = key.replace(/['"]+/g, '').trim();
    return translateError(key);
}

$(document).ready(function() {
    let currentProductId = null;

    // --- 2. CẤU HÌNH CSRF (BẮT BUỘC ĐỂ KHÔNG BỊ LỖI 403) ---
    const csrfToken = $("meta[name='_csrf']").attr("content");
    const csrfHeader = $("meta[name='_csrf_header']").attr("content");

    $.ajaxSetup({
        beforeSend: function(xhr) {
            if (csrfToken && csrfHeader) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        }
    });

    // --- 3. XỬ LÝ XÓA SẢN PHẨM KHỎI WISHLIST ---
    $(document).on('click', '.removeWishlistBtn', function(e) {
        e.preventDefault();
        e.stopPropagation();

        const productId = $(this).data('id');
        const wishlistDiv = $(this).closest('.wishlistItem');

        Swal.fire({
            title: 'Xóa sản phẩm?',
            text: "Bạn có chắc chắn muốn xóa khỏi danh sách yêu thích?",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#000000',
            confirmButtonText: 'Xóa',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: '/api/wishlist/remove',
                    type: 'POST', // POST cần có CSRF token ở trên
                    data: { productId: productId },
                    success: function(res) {
                        wishlistDiv.fadeOut(300, function(){ $(this).remove(); });

                        const countEl = $('#wishlistCount');
                        if(countEl.length) {
                            let count = parseInt(countEl.text()) - 1;
                            countEl.text(Math.max(0, count));
                            if(count <= 0) location.reload(); // Reload để hiện giao diện trống
                        }

                        Swal.fire({ icon: 'success', title: 'Đã xóa!', toast: true, position: 'top-end', showConfirmButton: false, timer: 1500 });
                    },
                    error: function(xhr) {
                        Swal.fire('Lỗi', getMessageFromXhr(xhr), 'error');
                    }
                });
            }
        });
    });

    // --- 4. MỞ POPUP CHỌN SIZE ---
    $(document).on('click', '.addToCartBtn', function(e) {
        e.stopPropagation();
        currentProductId = $(this).data('id');
        const productName = $(this).data('name');
        const productImage = $(this).data('image');
        const productPrice = $(this).data('price');

        $('#popupImage').attr('src', productImage);
        $('#popupName').attr('href', '/product/' + currentProductId).text(productName);
        $('#popupPrice').text(new Intl.NumberFormat('vi-VN').format(productPrice) + 'đ');
        $('#popupQty').val(1);

        // Gọi API lấy size
        $.ajax({
            url: '/api/products/getSize', // Đảm bảo URL Controller đúng: @GetMapping("/getSize")
            type: 'GET',
            data: { productId: currentProductId },
            success: function (sizes) {
                const $sizeSelect = $('#popupSize');
                $sizeSelect.empty().prop('disabled', false);

                if (sizes && sizes.length > 0) {
                    sizes.forEach(size => {
                        // size có thể là Object {id, name} hoặc String "38".
                        // Cần kiểm tra lại Backend trả về gì.
                        // Nếu trả về String:
                        $sizeSelect.append(`<option value="${size}">${size}</option>`);

                        // Nếu trả về Object (ví dụ: productVariantId):
                        // $sizeSelect.append(`<option value="${size.id}">${size.name}</option>`);
                    });
                } else {
                    $sizeSelect.append('<option selected>Hết hàng</option>').prop('disabled', true);
                }
            },
            error: function (xhr) {
                console.error('Lỗi lấy size:', xhr);
                Swal.fire('Lỗi', 'Không tải được thông tin size', 'error');
            }
        });

        $('#cartPopup').removeClass('hidden').addClass('flex');
    });

    // --- 5. XỬ LÝ NÚT "XÁC NHẬN" TRONG POPUP ---
    $('#popupAddToCart').on('click', function() {
        const $sizeSelect = $('#popupSize');
        if ($sizeSelect.is(':disabled') || !$sizeSelect.val()) {
            Swal.fire({ icon: 'warning', title: 'Thông báo', text: 'Sản phẩm này hiện không khả dụng hoặc bạn chưa chọn size.' });
            return;
        }

        const size = $sizeSelect.val();
        const qty = parseInt($('#popupQty').val()) || 1;

        $.ajax({
            url: '/api/cart/add',
            type: 'POST',
            // Lưu ý: Backend cần nhận đúng tham số (size tên là String hay sizeId là Long)
            data: { productId: currentProductId, size: size, quantity: qty },
            success: function(res) {
                $('#cartPopup').addClass('hidden').removeClass('flex');

                if (res.status === 'success') {
                    Swal.fire({
                        icon: 'success',
                        title: 'Thành công',
                        text: 'Đã thêm vào giỏ hàng!',
                        timer: 2000,
                        showConfirmButton: false
                    });
                    const cartBadge = $('#cart-qty');
                    if(cartBadge.length) cartBadge.text(res.totalItems);
                } else {
                    // SỬA LỖI BUG: Dùng res.message thay vì data.message
                    // Dùng hàm translateError để dịch lỗi
                    let msg = translateError(res.message);
                    Swal.fire({
                        icon: 'warning',
                        title: 'Thông báo',
                        text: msg,
                        confirmButtonColor: '#FF9900'
                    });
                }
            },
            error: function(xhr) {
                $('#cartPopup').addClass('hidden').removeClass('flex');

                // Xử lý trường hợp chưa đăng nhập (401)
                if (xhr.status === 401) {
                    Swal.fire({
                        icon: 'warning',
                        title: 'Bạn chưa đăng nhập!',
                        text: 'Vui lòng đăng nhập để mua hàng.',
                        showCancelButton: true,
                        confirmButtonText: 'Đăng nhập',
                        cancelButtonText: 'Hủy',
                        confirmButtonColor: '#FF9900'
                    }).then((result) => {
                        if (result.isConfirmed) window.location.href = '/login';
                    });
                } else {
                    Swal.fire('Lỗi', getMessageFromXhr(xhr), 'error');
                }
            }
        });
    });

    // --- 6. CÁC SỰ KIỆN UI KHÁC ---
    $('#closeCartPopup, #popupCancel').on('click', () => $('#cartPopup').addClass('hidden').removeClass('flex'));

    $('#cartPopup').on('click', function(e) {
        if ($(e.target).is('#cartPopup')) $(this).addClass('hidden').removeClass('flex');
    });

    $('.quantityPlus').on('click', function() {
        let input = $('#popupQty');
        input.val(parseInt(input.val()) + 1);
    });

    $('.quantityMinus').on('click', function() {
        let input = $('#popupQty');
        let val = parseInt(input.val());
        if (val > 1) input.val(val - 1);
    });
});