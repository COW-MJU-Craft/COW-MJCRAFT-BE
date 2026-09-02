ALTER TABLE order_fulfillment
    ADD COLUMN tracking_information VARCHAR(500) NULL AFTER delivery_memo;
