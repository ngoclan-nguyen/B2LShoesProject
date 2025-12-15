// Ẩn/Hiện khu vực biến thể
function toggleVariants() {
    const isChecked = document.getElementById('hasVariants').checked;
    const section = document.getElementById('variantSection');
    const simpleStock = document.getElementById('simpleStock');

    if (isChecked) {
        section.classList.remove('hidden');
        simpleStock.classList.add('hidden');
    } else {
        section.classList.add('hidden');
        simpleStock.classList.remove('hidden');
    }
}

//  Thêm dòng biến thể mới
function addVariantRow(colors) {
    // colors: Mảng chuỗi tên màu được truyền từ HTML vào (nếu cần)
    const tbody = document.getElementById('variantTableBody');
    const firstRow = tbody.querySelector('.variant-row');

    const newRow = firstRow.cloneNode(true);

    newRow.querySelectorAll('input').forEach(input => input.value = '');

    tbody.appendChild(newRow);
}

// Xóa dòng biến thể
function removeRow(btn) {
    const row = btn.closest('tr');
    const tbody = document.getElementById('variantTableBody');
    if (tbody.querySelectorAll('tr').length > 1) {
        row.remove();
    } else {
        alert("Phải có ít nhất một biến thể!");
    }
}

// Preview ảnh
function previewImages(input) {
    const container = document.getElementById('imagePreview');
    container.innerHTML = '';

    if (input.files) {
        Array.from(input.files).forEach(file => {
            const reader = new FileReader();
            reader.onload = function(e) {
                const div = document.createElement('div');
                div.className = 'w-full h-20 bg-gray-100 rounded overflow-hidden border border-gray-200 relative group';
                div.innerHTML = `<img src="${e.target.result}" class="w-full h-full object-cover">`;
                container.appendChild(div);
            }
            reader.readAsDataURL(file);
        });
    }
}