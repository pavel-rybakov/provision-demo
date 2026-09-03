CREATE TABLE reading_import
(
    id                     uuid         NOT NULL,
    original_filename      varchar(255) NOT NULL,
    file_hash              varchar(64)  NOT NULL,
    status                 varchar(20)  NOT NULL,
    uploaded_by_account_id uuid         NOT NULL,
    total_rows             integer      NOT NULL DEFAULT 0,
    valid_rows             integer      NOT NULL DEFAULT 0,
    invalid_rows           integer      NOT NULL DEFAULT 0,
    created_at             timestamptz  NOT NULL,
    validated_at           timestamptz,
    applied_at             timestamptz,

    CONSTRAINT pk_reading_import PRIMARY KEY (id),
    CONSTRAINT uq_reading_import_file_hash UNIQUE (file_hash),
    CONSTRAINT fk_reading_import_uploaded_by_account
        FOREIGN KEY (uploaded_by_account_id) REFERENCES account (id),
    CONSTRAINT ck_reading_import_status CHECK (status IN ('UPLOADED', 'READY', 'INVALID', 'APPLIED'))
);

CREATE TABLE reading_import_row
(
    id                     uuid          NOT NULL,
    reading_import_id      uuid          NOT NULL,
    row_number             integer       NOT NULL,
    meter_serial_number    varchar(100)  NOT NULL,
    measured_at            timestamptz,
    zone_t1                numeric(19,6),
    zone_t2                numeric(19,6),
    zone_t3                numeric(19,6),
    electricity_meter_id   uuid,
    validation_error       varchar(2000),

    CONSTRAINT pk_reading_import_row PRIMARY KEY (id),
    CONSTRAINT fk_reading_import_row_import FOREIGN KEY (reading_import_id) REFERENCES reading_import (id),
    CONSTRAINT fk_reading_import_row_meter FOREIGN KEY (electricity_meter_id) REFERENCES electricity_meter (id),
    CONSTRAINT uq_reading_import_row_number UNIQUE (reading_import_id, row_number)
);

COMMENT ON TABLE reading_import IS 'Импорты показаний приборов учёта из CSV-файлов';
COMMENT ON COLUMN reading_import.id IS 'Уникальный идентификатор импорта в формате UUID версии 7';
COMMENT ON COLUMN reading_import.original_filename IS 'Исходное имя CSV-файла';
COMMENT ON COLUMN reading_import.file_hash IS 'SHA-256 хеш содержимого файла для защиты от повторной загрузки';
COMMENT ON COLUMN reading_import.status IS 'Статус обработки импорта';
COMMENT ON COLUMN reading_import.uploaded_by_account_id IS 'Пользователь, загрузивший CSV-файл';
COMMENT ON COLUMN reading_import.total_rows IS 'Общее количество строк данных';
COMMENT ON COLUMN reading_import.valid_rows IS 'Количество успешно проверенных строк';
COMMENT ON COLUMN reading_import.invalid_rows IS 'Количество строк с ошибками';
COMMENT ON COLUMN reading_import.created_at IS 'Дата и время загрузки импорта';
COMMENT ON COLUMN reading_import.validated_at IS 'Дата и время завершения проверки';
COMMENT ON COLUMN reading_import.applied_at IS 'Дата и время применения импорта';

COMMENT ON TABLE reading_import_row IS 'Подготовленные строки импорта показаний';
COMMENT ON COLUMN reading_import_row.id IS 'Уникальный идентификатор строки в формате UUID версии 7';
COMMENT ON COLUMN reading_import_row.reading_import_id IS 'Идентификатор импорта';
COMMENT ON COLUMN reading_import_row.row_number IS 'Номер строки в исходном CSV-файле';
COMMENT ON COLUMN reading_import_row.meter_serial_number IS 'Серийный номер прибора из CSV-файла';
COMMENT ON COLUMN reading_import_row.measured_at IS 'Дата и время снятия показания';
COMMENT ON COLUMN reading_import_row.zone_t1 IS 'Значение тарифной зоны T1';
COMMENT ON COLUMN reading_import_row.zone_t2 IS 'Значение тарифной зоны T2';
COMMENT ON COLUMN reading_import_row.zone_t3 IS 'Значение тарифной зоны T3';
COMMENT ON COLUMN reading_import_row.electricity_meter_id IS 'Идентификатор найденного при проверке прибора';
COMMENT ON COLUMN reading_import_row.validation_error IS 'Описание ошибки проверки строки';
