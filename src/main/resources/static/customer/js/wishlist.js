$(document).ready(function() {
    let currentProductId = null; // Lưu productId đang thao tác toàn cục

    // 1. XỬ LÝ XÓA SẢN PHẨM KHỎI WISHLIST
    $(document).on('click', '.removeWishlistBtn', function(e) {
        e.preventDefault();
        e.stopPropagation();

        const productId = $(this).data('id');
        const wishlistDiv = $(this).closest('.wishlistItem');

        Swal.fire({
            title: 'Xóa sản phẩm?',
            text: "Bạn có chắc chắn muốn xóa sản phẩm này khỏi danh sách yêu thích?",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#000000',
            confirmButtonText: 'Đồng ý xóa',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: '/api/wishlist/remove',
                    type: 'POST',
                    data: { productId: productId },
                    success: function(res) {
                        wishlistDiv.remove();
                        const countEl = $('#wishlistCount');
                        if(countEl.length) {
                            let count = parseInt(countEl.text()) - 1;
                            countEl.text(count);
                            if(count <= 0) location.reload();
                        }
                    },
                    error: function(xhr) {
                        Swal.fire('Lỗi', 'Xóa thất bại!', 'error');
                    }
                });
            }
        });
    });

    // 2. MỞ POPUP CHỌN SIZE KHI NHẤN "THÊM VÀO GIỎ"
    $(document).on('click', '.addToCartBtn', function(e) {
        e.stopPropagation();
        currentProductId = $(this).data('id');
        const productName = $(this).data('name');
        const productImage = $(this).data('image');
        const productPrice = $(this).data('price');

        // Điền dữ liệu vào popup
        $('#popupImage').attr('src', productImage);
        $('#popupName').attr('href', '/product/' + currentProductId).text(productName);
        $('#popupPrice').text(new Intl.NumberFormat('vi-VN').format(productPrice) + 'đ');
        $('#popupQty').val(1);

        // Gọi API lấy size
        $.ajax({
            url: '/api/products/getSize',
            type: 'GET',
            data: { productId: currentProductId },
            success: function (sizes) {
                const $sizeSelect = $('#popupSize');
                $sizeSelect.empty().prop('disabled', false);

                if (sizes && sizes.length > 0) {
                    sizes.forEach(size => {
                        $sizeSelect.append(`<option value="${size}">${size}</option>`);
                    });
                } else {
                    $sizeSelect.append('<option selected>Không có size</option>').prop('disabled', true);
                }
            },
            error: function () {
                console.error('Không lấy được danh sách size');
            }
        });

        // Hiển thị popup
        $('#cartPopup').removeClass('hidden').css('opacity', '1');
    });

    // 3. XỬ LÝ THÊM VÀO GIỎ HÀNG (TRONG POPUP)
    $('#popupAddToCart').on('click', function() {
        const $sizeSelect = $('#popupSize');
        if ($sizeSelect.is(':disabled')) {
            Swal.fire({ icon: 'warning', title: 'Lỗi', text: 'Sản phẩm này không có size' });
            return;
        }

        const size = $sizeSelect.val();
        const qty = parseInt($('#popupQty').val()) || 1;

        $.ajax({
            url: '/api/cart/add',
            type: 'POST',
            data: { productId: currentProductId, size: size, quantity: qty },
            success: function(res) {
                if (res.status === 'success') {
                    Swal.fire({
                        icon: 'success',
                        title: 'Đã thêm vào giỏ hàng!',
                        toast: true, position: 'top-end', showConfirmButton: false, timer: 2000
                    });
                    // Cập nhật số lượng trên icon giỏ hàng (Badge)
                    const cartBadge = $('#cart-qty');
                    if(cartBadge.length) cartBadge.text(res.totalItems);
                } else {
                    Swal.fire({
                        icon: 'warning',
                        title: data.message,
                        toast: true, position: 'top-end', showConfirmButton: false, timer: 2000
                    });
                }
                $('#cartPopup').addClass('hidden');
            },
            error: function(err) {
                Swal.fire('Lỗi', 'Không thể thêm vào giỏ hàng', 'error');
            }
        });
    });

    // 4. CÁC SỰ KIỆN ĐÓNG POPUP & TĂNG GIẢM SL
    $('#closeCartPopup, #popupCancel').on('click', () => $('#cartPopup').addClass('hidden'));

    $('#cartPopup').on('click', function(e) {
        if ($(e.target).is('#cartPopup')) $(this).addClass('hidden');
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