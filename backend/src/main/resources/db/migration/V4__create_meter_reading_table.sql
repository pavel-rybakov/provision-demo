CREATE TABLE meter_reading
(
    id                   uuid          NOT NULL,
    electricity_meter_id uuid          NOT NULL,
    measured_at          timestamptz   NOT NULL,
    zone_t1              numeric(19,6) NOT NULL,
    zone_t2              numeric(19,6),
    zone_t3              numeric(19,6),
    source_type          varchar(20)   NOT NULL,
    created_at           timestamptz   NOT NULL,
    updated_at           timestamptz   NOT NULL,

    CONSTRAINT pk_meter_reading PRIMARY KEY (id),
    CONSTRAINT fk_meter_reading_electricity_meter
        FOREIGN KEY (electricity_meter_id) REFERENCES electricity_meter (id),
    CONSTRAINT uq_meter_reading_meter_measured_at
        UNIQUE (electricity_meter_id, measured_at),
    CONSTRAINT ck_meter_reading_zone_t1 CHECK (zone_t1 >= 0),
    CONSTRAINT ck_meter_reading_zone_t2 CHECK (zone_t2 IS NULL OR zone_t2 >= 0),
    CONSTRAINT ck_meter_reading_zone_t3 CHECK (zone_t3 IS NULL OR zone_t3 >= 0),
    CONSTRAINT ck_meter_reading_source_type CHECK (source_type IN ('MANUAL', 'CSV'))
);

COMMENT ON TABLE meter_reading IS 'Показания приборов учёта электроэнергии';
COMMENT ON COLUMN meter_reading.id IS 'Уникальный идентификатор показания в формате UUID версии 7';
COMMENT ON COLUMN meter_reading.electricity_meter_id IS 'Идентификатор прибора учёта, для которого передано показание';
COMMENT ON COLUMN meter_reading.measured_at IS 'Дата и время снятия показания';
COMMENT ON COLUMN meter_reading.zone_t1 IS 'Накопленное значение электроэнергии для тарифной зоны T1, кВт·ч';
COMMENT ON COLUMN meter_reading.zone_t2 IS 'Накопленное значение электроэнергии для тарифной зоны T2, кВт·ч';
COMMENT ON COLUMN meter_reading.zone_t3 IS 'Накопленное значение электроэнергии для тарифной зоны T3, кВт·ч';
COMMENT ON COLUMN meter_reading.source_type IS 'Источник показания: ручной ввод или импорт CSV';
COMMENT ON COLUMN meter_reading.created_at IS 'Дата и время создания записи в системе';
COMMENT ON COLUMN meter_reading.updated_at IS 'Дата и время последнего изменения записи';

CREATE INDEX ix_meter_reading_latest
    ON meter_reading (electricity_meter_id, measured_at DESC)
    INCLUDE (zone_t1, zone_t2, zone_t3, source_type);

COMMENT ON INDEX ix_meter_reading_latest IS 'Поиск последнего показания прибора для отчётов';
