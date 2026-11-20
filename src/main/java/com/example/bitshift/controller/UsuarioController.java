package com.example.bitshift.controller;

import com.example.bitshift.dto.UsuarioRequest;
import com.example.bitshift.dto.UsuarioResponse;
import com.example.bitshift.service.UsuarioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD de usuários.
 *
 * <p>Exemplos:</p>
 * <pre>
 * POST /api/usuarios {"nome":"Ana","ativo":true,"verificado":false}
 * GET  /api/usuarios
 * GET  /api/usuarios?ativo=true&amp;verificado=true
 * GET  /api/usuarios/1
 * PUT  /api/usuarios/1 {"nome":"Ana","ativo":true,"verificado":true}
 * DELETE /api/usuarios/1
 * </pre>
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest req) {
        UsuarioResponse criado = service.criar(req);
        return ResponseEntity.created(URI.create("/api/usuarios/" + criado.id())).body(criado);
    }

    @GetMapping
    public List<UsuarioResponse> listar(
            @RequestParam Optional<Boolean> ativo,
            @RequestParam Optional<Boolean> verificado) {
        return service.listar(ativo, verificado);
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
