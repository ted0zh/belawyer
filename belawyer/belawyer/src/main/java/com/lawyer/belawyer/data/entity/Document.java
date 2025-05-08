package com.lawyer.belawyer.data.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "documents")
@Data
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filePath;
    private String category;
    private Timestamp uploadedAt;

    @ManyToOne
    @JoinColumn(name = "case_id")
    private Case caseEntity;
}
