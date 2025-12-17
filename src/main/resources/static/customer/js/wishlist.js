$(document).ready(function () {

    const csrfToken  = document.querySelector("meta[name='_csrf']")?.content;
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.content;

    $.ajaxSetup({
        beforeSend: function (xhr) {
            if (csrfToken && csrfHeader) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        }
    });

    let currentProductId = null;

    $(document).on('click', '.removeWishlistBtn', function (e) {
        e.preventDefault();
        e.stopPropagation();

        const productId = $(this).data('id');
        const wishlistDiv = $(this).closest('.wishlistItem');

        Swal.fire({
            title: 'Xóa sản phẩm?',
            text: 'Bạn có chắc chắn muốn xóa sản phẩm này khỏi danh sách yêu thích?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#000',
            confirmButtonText: 'Đồng ý xóa',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: '/api/wishlist/remove',
                    type: 'POST',
                    data: { productId },
                    success: function () {
                        wishlistDiv.remove();

                        const countEl = $('#wishlistCount');
                        if (countEl.length) {
                            let count = parseInt(countEl.text()) - 1;
                            countEl.text(Math.max(count, 0));

                            if (count <= 0) {
                                location.reload();
                            }
                        }

                        Swal.fire({
                            icon: 'success',
                            title: 'Đã xóa!',
                            toast: true,
                            position: 'top-end',
                            showConfirmButton: false,
                            timer: 1500
                        });
                    },
                    error: function (xhr) {
                        Swal.fire('Lỗi', 'Xóa thất bại!', 'error');
                        console.error(xhr.responseText);
                    }
                });
            }
        });
    });

    $(document).on('click', '.addToCartBtn', function (e) {
        e.stopPropagation();

        currentProductId = $(this).data('id');
        const productName  = $(this).data('name');
        const productImage = $(this).data('image');
        const productPrice = $(this).data('price');

        $('#popupImage').attr('src', productImage);
        $('#popupName')
            .attr('href', '/product/' + currentProductId)
            .text(productName);

        $('#popupPrice').text(
            new Intl.NumberFormat('vi-VN').format(productPrice) + 'đ'
        );

        $('#popupQty').val(1);

        // Lấy size
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
                    $sizeSelect
                        .append('<option>Không có size</option>')
                        .prop('disabled', true);
                }
            },
            error: function () {
                Swal.fire('Lỗi', 'Không lấy được danh sách size', 'error');
            }
        });

        $('#cartPopup').removeClass('hidden');
    });

    $('#closeCartPopup, #popupCancel').on('click', function () {
        $('#cartPopup').addClass('hidden');
    });

    $('#cartPopup').on('click', function (e) {
        if ($(e.target).is('#cartPopup')) {
            $(this).addClass('hidden');
        }
    });

    $('#popupAddToCart').on('click', function () {

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
        const quantity = parseInt($('#popupQty').val()) || 1;

        $.ajax({
            url: '/api/cart/add',
            type: 'POST',
            data: {
                productId: currentProductId,
                size,
                quantity
            },
            success: function (res) {
                if (res.status === 'success') {
                    Swal.fire({
                        icon: 'success',
                        title: res.message,
                        showConfirmButton: false,
                        timer: 1500
                    });

                    const cartBadge = $('#cart-qty');
                    if (cartBadge.length) {
                        cartBadge.text(res.totalItems);
                        cartBadge.parent().addClass('scale-125');
                        setTimeout(() => {
                            cartBadge.parent().removeClass('scale-125');
                        }, 200);
                    }
                } else {
                    Swal.fire('Lỗi', res.message, 'error');
                }

                $('#cartPopup').addClass('hidden');
            },
            error: function (xhr) {
                Swal.fire('Lỗi', xhr.responseText, 'error');
            }
        });
    });

    $('#cartPopup .quantityPlus').on('click', function () {
        const input = $(this).siblings('.quantityDisplay');
        input.val((parseInt(input.val()) || 1) + 1);
    });

    $('#cartPopup .quantityMinus').on('click', function () {
        const input = $(this).siblings('.quantityDisplay');
        let current = parseInt(input.val()) || 1;
        if (current > 1) input.val(current - 1);
    });

});
