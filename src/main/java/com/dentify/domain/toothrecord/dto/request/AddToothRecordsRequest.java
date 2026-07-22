package com.dentify.domain.toothrecord.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request body for {@code POST /api/tooth-records/{medicalHistoryId}}.
 *
 * <p>The path variable {@code medicalHistoryId} is authoritative and is intentionally not
 * duplicated as a field here. {@code id} and {@code createdAt} are server-generated and are
 * likewise not accepted from the client (see the nested {@code CreateToothRecordItem}).
 */
public record AddToothRecordsRequest( @NotEmpty(message = "At least one tooth record item must be provided")
                                      @Size(max = AddToothRecordsRequest.MAX_BULK_SIZE, message = "Bulk request exceeds the maximum of 15 tooth record items")
                                      @Valid
                                      List<CreateToothRecordItem> toothRecordItems) {

    /**
     * Maximum number of {@link CreateToothRecordItem} entries accepted per request
     */
    public static final int MAX_BULK_SIZE = 15;
}

