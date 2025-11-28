package com.technova.technov.domain.controller;

import com.technova.technov.domain.dto.UserPaymentMethodDto;
import com.technova.technov.domain.service.UserPaymentMethodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-payment-methods")
@CrossOrigin("*")
public class UserPaymentMethodController {

    private final UserPaymentMethodService userPaymentMethodService;

    public UserPaymentMethodController(UserPaymentMethodService userPaymentMethodService) {
        this.userPaymentMethodService = userPaymentMethodService;
    }

    @GetMapping
    public ResponseEntity<List<UserPaymentMethodDto>> listarTodos() {
        List<UserPaymentMethodDto> metodos = userPaymentMethodService.listarTodos();
        return ResponseEntity.ok(metodos);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<UserPaymentMethodDto>> listarPorUsuario(@PathVariable Integer usuarioId) {
        List<UserPaymentMethodDto> metodos = userPaymentMethodService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(metodos);
    }

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<UserPaymentMethodDto> guardar(
            @PathVariable Integer usuarioId,
            @RequestBody UserPaymentMethodDto userPaymentMethodDto) {
        UserPaymentMethodDto guardado = userPaymentMethodService.guardar(usuarioId, userPaymentMethodDto);
        return ResponseEntity.ok(guardado);
    }
}

