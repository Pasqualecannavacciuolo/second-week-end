package com;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class Tavolo {
    private int numero;
    private int capienza;
}
