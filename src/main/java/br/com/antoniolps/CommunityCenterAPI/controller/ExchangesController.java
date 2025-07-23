package br.com.antoniolps.CommunityCenterAPI.controller;

import br.com.antoniolps.CommunityCenterAPI.model.dtos.ExchangeRequest;
import br.com.antoniolps.CommunityCenterAPI.model.dtos.ExchangeReturn;
import br.com.antoniolps.CommunityCenterAPI.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController(value = "exchanges")
@RequiredArgsConstructor
public class ExchangesController {

    private final ExchangeService exchangeService;

    @PostMapping()
    public ResponseEntity<Void> exchangeResources(ExchangeRequest dto) {
        exchangeService.exchangeResources(dto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping()
    public ResponseEntity<List<ExchangeReturn>> getAllExchangesAfterDate(String date, String centerId) {
        List<ExchangeReturn> exchanges = exchangeService.getAllExchangesByCenterAfterDate(centerId, date);
        return ResponseEntity.ok(exchanges);
    }

    @GetMapping("{centerId}/all")
    public ResponseEntity<List<ExchangeReturn>> getAllByCenterId(@PathVariable String centerId) {
        List<ExchangeReturn> exchanges = exchangeService.getAllByCenterId(centerId);
        return ResponseEntity.ok(exchanges);
    }
}
