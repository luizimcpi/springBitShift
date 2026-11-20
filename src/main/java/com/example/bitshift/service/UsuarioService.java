package com.example.bitshift.service;

import com.example.bitshift.domain.Usuario;
import com.example.bitshift.dto.UsuarioRequest;
import com.example.bitshift.dto.UsuarioResponse;
import com.example.bitshift.exception.ResourceNotFoundException;
import com.example.bitshift.flags.UserFlags;
import com.example.bitshift.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negócio. Toda conversão booleano ↔ bitmask passa pelos
 * setters da entidade ({@code setAtivo}/{@code setVerificado}), que usam
 * {@link UserFlags} — o controller nunca manipula bits diretamente.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest req) {
        Usuario u = new Usuario(req.nome(), req.ativoOuFalse(), req.verificadoOuFalse());
        return UsuarioResponse.de(repository.save(u));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Optional<Boolean> ativo, Optional<Boolean> verificado) {
        // Monta máscara/esperado para o filtro bitwise no banco.
        int mask = 0;
        int expected = 0;
        if (ativo.isPresent()) {
            mask |= UserFlags.ATIVO;
            if (ativo.get()) {
                expected |= UserFlags.ATIVO;
            }
        }
        if (verificado.isPresent()) {
            mask |= UserFlags.VERIFICADO;
            if (verificado.get()) {
                expected |= UserFlags.VERIFICADO;
            }
        }
        List<Usuario> base = (mask == 0)
                ? repository.findAll()
                : repository.findByFlagsMask(mask, expected);
        return base.stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.de(obter(id));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest req) {
        Usuario u = obter(id);
        u.setNome(req.nome());
        u.setAtivo(req.ativoOuFalse());
        u.setVerificado(req.verificadoOuFalse());
        return UsuarioResponse.de(repository.save(u));
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário " + id + " não encontrado");
        }
        repository.deleteById(id);
    }

    private Usuario obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário " + id + " não encontrado"));
    }
}
