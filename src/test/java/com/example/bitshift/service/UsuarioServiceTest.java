package com.example.bitshift.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.bitshift.domain.Usuario;
import com.example.bitshift.dto.UsuarioRequest;
import com.example.bitshift.dto.UsuarioResponse;
import com.example.bitshift.exception.ResourceNotFoundException;
import com.example.bitshift.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Testes unitários do service com repositório mockado (sem banco). */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository repository;

    @InjectMocks
    UsuarioService service;

    @Test
    void criarDeveConverterBooleanosEmBitmask() {
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponse resp = service.criar(new UsuarioRequest("Ana", true, false));

        verify(repository).save(captor.capture());
        assertEquals(1, captor.getValue().getFlags());
        assertEquals(1L, resp.id());
        assertTrue(resp.ativo());
        assertEquals(1, resp.flags());
    }

    @Test
    void criarComNulosDeveAssumirFalse() {
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse resp = service.criar(new UsuarioRequest("Zé", null, null));

        assertEquals(0, resp.flags());
    }

    @Test
    void buscarPorIdInexistenteDeveLancar404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void listarSemFiltroDeveUsarFindAll() {
        Usuario u = new Usuario("Ana", true, true);
        u.setId(1L);
        when(repository.findAll()).thenReturn(List.of(u));

        List<UsuarioResponse> lista = service.listar(Optional.empty(), Optional.empty());

        verify(repository).findAll();
        verify(repository, never()).findByFlagsMask(any(Integer.class), any(Integer.class));
        assertEquals(1, lista.size());
        assertEquals(3, lista.get(0).flags());
    }

    @Test
    void listarComFiltroDeveMontarMascaraBitwise() {
        when(repository.findByFlagsMask(1, 1)).thenReturn(List.of(new Usuario("A", true, false)));

        List<UsuarioResponse> lista = service.listar(Optional.of(true), Optional.empty());

        verify(repository).findByFlagsMask(1, 1);
        assertEquals(1, lista.size());
    }

    @Test
    void listarAtivoFalseVerificadoTrueDeveUsarMascara3Esperado2() {
        when(repository.findByFlagsMask(3, 2)).thenReturn(List.of());

        service.listar(Optional.of(false), Optional.of(true));

        verify(repository).findByFlagsMask(3, 2);
    }

    @Test
    void removerInexistenteDeveLancar404() {
        when(repository.existsById(5L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.remover(5L));
        verify(repository, never()).deleteById(any());
    }
}
