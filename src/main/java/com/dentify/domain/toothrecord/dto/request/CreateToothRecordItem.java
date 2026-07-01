package com.dentify.domain.toothrecord.dto.request;

import com.dentify.domain.toothrecord.enums.RecordType;
import com.dentify.domain.toothrecord.enums.ToothFace;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateToothRecordItem {

    @NotEmpty(message = "at least one piece number is required")
    private List<Integer> pieceNumbers;

    @NotNull(message = "record type is required")
    private RecordType recordType;

    @NotNull(message = "tooth face is required")
    private ToothFace face;

    @NotNull(message = "diagnosis id is required")
    private Long diagnosisId;

    @Size(max = 2000, message = "observations must be at most 2000 characters")
    private String observations; // nullable
}