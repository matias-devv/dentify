CREATE UNIQUE INDEX uq_one_active_treatment_per_patient_product
    ON treatments(id_patient, id_product)
    WHERE treatment_status = 'CREATED';