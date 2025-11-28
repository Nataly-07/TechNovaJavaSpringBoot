package com.technova.technov.domain.controller;

import com.technova.technov.domain.dto.PagoDto;
import com.technova.technov.domain.service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin("*")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    public ResponseEntity<List<PagoDto>> listarTodos() {
        List<PagoDto> pagos = pagoService.listarTodos();
        return ResponseEntity.ok(pagos);
    }

    @PostMapping
    public ResponseEntity<PagoDto> registrar(@RequestBody PagoDto pagoDto) {
        PagoDto registrado = pagoService.registrar(pagoDto);
        return ResponseEntity.ok(registrado);
    }
}

