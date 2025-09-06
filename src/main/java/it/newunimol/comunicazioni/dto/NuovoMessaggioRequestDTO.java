package it.newunimol.comunicazioni.dto;

public record NuovoMessaggioRequestDTO (
    String senderId,       // opzionale: se valorizzato forza il mittente invece del JWT
    String receiverId,
    Long courseContextId,
    String subject,
    String body
) {}
