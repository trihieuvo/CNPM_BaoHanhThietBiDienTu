document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('search-sdt');
    const resultsContainer = document.getElementById('search-results');
    const customerIdField = document.getElementById('khachHangId');
    const hoTenField = document.getElementById('hoTen');
    const soDienThoaiField = document.getElementById('soDienThoai');
    const emailField = document.getElementById('email');
    const diaChiField = document.getElementById('diaChi');

    function resetCustomerForm() {
        customerIdField.value = '0';
        hoTenField.value = '';
        emailField.value = '';
        diaChiField.value = '';
        soDienThoaiField.value = searchInput.value;
        hoTenField.focus();
    }

    searchInput.addEventListener('input', function() {
        const query = searchInput.value;

        if (query.length < 3) {
            resultsContainer.innerHTML = '';
            if(query.length === 0) {
                 resetCustomerForm();
                 soDienThoaiField.value = '';
            }
            return;
        }

        fetch(`/receptionist/tiep-nhan/api/customers/search?sdt=${query}`)
            .then(response => response.json())
            .then(data => {
                resultsContainer.innerHTML = '';
                if (data.length > 0) {
                    const list = document.createElement('ul');
                    list.className = 'list-group';
                    data.forEach(customer => {
                        const item = document.createElement('li');
                        item.className = 'list-group-item list-group-item-action';
                        item.textContent = `${customer.hoTen} - ${customer.soDienThoai}`;
                        item.style.cursor = 'pointer';
                        
                        item.addEventListener('click', function() {
                            customerIdField.value = customer.maKH;
                            hoTenField.value = customer.hoTen;
                            soDienThoaiField.value = customer.soDienThoai;
                            emailField.value = customer.email || '';
                            diaChiField.value = customer.diaChi || '';
                            resultsContainer.innerHTML = '';
                            searchInput.value = customer.soDienThoai;
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
