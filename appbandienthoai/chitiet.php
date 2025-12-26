    <?php
    include "connect.php";

    $page = $_POST['page'];
    $total = 5; // số sản phẩm hiển thị trên 1 trang
    $pos = ($page - 1) * $total; // vị trí bắt đầu của LIMIT
    $loai = $_POST['loai'];

    // Câu truy vấn
    $query = "SELECT * FROM sanphammoi WHERE loai = '$loai' LIMIT $pos, $total";
    $data = mysqli_query($conn, $query);

    $result = array();
    while ($row = mysqli_fetch_assoc($data)) {
        $result[] = $row;
    }

    // Kiểm tra dữ liệu trả về
    if (!empty($result)) {
        $arr = [
            'success' => true,
            'message' => 'thành công',
            'result'  => $result
        ];
    } else {
        $arr = [
            'success' => false,
            'message' => 'thành công',
            'result'  => $result
        ];
    }

    // Trả kết quả dạng JSON
    print_r(json_encode($arr));
    ?>
