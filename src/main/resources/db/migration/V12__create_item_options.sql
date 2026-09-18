CREATE TABLE item_option_groups (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    required BIT(1) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_item_option_groups_item FOREIGN KEY (item_id) REFERENCES project_items (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE item_option_values (
    id BIGINT NOT NULL AUTO_INCREMENT,
    option_group_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    additional_price INT NOT NULL DEFAULT 0,
    stock_qty INT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_item_option_values_group FOREIGN KEY (option_group_id) REFERENCES item_option_groups (id) ON DELETE CASCADE
) ENGINE=InnoDB;
