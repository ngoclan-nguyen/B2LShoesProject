const Toast = Swal.mixin({
    toast: true,
    position: 'top-end',
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true,
    didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer)
        toast.addEventListener('mouseleave', Swal.resumeTimer)
    }
});

document.addEventListener("DOMContentLoaded", function() {

    const urlParams = new URLSearchParams(window.location.search);

    if (urlParams.has('deleteSuccess')) {
        Toast.fire({
            icon: 'success',
            title: 'Đã xóa sản phẩm thành công!'
        });
        cleanUrl();
    }

    if (urlParams.has('saveSuccess')) {
        Toast.fire({
            icon: 'success',
            title: 'Đã lưu thông tin sản phẩm!'
        });
        cleanUrl();
    }

    if (urlParams.has('error')) {
        Toast.fire({
            icon: 'error',
            title: 'Có lỗi xảy ra, vui lòng thử lại!'
        });
        cleanUrl();
    }
});

function deleteProduct(id) {
    Swal.fire({
        title: 'Bạn có chắc chắn?',
        text: "Sản phẩm này sẽ bị chuyển vào thùng rác!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Vâng, xóa nó!',
        cancelButtonText: 'Hủy bỏ',
        reverseButtons: true
    }).then((result) => {
        if (result.isConfirmed) {
            // Chuyển hướng sang Controller để xóa
            window.location.href = '/admin/product/delete/' + id;
        }
    })
}

function cleanUrl() {
    const newUrl = window.location.pathname;
    window.history.replaceState(null, null, newUrl);
}