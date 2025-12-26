-- Thêm cột trạng thái tài khoản vào bảng user
-- status: 1 = Active (hoạt động), 0 = Locked (bị khóa)

ALTER TABLE user ADD COLUMN IF NOT EXISTS status INT DEFAULT 1 COMMENT '1=Active, 0=Locked';

-- Cập nhật tất cả tài khoản hiện có thành Active
UPDATE user SET status = 1 WHERE status IS NULL;

-- Tạo index cho việc query nhanh hơn
CREATE INDEX IF NOT EXISTS idx_user_status ON user(status);

SELECT 'Account status column added successfully!' AS message;

