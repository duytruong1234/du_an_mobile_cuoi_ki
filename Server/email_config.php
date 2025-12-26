<?php
// Cấu hình email để gửi OTP
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

// Nếu bạn dùng Composer, uncomment dòng này:
// require 'vendor/autoload.php';

// Nếu KHÔNG dùng Composer, download PHPMailer và include thủ công:
require 'PHPMailer/src/Exception.php';
require 'PHPMailer/src/PHPMailer.php';
require 'PHPMailer/src/SMTP.php';

function sendOTPEmail($toEmail, $otp, $userName = '') {
    $mail = new PHPMailer(true);

    try {
        // Cấu hình SMTP
        $mail->isSMTP();
        $mail->Host       = 'smtp.gmail.com'; // SMTP server của Gmail
        $mail->SMTPAuth   = true;
        $mail->Username   = 'your-email@gmail.com'; // ⚠️ THAY ĐỔI EMAIL CỦA BẠN
        $mail->Password   = 'your-app-password';    // ⚠️ THAY ĐỔI APP PASSWORD (không phải mật khẩu Gmail)
        $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
        $mail->Port       = 587;
        $mail->CharSet    = 'UTF-8';

        // Người gửi
        $mail->setFrom('your-email@gmail.com', 'App Bán Đồng Hồ'); // ⚠️ THAY ĐỔI

        // Người nhận
        $mail->addAddress($toEmail);

        // Nội dung email
        $mail->isHTML(true);
        $mail->Subject = 'Mã OTP Đặt Lại Mật Khẩu - App Bán Đồng Hồ';

        $mailContent = "
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background-color: #f9f9f9; }
                .otp-box { background-color: #fff; border: 2px solid #4CAF50; padding: 20px; margin: 20px 0; text-align: center; }
                .otp-code { font-size: 32px; font-weight: bold; color: #4CAF50; letter-spacing: 5px; }
                .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
            </style>
        </head>
        <body>
            <div class='container'>
                <div class='header'>
                    <h2>Đặt Lại Mật Khẩu</h2>
                </div>
                <div class='content'>
                    <p>Xin chào" . ($userName ? " <strong>$userName</strong>" : "") . ",</p>
                    <p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản <strong>$toEmail</strong></p>
                    <p>Đây là mã OTP của bạn:</p>
                    <div class='otp-box'>
                        <div class='otp-code'>$otp</div>
                    </div>
                    <p><strong>Lưu ý:</strong></p>
                    <ul>
                        <li>Mã OTP này có hiệu lực trong <strong>5 phút</strong></li>
                        <li>Không chia sẻ mã này với bất kỳ ai</li>
                        <li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>
                    </ul>
                </div>
                <div class='footer'>
                    <p>&copy; 2025 App Bán Đồng Hồ. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        ";

        $mail->Body = $mailContent;
        $mail->AltBody = "Mã OTP của bạn là: $otp. Mã có hiệu lực trong 5 phút.";

        $mail->send();
        return true;
    } catch (Exception $e) {
        error_log("Email Error: {$mail->ErrorInfo}");
        return false;
    }
}

// Hàm tạo mã OTP ngẫu nhiên 6 số
function generateOTP() {
    return str_pad(rand(0, 999999), 6, '0', STR_PAD_LEFT);
}
?>

