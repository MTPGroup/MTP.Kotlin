<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>密码已更改</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }

        .container {
            background-color: #ffffff;
            border-radius: 8px;
            padding: 40px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .header {
            text-align: center;
            margin-bottom: 30px;
        }

        .logo {
            font-size: 28px;
            font-weight: bold;
            color: #6366f1;
        }

        .title {
            font-size: 24px;
            font-weight: 600;
            color: #1f2937;
            margin-bottom: 20px;
        }

        .icon-container {
            text-align: center;
            margin: 30px 0;
        }

        .icon {
            display: inline-block;
            font-size: 48px;
            background-color: #f0fdf4;
            padding: 16px;
            border-radius: 50%;
        }

        .message {
            color: #6b7280;
            font-size: 14px;
            margin-bottom: 20px;
        }

        .warning {
            background-color: #fef3c7;
            border-left: 4px solid #f59e0b;
            padding: 12px 16px;
            margin: 20px 0;
            font-size: 13px;
            color: #92400e;
        }

        .footer {
            margin-top: 40px;
            padding-top: 20px;
            border-top: 1px solid #e5e7eb;
            font-size: 12px;
            color: #9ca3af;
            text-align: center;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="logo">Azusa</div>
    </div>

    <h1 class="title">密码已更改</h1>

    <div class="icon-container">
        <div class="icon">&#x1F512;</div>
    </div>

    <p class="message">您的账户密码已成功更改。所有已登录的设备已被强制登出，您需要使用新密码重新登录。</p>

    <div class="warning">
        <strong>安全提示：</strong> 如果这不是您本人的操作，请立即联系我们的客服团队，您的账户可能已被他人访问。
    </div>

    <div class="footer">
        <p>此邮件由系统自动发送，请勿直接回复。</p>
        <p>&copy; 2026 Azusa. 版权所有。</p>
    </div>
</div>
</body>
</html>
