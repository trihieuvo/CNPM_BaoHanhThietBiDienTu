document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('search-sdt');
    const resultsContainer = document.getElementById('search-results');
    const customerIdField = document.getElementById('khachHangId');
    const hoTenField = document.getElementById('hoTen');
    const soDienThoaiField = document.getElementById('soDienThoai');
    const emailField = document.getElementById('email');
    const diaChiField = document.getElementById('diaChi');

    // Hàm để reset form về trạng thái thêm khách mới
    function resetCustomerForm() {
        customerIdField.value = '0';
        hoTenField.value = '';
        emailField.value = '';
        diaChiField.value = '';
        // Giữ lại SĐT người dùng đã gõ để tiện cho việc thêm mới
        soDienThoaiField.value = searchInput.value; 
        hoTenField.focus();
    }

    // Lắng nghe sự kiện gõ phím trong ô tìm kiếm
    searchInput.addEventListener('input', function() {
        const query = searchInput.value;

        // Nếu ô tìm kiếm trống, dọn dẹp kết quả và reset form
        if (query.length < 3) { // Chỉ tìm khi có ít nhất 3 ký tự
            resultsContainer.innerHTML = '';
            if(query.length === 0) {
                 resetCustomerForm();
                 soDienThoaiField.value = '';
            }
            return;
        }

        // Gọi API tìm kiếm của server
        fetch(`/receptionist/tiep-nhan/api/customers/search?sdt=${query}`)
            .then(response => response.json())
            .then(data => {
                resultsContainer.innerHTML = ''; // Xóa kết quả cũ

                if (data.length > 0) {
                    const list = document.createElement('ul');
                    list.className = 'list-group';
                    data.forEach(customer => {
                        const item = document.createElement('li');
                        item.className = 'list-group-item list-group-item-action';
                        item.textContent = `${customer.hoTen} - ${customer.soDienThoai}`;
                        item.style.cursor = 'pointer';
                        
                        // Khi click chọn một khách hàng từ kết quả
                        item.addEventListener('click', function() {
                            customerIdField.value = customer.maKH;
                            hoTenField.value = customer.hoTen;
                            soDienThoaiField.value = customer.soDienThoai;
                            emailField.value = customer.email || '';
                            diaChiField.value = customer.diaChi || '';
                            resultsContainer.innerHTML = ''; // Ẩn danh sách kết quả
                            searchInput.value = customer.soDienThoai; // Cập nhật ô search
                        });
                        list.appendChild(item);
                    });
                    resultsContainer.appendChild(list);
                } else {
                     resultsContainer.innerHTML = '<div class="alert alert-warning p-2 small">Không tìm thấy khách hàng. Vui lòng nhập thông tin để thêm mới.</div>';
                     resetCustomerForm();
                }
            })
            .catch(error => {
                console.error('Lỗi khi tìm kiếm khách hàng:', error);
                resultsContainer.innerHTML = '<div class="alert alert-danger p-2 small">Lỗi kết nối máy chủ.</div>';
            });
    });
});