CREATE TABLE public.otp_codes
(
    id         UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    email      TEXT                     NOT NULL,
    code       VARCHAR(10)              NOT NULL,
    type       VARCHAR(20)              NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_used    BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX otp_codes_email_type_idx ON public.otp_codes (email, type);