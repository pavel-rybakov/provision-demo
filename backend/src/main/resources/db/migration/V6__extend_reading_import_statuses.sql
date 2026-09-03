ALTER TABLE reading_import DROP CONSTRAINT ck_reading_import_status;

ALTER TABLE reading_import
    ADD CONSTRAINT ck_reading_import_status
        CHECK (status IN ('UPLOADED', 'VALIDATING', 'READY', 'INVALID', 'APPLYING', 'FAILED', 'APPLIED'));
