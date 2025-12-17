$(function() {

    const csrfToken  = document.querySelector("meta[name='_csrf']")?.content;
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.content;

    // Tự động gắn CSRF token cho TẤT CẢ request AJAX
    $.ajaxSetup({
        beforeSend: function (xhr) {
            if (csrfToken && csrfHeader) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        }
    });


    // Hàm định dạng tiền tệ (VNĐ)
    function formatCurrency(amount) {
        if (amount === undefined || amount === null) return '0 ₫';
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }

    // (Click sản phẩm, Checkbox)
    // Chuyển hướng khi click vào sản phẩm (Trừ các nút chức năng)
    $(document).on('click', '.cartItem', function(e) {
        // Nếu click vào checkbox, nút tăng giảm, nút xóa thì không chuyển trang
        if ($(e.target).closest('.itemCheckbox, .adjustQuantity, .removeCartBtn').length) return;

        const productId = $(this).attr('id').replace('cartItem-', '');
        window.location.href = `/product/${productId}`;
    });

    // Hàm cập nhật tổng tiền khi chọn/bỏ chọn sản phẩm
    function updateCartSelection() {
        const selectedIds = $('.itemCheckbox:checked').map(function() {
            return Number($(this).data('id'));
        }).get();

        $.ajax({
            url: '/api/cart/updateSelection',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(selectedIds),
            success: function(data) {
                // Cập nhật hiển thị tổng tiền
                $('#totalAmountDisplay').text(formatCurrency(data.totalAmount));

                // Khi thay đổi lựa chọn sản phẩm, Voucher cũ không còn tính đúng nữa
                // -> Reset giảm giá về 0 và Tổng tiền cuối = Tổng tiền hàng
                $('#finalAmountDisplay').text(formatCurrency(data.totalAmount));
                $('#discountDisplay').text('- 0 ₫');
            },
            error: function(xhr, status, error) {
                console.error("Cập nhật thất bại:", error);
            }
        });
    }

    $(document).on('change', '.itemCheckbox', updateCartSelection);

    $(document).on('change', '#selectAll', function() {
        $('.itemCheckbox').prop('checked', this.checked);
        updateCartSelection();
    });

    // CHỨC NĂNG XÓA SẢN PHẨM

    $(document).on('click', '.removeCartBtn', function() {
        const productVariantId = $(this).data('id');
        const cartItemDiv = $(this).closest('.cartItem');

        Swal.fire({
            title: 'Xóa sản phẩm?',
            text: "Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#000000',
            confirmButtonText: 'Đồng ý xóa',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: '/api/cart/remove',
                    type: 'POST',
                    data: { productVariantId: productVariantId },
                    success: function(data) {
                        cartItemDiv.remove();
                        updateCartSelection(); // Tính lại tiền

                        // Cập nhật badge số lượng trên Header
                        const cartBadge = document.getElementById('cart-qty');
                        if(cartBadge) {
                            cartBadge.innerText = data.totalItems;
                            if (data.totalItems === 0) {
                                location.reload(); // Reload nếu giỏ trống
                            } else {
                                Swal.fire({
                                    icon: 'success',
                                    title: 'Đã xóa!',
                                    toast: true,
                                    position: 'top-end',
                                    showConfirmButton: false,
                                    timer: 2000
                                });
                            }
                        }
                    },
                    error: function() {
                        Swal.fire('Lỗi', 'Xóa thất bại! Vui lòng thử lại.', 'error');
                    }
                });
            }
        });
    });

    // CHỨC NĂNG TĂNG/GIẢM SỐ LƯỢNG

    // Giảm số lượng
    $(document).on('click', '.quantityMinus', function() {
        const btn = $(this);
        const productVariantId = btn.data('id');
        const qtyEl = btn.siblings('.quantityDisplay');
        let quantity = parseInt(qtyEl.text());

        if (quantity > 1) {
            quantity--;
            updateQuantityOnServer(productVariantId, quantity, qtyEl);
        }
    });

    // Tăng số lượng
    $(document).on('click', '.quantityPlus', function() {
        const btn = $(this);
        const productVariantId = btn.data('id');
        let stock = parseInt(btn.data('quantity'));
        const qtyEl = btn.siblings('.quantityDisplay');
        let quantity = parseInt(qtyEl.text());

        if (quantity < stock) {
            quantity++;
            updateQuantityOnServer(productVariantId, quantity, qtyEl);
        } else {
            Swal.fire({
                icon: 'warning',
                title: 'Đã đạt giới hạn tồn kho!',
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 2000
            });
        }
    });

    // Hàm gửi số lượng mới lên server
    function updateQuantityOnServer(productVariantId, quantity, qtyElement) {
        $.ajax({
            url: '/api/cart/updateQuantity',
            type: 'POST',
            data: { productVariantId: productVariantId, quantity: quantity },
            success: function(data) {
                // Chỉ cập nhật số hiển thị khi server báo thành công
                qtyElement.text(quantity);
                updateCartSelection(); // Tính lại tổng tiền

                // Cập nhật badge header
                const cartBadge = document.getElementById('cart-qty');
                if(cartBadge) {
                    cartBadge.innerText = data.totalItems;
                }
            },
            error: function(xhr) {
                console.error("Cập nhật số lượng thất bại");
            }
        });
    }

    // CHỨC NĂNG ÁP DỤNG VOUCHER

    function applyVoucherLogic() {
        const voucherCode = $('#voucher').val().trim();

        // Lấy danh sách ID sản phẩm đang được check
        const selectedIds = [];
        $('.itemCheckbox:checked').each(function () {
            selectedIds.push($(this).data('id'));
        });

        // Validate đầu vào
        if (!voucherCode) {
            Swal.fire('Lỗi', 'Vui lòng nhập hoặc chọn mã khuyến mãi.', 'warning');
            return;
        }

        if (selectedIds.length === 0) {
            Swal.fire('Lỗi', 'Bạn chưa chọn sản phẩm nào để thanh toán.', 'warning');
            return;
        }

        // Hiệu ứng loading nút bấm
        const btn = $('#btnApplyVoucher');
        const originalText = btn.text();
        btn.prop('disabled', true).text('Đang xử lý...');

        //  Gửi AJAX
        $.ajax({
            url: '/api/cart/applyVoucher',
            type: 'POST',
            traditional: true,
            data: {
                voucherCode: voucherCode,
                productVariantIds: selectedIds
            },
            success: function (response) {
                btn.prop('disabled', false).text(originalText);

                if (response.status === 'success') {

                    let resultData = response.data;

                    if (!resultData && response.originalAmount) {
                        resultData = response;
                    }

                    if (resultData) {
                        // Cập nhật giao diện
                        $('#totalAmountDisplay').text(formatCurrency(resultData.originalAmount));
                        $('#discountDisplay').text('- ' + formatCurrency(resultData.discountAmount));
                        $('#finalAmountDisplay').text(formatCurrency(resultData.finalAmount));

                        Swal.fire({
                            icon: 'success',
                            title: 'Thành công!',
                            text: 'Đã áp dụng mã giảm giá: ' + formatCurrency(resultData.discountAmount),
                            timer: 2000,
                            showConfirmButton: false
                        });
                    } else {
                        Swal.fire('Lỗi', 'Dữ liệu phản hồi từ server không hợp lệ.', 'error');
                    }
                } else {
                    Swal.fire('Thất bại', response.message, 'error');
                    $('#discountDisplay').text('- 0 ₫');
                }
            },
            error: function (xhr) {
                btn.prop('disabled', false).text(originalText);

                // Reset lại hiển thị giảm giá
                $('#discountDisplay').text('- 0 ₫');

                let msg = 'Có lỗi xảy ra khi kết nối server.';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    msg = xhr.responseJSON.message;
                }
                Swal.fire('Không thể áp dụng', msg, 'error');
            }
        });
    }

    // Lắng nghe sự kiện click nút ÁP DỤNG (Dùng ID mới)
    $(document).on('click', '#btnApplyVoucher', function(e) {
        e.preventDefault(); // Ngăn form submit reload trang
        applyVoucherLogic();
    });

    // Lắng nghe sự kiện Enter trong ô input voucher
    $(document).on('keypress', '#voucher', function(e) {
        if (e.which === 13) { // Phím Enter
            e.preventDefault();
            applyVoucherLogic();
        }
    });

});