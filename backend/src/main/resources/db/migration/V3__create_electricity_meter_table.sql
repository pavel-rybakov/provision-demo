CREATE TABLE electricity_meter
(
    id                       uuid          NOT NULL,
    created_by_account_id    uuid          NOT NULL,
    updated_by_account_id    uuid,
    created_at               timestamptz   NOT NULL,
    updated_at               timestamptz   NOT NULL,
    serial_number            varchar(100)  NOT NULL,
    inventory_number         varchar(100)  NOT NULL,
    manufacture_year         integer,
    transformation_ratio     numeric(19,6),
    installation_date        date,
    seal_number              varchar(100),
    antimagnetic_seal_number varchar(100),
    installation_location    varchar(500),
    note                     varchar(2000),
    gis_housing_id           varchar(100),

    CONSTRAINT pk_electricity_meter PRIMARY KEY (id),
    CONSTRAINT fk_electricity_meter_created_by_account
        FOREIGN KEY (created_by_account_id) REFERENCES account (id),
    CONSTRAINT fk_electricity_meter_updated_by_account
        FOREIGN KEY (updated_by_account_id) REFERENCES account (id),
    CONSTRAINT uq_electricity_meter_serial_number UNIQUE (serial_number),
    CONSTRAINT uq_electricity_meter_inventory_number UNIQUE (inventory_number),
    CONSTRAINT uq_electricity_meter_gis_housing_id UNIQUE (gis_housing_id),
    CONSTRAINT ck_electricity_meter_manufacture_year CHECK (manufacture_year BETWEEN 1800 AND 9999),
    CONSTRAINT ck_electricity_meter_transformation_ratio CHECK (transformation_ratio > 0)
);

COMMENT ON TABLE electricity_meter IS 'Приборы учёта электроэнергии';
COMMENT ON COLUMN electricity_meter.id IS 'Уникальный идентификатор прибора учёта в формате UUID версии 7';
COMMENT ON COLUMN electricity_meter.created_by_account_id IS 'Идентификатор учётной записи пользователя, создавшего прибор учёта';
COMMENT ON COLUMN electricity_meter.updated_by_account_id IS 'Идентификатор учётной записи пользователя, последним изменившего прибор учёта';
COMMENT ON COLUMN electricity_meter.created_at IS 'Дата и время создания прибора учёта в системе';
COMMENT ON COLUMN electricity_meter.updated_at IS 'Дата и время последнего изменения прибора учёта';
COMMENT ON COLUMN electricity_meter.serial_number IS 'Серийный номер прибора учёта';
COMMENT ON COLUMN electricity_meter.inventory_number IS 'Инвентарный номер прибора учёта';
COMMENT ON COLUMN electricity_meter.manufacture_year IS 'Год изготовления прибора учёта';
COMMENT ON COLUMN electricity_meter.transformation_ratio IS 'Коэффициент трансформации прибора учёта';
COMMENT ON COLUMN electricity_meter.installation_date IS 'Дата установки прибора учёта';
COMMENT ON COLUMN electricity_meter.seal_number IS 'Номер пломбы';
COMMENT ON COLUMN electricity_meter.antimagnetic_seal_number IS 'Номер антимагнитной пломбы';
COMMENT ON COLUMN electricity_meter.installation_location IS 'Место установки прибора учёта';
COMMENT ON COLUMN electricity_meter.note IS 'Примечание к прибору учёта';
COMMENT ON COLUMN electricity_meter.gis_housing_id IS 'Идентификатор прибора учёта в ГИС ЖКХ';
