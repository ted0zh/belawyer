package com.lawyer.belawyer.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentSummaryDto {
    private Long id;
    private String name;
    private String summary;
}
