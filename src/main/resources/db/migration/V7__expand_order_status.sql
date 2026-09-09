ALTER TABLE orders
    MODIFY status ENUM(
        'PENDING_DEPOSIT',
        'PAID',
        'IN_PRODUCTION',
        'READY_TO_SHIP',
        'DELIVERED',
        'CANCELED',
        'REFUND_REQUESTED',
        'REFUNDED'
    ) NOT NULL;
