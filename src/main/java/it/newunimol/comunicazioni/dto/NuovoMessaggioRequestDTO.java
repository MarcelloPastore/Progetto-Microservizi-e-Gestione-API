package it.newunimol.comunicazioni.dto;

public record NuovoMessaggioRequestDTO (
    String senderId,       // SOLO profilo dev: permette simulare mittente diverso. Ignorato in ambienti non-dev.
    String receiverId,
    Long courseContextId,
    String subject,
    String body
) {}
