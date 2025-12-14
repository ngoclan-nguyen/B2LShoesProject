function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebar-overlay');

    if (sidebar.classList.contains('-translate-x-full')) {
        sidebar.classList.remove('-translate-x-full');
        overlay.classList.remove('hidden');
        setTimeout(() => overlay.classList.remove('opacity-0'), 10);
    } else {
        sidebar.classList.add('-translate-x-full');
        overlay.classList.add('opacity-0');
        setTimeout(() => overlay.classList.add('hidden'), 300);
    }
}

// Hàm xác nhận xóa chung
function confirmDelete(btn, entityName = 'mục') {
    const id = btn.getAttribute('data-id');
    if(confirm(`Bạn có chắc chắn muốn xóa ${entityName} có ID ${id} không?`)) {
        alert(`Đã gửi yêu cầu xóa ID: ${id}`);
        // window.location.href = `/admin/${entityName}/delete/${id}`;
    }
}