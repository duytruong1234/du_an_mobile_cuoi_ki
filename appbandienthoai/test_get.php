
<?php
header('Content-Type: application/json; charset=utf-8');
echo json_encode($_GET, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
?>
