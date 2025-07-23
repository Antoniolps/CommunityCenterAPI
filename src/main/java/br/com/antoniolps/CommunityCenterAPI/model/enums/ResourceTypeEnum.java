package br.com.antoniolps.CommunityCenterAPI.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResourceTypeEnum {
    MEDICO(4),
    VOLUNTARIO(3),
    KIT_MEDICO(7),
    VEICULO(5),
    CESTA_BASICA(2);

    private final int points;
}
