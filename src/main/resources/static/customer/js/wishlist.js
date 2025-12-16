$(document).on('click', '.removeWishlistBtn', function(e) {
    e.preventDefault();
    e.stopPropagation();

    const productId = $(this).data('id');
    const wishlistDiv = $(this).closest('.wishlistItem');

    // ✅ BƯỚC XÁC NHẬN BẰNG SWEETALERT2
    Swal.fire({
        title: 'Xóa sản phẩm?',
        text: "Bạn có chắc chắn muốn xóa sản phẩm này khỏi danh sách yêu thích?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33', // Màu đỏ cho nút xóa
        cancelButtonColor: '#000000', // Màu đen cho nút hủy
        confirmButtonText: 'Đồng ý xóa',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            // Nếu người dùng xác nhận
            $.ajax({
                url: '/api/wishlist/remove',
                type: 'POST',
                data: { productId: productId },
                success: function(res) {
                    wishlistDiv.remove(); // Xóa khỏi DOM

                    // Cập nhật số lượng
                    const countEl = $('#wishlistCount');
                    if(countEl.length) {
                        let count = parseInt(countEl.text()) - 1;
                        countEl.text(count);

                        if(count <= 0) {
                            // Nếu hết sản phẩm, tải lại trang để hiển thị trạng thái trống
                            location.reload();
                        } else {
                            Swal.fire({
                                icon: 'success',
                                title: 'Đã xóa!',
                                text: 'Sản phẩm đã được xóa khỏi danh sách yêu thích.',
                                toast: true,
                                position: 'top-end',
                                showConfirmButton: false,
                                timer: 2000
                            });
                        }
                    }
                },
                error: function(xhr) {
                    Swal.fire('Lỗi', 'Xóa thất bại! Vui lòng thử lại.', 'error');
                    console.error("Xóa thất bại:", xhr.responseText);
                }
            });
        }
    });

    let currentProductId = null; // lưu productId đang thao tác

    // Click nút Thêm vào giỏ hàng
    $(document).on('click', '.addToCartBtn', function(e) {
        e.stopPropagation();
        currentProductId = $(this).data('id');
        const productName = $(this).data('name');
        const productImage = $(this).data('image');
        const productPrice = $(this).data('price');

        // Điền dữ liệu vào popup
        $('#popupImage').attr('src', productImage);
        $('#popupName').attr('href', '/product/' + currentProductId);
        $('#popupName').text(productName);
        $('#popupPrice').text(new Intl.NumberFormat('vi-VN').format(productPrice) + 'đ');

        $('#popupQty').val(1);
        // === Gọi API lấy size ===
        $.ajax({
            url: '/api/products/getSize',
            type: 'GET',
            data: { productId: currentProductId },
            success: function (sizes) {
                const $sizeSelect = $('#popupSize');
                $sizeSelect.empty(); // clear size cũ

                if (sizes && sizes.length > 0) {
                    sizes.forEach(function (size) {
                        $sizeSelect.append(
                            `<option value="${size}">${size}</option>`
                        );
                    });
                } else {
                    $sizeSelect
                        .append('<option selected>Không có size</option>')
                        .prop('disabled', true);
                }
            },
            error: function () {
                alert('Không lấy được danh sách size');
            }
        });

        // Hiển thị popup
        $('#cartPopup').removeClass('hidden');

        // Hiển thị popup
        $('#cartPopup').removeClass('hidden');
    });

    // Đóng popup khi click × hoặc click Hủy
    $('#closeCartPopup, #popupCancel').on('click', function() {
        $('#cartPopup').addClass('hidden');
    });

    // Đóng popup khi click ra ngoài modal content
    $('#cartPopup').on('click', function(e) {
        if ($(e.target).is('#cartPopup')) {
            $(this).addClass('hidden');
        }
    });

    // Thêm vào giỏ hàng
    $('#popupAddToCart').on('click', function() {
        const $sizeSelect = $('#popupSize');

        if ($sizeSelect.is(':disabled')) {
            Swal.fire({
                icon: 'warning',
                title: 'Không thể thêm vào giỏ',
                text: 'Sản phẩm này không có size'
            });
            return;
        }
        const size = $sizeSelect.val();
        const qty = parseInt($('#popupQty').val()) || 1;

        $.ajax({
            url: '/api/cart/add',
            type: 'POST',
            headers: { },
            data: {
                productId: currentProductId,
                size: size,
                quantity: qty
            },
            success: function(res) {
                if (res.status === 'success') {
                    Swal.fire({
                        icon: 'success',
                        title: res.message,
                        showConfirmButton: false,
                        timer: 1500
                    });
                    const cartBadge = document.getElementById('cart-qty');
                    if(cartBadge) {
                        cartBadge.innerText = res.totalItems;
                        cartBadge.parentElement.classList.add('scale-125');
                        setTimeout(() => cartBadge.parentElement.classList.remove('scale-125'), 200);
                    }
                } else {
                    Swal.fire({
                        icon: 'error',
                        title: 'Lỗi',
                        text: res.message
                    });
                }
                $('#cartPopup').addClass('hidden');
            },
            error: function(err) {
                alert('Thêm thất bại: ' + err.responseText);
            }
        });
    });

    // Tăng giảm số lượng
    $('#cartPopup .quantityPlus').click(function() {
        const input = $(this).siblings('.quantityDisplay');
        let current = parseInt(input.val()) || 1;
        input.val(current + 1);
    });

    $('#cartPopup .quantityMinus').click(function() {
        const input = $(this).siblings('.quantityDisplay');
        let current = parseInt(input.val()) || 1;
        if (current > 1) input.val(current - 1);
    });
});