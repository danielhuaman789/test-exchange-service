package pe.test.exchange.application.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.application.service.ConversionApplicationService;

@RestController
@RequestMapping("/api/conversion")
public class ConversionController {

    private final ConversionApplicationService service;

    public ConversionController(
            ConversionApplicationService service
    ) {
        this.service = service;
    }

    @PostMapping
    public ConversionResponse convertir(
            @RequestBody ConversionRequest request
    ) {
        return service.convertir(request);
    }
}
