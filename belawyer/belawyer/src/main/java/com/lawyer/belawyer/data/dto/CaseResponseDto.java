package com.lawyer.belawyer.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CaseResponseDto {

        private Long id;
        private String status;
        private String description;
        private String title;
        private String institution;

}
