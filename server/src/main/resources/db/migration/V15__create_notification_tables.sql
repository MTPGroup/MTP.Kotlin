-- 通知模板表
CREATE TABLE IF NOT EXISTS notification_templates (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(500),
    content TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- 通知模板索引
CREATE INDEX idx_notification_templates_type_channel ON notification_templates(type, channel);
CREATE INDEX idx_notification_templates_is_active ON notification_templates(is_active);

-- 通知日志表
CREATE TABLE IF NOT EXISTS notification_logs (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    content TEXT NOT NULL,
    template_id UUID REFERENCES notification_templates(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- 通知日志索引
CREATE INDEX idx_notification_logs_user_id ON notification_logs(user_id);
CREATE INDEX idx_notification_logs_status ON notification_logs(status);
CREATE INDEX idx_notification_logs_channel_status ON notification_logs(channel, status);
CREATE INDEX idx_notification_logs_created_at ON notification_logs(created_at DESC);

-- 添加约束
ALTER TABLE notification_templates ADD CONSTRAINT notification_templates_type_check
    CHECK (type IN ('OTP_VERIFICATION', 'WELCOME', 'PASSWORD_RESET', 'ACCOUNT_DELETION', 'SYSTEM_ALERT'));

ALTER TABLE notification_templates ADD CONSTRAINT notification_templates_channel_check
    CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'WEBSOCKET'));

ALTER TABLE notification_logs ADD CONSTRAINT notification_logs_channel_check
    CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'WEBSOCKET'));

ALTER TABLE notification_logs ADD CONSTRAINT notification_logs_status_check
    CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'DELIVERED'));
