package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.MaquinaRequest;
import com.industrial.productionsystem.dto.MaquinaResponse;
import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.repository.CompanyRepository;
import com.industrial.productionsystem.repository.MaquinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.industrial.productionsystem.repository.FalhaMaquinaRepository;
import com.industrial.productionsystem.repository.OrdemDeProducaoRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaquinaServiceTest {

    @Mock private MaquinaRepository repository;
    @Mock private CompanyRepository companyRepository;
    @Mock private FalhaMaquinaRepository falhaMaquinaRepository;
    @Mock private OrdemDeProducaoRepository ordemDeProducaoRepository;
    @InjectMocks private MaquinaService service;

    private Company company;
    private final Long COMPANY_ID = 1L;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(COMPANY_ID);
        company.setName("Empresa Teste");
    }

    // ── helper ──────────────────────────────────────────────────────

    private MaquinaRequest requestValido() {
        MaquinaRequest req = new MaquinaRequest();
        req.setNome("Torno CNC 01");
        req.setTipo("CNC");
        req.setCapacidadePorHora(100);
        return req;
    }

    private Maquina maquinaSalva(String nome, StatusMaquina status) {
        Maquina m = new Maquina(nome, "CNC", 100, company);
        m.setStatus(status);
        return m;
    }

    // ── criar ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve criar máquina com status ATIVA por padrão")
    void deveCriarMaquinaComStatusPadrao() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(repository.save(any())).thenReturn(maquinaSalva("Torno CNC 01", StatusMaquina.ATIVA));

        MaquinaResponse response = service.criar(requestValido(), COMPANY_ID);

        assertNotNull(response);
        assertEquals("Torno CNC 01", response.getNome());
        assertEquals(StatusMaquina.ATIVA, response.getStatus());
        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve criar máquina com status informado no request")
    void deveCriarMaquinaComStatusInformado() {
        MaquinaRequest req = requestValido();
        req.setStatus(StatusMaquina.MANUTENCAO);

        Maquina salva = maquinaSalva("Torno CNC 01", StatusMaquina.MANUTENCAO);

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(repository.save(any())).thenReturn(salva);

        MaquinaResponse response = service.criar(req, COMPANY_ID);

        assertEquals(StatusMaquina.MANUTENCAO, response.getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar máquina para empresa inexistente")
    void deveLancarExcecaoEmpresaInexistente() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.criar(requestValido(), COMPANY_ID));
        verify(repository, never()).save(any());
    }

    // ── listar ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve listar máquinas filtrando por empresa")
    void deveListarMaquinasDaEmpresa() {
        when(repository.findByCompanyId(COMPANY_ID)).thenReturn(
                List.of(maquinaSalva("M1", StatusMaquina.ATIVA),
                        maquinaSalva("M2", StatusMaquina.INATIVA)));

        List<MaquinaResponse> lista = service.listar(COMPANY_ID);

        assertEquals(2, lista.size());
        verify(repository, times(1)).findByCompanyId(COMPANY_ID);
    }

    // ── alterarStatus ────────────────────────────────────────────────

    @Test
    @DisplayName("Deve alterar status da máquina com sucesso")
    void deveAlterarStatus() {
        Maquina maquina = maquinaSalva("Torno", StatusMaquina.ATIVA);
        when(repository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(maquina));
        when(repository.save(any())).thenReturn(maquina);

        MaquinaResponse response = service.alterarStatus(1L, StatusMaquina.MANUTENCAO, COMPANY_ID);

        assertEquals(StatusMaquina.MANUTENCAO, response.getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção ao alterar status de máquina inexistente")
    void deveLancarExcecaoMaquinaInexistente() {
        when(repository.findByIdAndCompanyId(99L, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.alterarStatus(99L, StatusMaquina.INATIVA, COMPANY_ID));
    }

    @Test
    @DisplayName("Deve lançar exceção ao alterar status com valor nulo")
    void deveLancarExcecaoStatusNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.alterarStatus(1L, null, COMPANY_ID));
        verify(repository, never()).save(any());
    }


// ── deletar ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Não deve deletar máquina com falhas vinculadas")
    void naoDeveDeletarMaquinaComFalhasVinculadas() {
        Maquina maquina = maquinaSalva("Torno CNC 01", StatusMaquina.ATIVA);

        when(repository.findByIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(Optional.of(maquina));
        when(falhaMaquinaRepository.existsByMaquinaIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.deletar(1L, COMPANY_ID)
        );

        assertEquals(
                "Não é possível excluir máquina com falhas ou ordens vinculadas",
                exception.getMessage()
        );

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Não deve deletar máquina com ordens vinculadas")
    void naoDeveDeletarMaquinaComOrdensVinculadas() {
        Maquina maquina = maquinaSalva("Torno CNC 01", StatusMaquina.ATIVA);

        when(repository.findByIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(Optional.of(maquina));
        when(falhaMaquinaRepository.existsByMaquinaIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(false);
        when(ordemDeProducaoRepository.existsByMaquinaIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.deletar(1L, COMPANY_ID)
        );

        assertEquals(
                "Não é possível excluir máquina com falhas ou ordens vinculadas",
                exception.getMessage()
        );

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve deletar máquina sem falhas ou ordens vinculadas")
    void deveDeletarMaquinaSemVinculos() {
        Maquina maquina = maquinaSalva("Torno CNC 01", StatusMaquina.ATIVA);

        when(repository.findByIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(Optional.of(maquina));
        when(falhaMaquinaRepository.existsByMaquinaIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(false);
        when(ordemDeProducaoRepository.existsByMaquinaIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(false);

        service.deletar(1L, COMPANY_ID);

        verify(repository, times(1)).delete(maquina);
    }
}
