<?php

include "connect.php";

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

require 'PHPMailer/src/Exception.php';
require 'PHPMailer/src/PHPMailer.php';
require 'PHPMailer/src/SMTP.php';

header('Content-Type: application/json; charset=utf-8');

$email = $_POST['email'];
$query = "SELECT * FROM `user` WHERE `email`='".$email."'";
$data = mysqli_query($conn, $query);
$result = array();

while ($row = mysqli_fetch_assoc($data)) {
    $result[] = $row;
}

if (empty($result)) {
    $arr = [
        'success' => false,
        'message' => 'Mail khong chinh xac',
        'result' => []
    ];
} else {
    // Lấy thông tin user
    $email = $result[0]["email"];
    $pass = $result[0]["pass"]; // Token hoặc pass hiện tại

    // Tạo link reset
    $link = "http://localhost/appbandienthoai/reset_pass.php?email=".$email."&token=".$pass;

    // Gửi mail
    $mail = new PHPMailer(true);
    try {
        $mail->CharSet = "UTF-8";
        $mail->isSMTP();
        $mail->SMTPAuth = true;
        $mail->SMTPSecure = "ssl";
        $mail->Host = "smtp.gmail.com";
        $mail->Port = 465;
        $mail->Username = "truongnguyen1714@gmail.com"; // Gmail của bạn
        $mail->Password = "qqma eldm bgxo eiis"; // App Password
        $mail->setFrom("truongnguyen1714@gmail.com", "App ban dong ho");
        $mail->addAddress($email);
        $mail->isHTML(true);
        $mail->Subject = "Reset Password";
        $mail->Body = 'Click vào liên kết sau để đặt lại mật khẩu:<br><a href="'.$link.'">'.$link.'</a>';

        if ($mail->send()) {
            $arr = [
                'success' => true,
                'message' => 'Đã gửi mail thành công, vui lòng kiểm tra hộp thư đến',
                'result' => $result
            ];
        } else {
            $arr = [
                'success' => false,
                'message' => 'Không gửi được email',
                'result' => []
            ];
        }
    } catch (Exception $e) {
        $arr = [
            'success' => false,
            'message' => 'Mail Error: '.$mail->ErrorInfo,
            'result' => []
        ];
    }
}

echo json_encode($arr);
die();

?>
