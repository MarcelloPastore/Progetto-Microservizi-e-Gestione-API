package it.newunimol.comunicazioni.dto;

import java.time.LocalDateTime;

import it.newunimol.comunicazioni.model.ReadStatus;

public record MessaggioDTO (
    Long id,
    String senderId,
    String receiverId,
    Long courseContextId,
    String subject,
    String body,
    LocalDateTime timestamp,
    ReadStatus readStatus
){}
