// Hàm định dạng tiền tệ
function formatCurrency(number) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(number);
}

// Hàm chuyển Tab
function openTab(evt, tabName) {
    var i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName("tab-content");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName("tab-btn");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].classList.remove("active");
        tablinks[i].style.borderBottom = "2px solid transparent";
    }
    document.getElementById(tabName).style.display = "block";
    if (evt) {
        evt.currentTarget.classList.add("active");
        evt.currentTarget.style.borderBottom = "2px solid black";
    }
}

// Hàm chọn Size
function selectSize(btn) {
    document.querySelectorAll('.size-btn').forEach(el => el.classList.remove('selected'));
    btn.classList.add('selected');
    // Lưu size đã chọn vào biến toàn cục window (nếu cần truy cập từ bên ngoài)
    window.currentSelectedSize = btn.innerText;

    const errorMsg = document.getElementById('size-error');
    if(errorMsg) errorMsg.classList.add('hidden');
}

// Hàm đổi ảnh chính
function changeImage(element) {
    const newSrc = element.querySelector('img').src;
    document.getElementById('mainImage').src = newSrc;
    document.querySelectorAll('.border-black').forEach(el => el.classList.remove('border-black', 'border-2'));
    element.classList.add('border-black', 'border-2');
}

// Hàm xem trước ảnh upload (Review)
function previewImages(input) {
    const container = document.getElementById('imagePreviewContainer');
    container.innerHTML = '';
    if (input.files) {
        const files = Array.from(input.files).slice(0, 5);
        files.forEach(file => {
            const reader = new FileReader();
            reader.onload = function(e) {
                const imgDiv = document.createElement('div');
                imgDiv.className = 'w-16 h-16 border rounded overflow-hidden relative shadow-sm';
                imgDiv.innerHTML = `<img src="${e.target.result}" class="w-full h-full object-cover">`;
                container.appendChild(imgDiv);
            }
            reader.readAsDataURL(file);
        });
    }
}

// Hàm chọn sao đánh giá
function rate(star) {
    const inputRating = document.getElementById('ratingValue');
    if(inputRating) inputRating.value = star;

    const container = document.getElementById('starContainer');
    if(container) {
        const stars = container.getElementsByTagName('i');
        for (let i = 0; i < stars.length; i++) {
            if (i < star) {
                stars[i].classList.remove('text-gray-300');
                stars[i].classList.add('text-yellow-400');
            } else {
                stars[i].classList.remove('text-yellow-400');
                stars[i].classList.add('text-gray-300');
            }
        }
    }
}