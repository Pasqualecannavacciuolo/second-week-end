package com;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class Prenotazione {
    private String cognome;
    private String data;
    private int numeroPersone;
    private String cellulare;
    private int numeroTavolo;
}
