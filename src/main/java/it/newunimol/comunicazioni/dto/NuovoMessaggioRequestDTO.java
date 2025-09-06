package it.newunimol.comunicazioni.dto;

public record NuovoMessaggioRequestDTO (
    String receiverId,
    Long courseContextId,
    String subject,
    String body
) {}
