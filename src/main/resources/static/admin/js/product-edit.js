function toggleVariants() {
    const isChecked = document.getElementById('hasVariants').checked;
    const section = document.getElementById('variantSection');
    const simpleStock = document.getElementById('simpleStock');

    if (isChecked) {
        section.classList.remove('hidden');
        simpleStock.classList.add('hidden');
        document.querySelector('#simpleStock input[name="quantity"]').value = 0;
    } else {
        section.classList.add('hidden');
        simpleStock.classList.remove('hidden');
    }
}

// Hàm Thêm dòng biến thể (ĐÃ LÀM SẠCH COLOR)
function addVariantRow() {
    const tbody = document.getElementById('variantTableBody');
    // Lấy hàng đầu tiên làm mẫu (cần đảm bảo có ít nhất 1 hàng)
    let firstRow = tbody.querySelector('.variant-row');

    // Nếu tbody chưa có hàng nào (ví dụ, sản phẩm mới), ta tạo một mẫu rỗng
    if (!firstRow) {
        firstRow = document.createElement('tr');
        firstRow.className = 'variant-row';
        firstRow.innerHTML = `
            <td class="pr-2 pb-2">
                <input type="text" name="variantSize[]" placeholder="VD: 40" class="w-full border border-gray-300 rounded px-2 py-1.5 focus:outline-none">
            </td>
            <td class="pr-2 pb-2">
                <input type="number" name="variantStock[]" placeholder="0" class="w-full border border-gray-300 rounded px-2 py-1.5 focus:outline-none">
            </td>
            <td class="pb-2 text-right">
                <button type="button" onclick="removeRow(this)" class="text-red-500 hover:text-red-700"><i class="fa-solid fa-trash"></i></button>
            </td>
        `;
    }

    const newRow = firstRow.cloneNode(true);
    newRow.querySelectorAll('input').forEach(input => input.value = '');

    // Cập nhật lại index cho các trường name
    const rowsCount = tbody.querySelectorAll('.variant-row').length;

    // Chỉ cập nhật name cho variantId, variantSize, variantStock
    newRow.querySelectorAll('[name^="variantId"], [name^="variantSize"], [name^="variantStock"]').forEach(input => {
        const originalName = input.getAttribute('name').replace(/\[\d+\]/g, '[]'); // Xóa index cũ
        input.setAttribute('name', originalName.replace('[]', `[${rowsCount}]`));
    });

    // Đảm bảo ẩn trường variantId nếu đây là hàng mới
    const variantIdInput = newRow.querySelector('[name^="variantId"]');
    if (variantIdInput) variantIdInput.value = '';

    tbody.appendChild(newRow);
}

function removeRow(btn) {
    const row = btn.closest('tr');
    const tbody = document.getElementById('variantTableBody');
    // Giữ lại ít nhất 1 dòng để làm mẫu
    if (tbody.querySelectorAll('.variant-row').length > 1) {
        row.remove();
    } else {
        // Tùy chọn: Bạn có thể cho phép xóa hàng cuối cùng và chuyển sang SimpleStock
        alert("Phải có ít nhất một biến thể, hoặc bỏ chọn 'Sản phẩm này có nhiều size/màu'.");
    }
}
// Hàm Khởi tạo trạng thái ban đầu
function checkInitialState() {
    toggleVariants();
}

document.addEventListener('DOMContentLoaded', checkInitialState);