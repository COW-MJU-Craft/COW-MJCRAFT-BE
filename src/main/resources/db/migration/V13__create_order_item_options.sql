CREATE TABLE order_item_options (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_item_id BIGINT NOT NULL,
    option_value_id BIGINT NOT NULL,
    option_group_name_snapshot VARCHAR(100) NOT NULL,
    option_value_name_snapshot VARCHAR(100) NOT NULL,
    additional_price_snapshot INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_item_options_order_item FOREIGN KEY (order_item_id) REFERENCES order_items (id),
    CONSTRAINT fk_order_item_options_option_value FOREIGN KEY (option_value_id) REFERENCES item_option_values (id)
) ENGINE=InnoDB;
