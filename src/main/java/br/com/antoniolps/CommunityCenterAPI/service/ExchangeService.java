package br.com.antoniolps.CommunityCenterAPI.service;

import br.com.antoniolps.CommunityCenterAPI.model.dtos.ExchangeRequest;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.ExchangeReturn;

import java.util.List;

public interface ExchangeService {

    void exchangeResources(ExchangeRequest dto);

    List<ExchangeReturn> getAllExchangesByCenterAfterDate(String centerId, String date);

    List<ExchangeReturn> getAllByCenterId(String centerId);


}

