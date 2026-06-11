package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.FalhaMaquinaRequest;
import com.industrial.productionsystem.dto.FalhaMaquinaResponse;
import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.entity.FalhaMaquina;
import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.entity.enums.SeveridadeFalha;
import com.industrial.productionsystem.entity.enums.StatusFalha;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.exception.NotFoundException;
import com.industrial.productionsystem.repository.CompanyRepository;
import com.industrial.productionsystem.repository.FalhaMaquinaRepository;
import com.industrial.productionsystem.repository.MaquinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FalhaMaquinaServiceTest {

    @Mock private FalhaMaquinaRepository repository;
    @Mock private MaquinaRepository maquinaRepository;
    @Mock private CompanyRepository companyRepository;
    @InjectMocks private FalhaMaquinaService service;

    private Company company;
    private Maquina maquina;
    private final Long COMPANY_ID = 1L;
    private final Long MAQUINA_ID = 10L;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(COMPANY_ID);
        company.setName("Empresa Teste");

        maquina = new Maquina("Torno CNC 01", "CNC", 100, company);
        maquina.setId(MAQUINA_ID);
        maquina.setStatus(StatusMaquina.ATIVA);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private FalhaMaquinaRequest requestValido() {
        FalhaMaquinaRequest req = new FalhaMaquinaRequest();
        req.setMaquinaId(MAQUINA_ID);
        req.setDescricao("Motor superaquecendo");
        req.setSeveridade(SeveridadeFalha.ALTA);
        return req;
    }

    private FalhaMaquina falhaComStatus(StatusFalha status) {
        FalhaMaquina f = new FalhaMaquina();
        f.setId(100L);
        f.setDescricao("Motor superaquecendo");
        f.setSeveridade(SeveridadeFalha.ALTA);
        f.setStatus(status);
        f.setMaquina(maquina);
        f.setCompany(company);
        return f;
    }

    // ── registrar ────────────────────────────────────────────────────

    @Test
    @DisplayName("Registrar falha deve colocar a máquina em MANUTENCAO")
    void registrarDeveColocarMaquinaEmManutencao() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(maquinaRepository.findByIdAndCompanyId(MAQUINA_ID, COMPANY_ID))
                .thenReturn(Optional.of(maquina));
        when(repository.save(any())).thenReturn(falhaComStatus(StatusFalha.ABERTA));

        FalhaMaquinaResponse response = service.registrar(requestValido(), COMPANY_ID);

        assertNotNull(response);
        assertEquals(StatusFalha.ABERTA, response.getStatus());

        ArgumentCaptor<Maquina> captor = ArgumentCaptor.forClass(Maquina.class);
        verify(maquinaRepository, times(1)).save(captor.capture());
        assertEquals(StatusMaquina.MANUTENCAO, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Registrar falha em máquina inexistente deve lançar NotFoundException")
    void registrarFalhaMaquinaInexistente() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(maquinaRepository.findByIdAndCompanyId(MAQUINA_ID, COMPANY_ID))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.registrar(requestValido(), COMPANY_ID));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar falha para empresa inexistente deve lançar NotFoundException")
    void registrarFalhaEmpresaInexistente() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.registrar(requestValido(), COMPANY_ID));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar falha deve guardar status anterior da máquina")
    void registrarFalhaDeveGuardarStatusAnteriorDaMaquina() {
        maquina.setStatus(StatusMaquina.INATIVA);

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(maquinaRepository.findByIdAndCompanyId(MAQUINA_ID, COMPANY_ID))
                .thenReturn(Optional.of(maquina));
        when(repository.save(any())).thenReturn(falhaComStatus(StatusFalha.ABERTA));

        service.registrar(requestValido(), COMPANY_ID);

        ArgumentCaptor<Maquina> captor = ArgumentCaptor.forClass(Maquina.class);
        verify(maquinaRepository, times(1)).save(captor.capture());

        Maquina maquinaSalva = captor.getValue();

        assertEquals(StatusMaquina.MANUTENCAO, maquinaSalva.getStatus());
        assertEquals(StatusMaquina.INATIVA, maquinaSalva.getStatusAnteriorManutencao());
    }


    // ── resolver ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Resolver última falha aberta deve voltar a máquina para ATIVA")
    void resolverUltimaFalhaVoltaMaquinaParaAtiva() {
        maquina.setStatus(StatusMaquina.MANUTENCAO);
        maquina.setStatusAnteriorManutencao(StatusMaquina.ATIVA);
        FalhaMaquina falha = falhaComStatus(StatusFalha.ABERTA);

        when(repository.findByIdAndCompanyId(100L, COMPANY_ID)).thenReturn(Optional.of(falha));
        when(repository.save(any())).thenReturn(falha);
        when(repository.existsByMaquinaIdAndStatus(MAQUINA_ID, StatusFalha.ABERTA))
                .thenReturn(false);

        FalhaMaquinaResponse response = service.resolver(100L, COMPANY_ID);

        assertEquals(StatusFalha.RESOLVIDA, response.getStatus());
        assertNotNull(response.getDataResolucao());

        ArgumentCaptor<Maquina> captor = ArgumentCaptor.forClass(Maquina.class);
        verify(maquinaRepository, times(1)).save(captor.capture());
        assertEquals(StatusMaquina.ATIVA, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Resolver falha quando ainda há outra aberta deve manter a máquina em MANUTENCAO")
    void resolverComOutraFalhaAbertaMantemManutencao() {
        maquina.setStatus(StatusMaquina.MANUTENCAO);
        FalhaMaquina falha = falhaComStatus(StatusFalha.ABERTA);

        when(repository.findByIdAndCompanyId(100L, COMPANY_ID)).thenReturn(Optional.of(falha));
        when(repository.save(any())).thenReturn(falha);
        when(repository.existsByMaquinaIdAndStatus(MAQUINA_ID, StatusFalha.ABERTA))
                .thenReturn(true);

        service.resolver(100L, COMPANY_ID);

        verify(maquinaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Resolver falha inexistente deve lançar NotFoundException")
    void resolverFalhaInexistente() {
        when(repository.findByIdAndCompanyId(999L, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.resolver(999L, COMPANY_ID));
    }

    @Test
    @DisplayName("Resolver última falha deve restaurar status anterior INATIVA")
    void resolverUltimaFalhaDeveRestaurarStatusAnteriorInativa() {
        maquina.setStatus(StatusMaquina.MANUTENCAO);
        maquina.setStatusAnteriorManutencao(StatusMaquina.INATIVA);

        FalhaMaquina falha = falhaComStatus(StatusFalha.ABERTA);

        when(repository.findByIdAndCompanyId(100L, COMPANY_ID)).thenReturn(Optional.of(falha));
        when(repository.save(any())).thenReturn(falha);
        when(repository.existsByMaquinaIdAndStatus(MAQUINA_ID, StatusFalha.ABERTA))
                .thenReturn(false);

        FalhaMaquinaResponse response = service.resolver(100L, COMPANY_ID);

        assertEquals(StatusFalha.RESOLVIDA, response.getStatus());

        ArgumentCaptor<Maquina> captor = ArgumentCaptor.forClass(Maquina.class);
        verify(maquinaRepository, times(1)).save(captor.capture());

        Maquina maquinaSalva = captor.getValue();

        assertEquals(StatusMaquina.INATIVA, maquinaSalva.getStatus());
        assertNull(maquinaSalva.getStatusAnteriorManutencao());
    }


    // ── listar ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve listar falhas filtrando por empresa")
    void deveListarFalhasDaEmpresa() {
        when(repository.findByCompanyId(COMPANY_ID))
                .thenReturn(List.of(falhaComStatus(StatusFalha.ABERTA),
                        falhaComStatus(StatusFalha.RESOLVIDA)));

        List<FalhaMaquinaResponse> lista = service.listar(COMPANY_ID);

        assertEquals(2, lista.size());
        verify(repository, times(1)).findByCompanyId(COMPANY_ID);
    }

    @Test
    @DisplayName("Deve listar o histórico de falhas de uma máquina específica")
    void deveListarFalhasPorMaquina() {
        when(repository.findByMaquinaIdAndCompanyId(MAQUINA_ID, COMPANY_ID))
                .thenReturn(List.of(falhaComStatus(StatusFalha.RESOLVIDA)));

        List<FalhaMaquinaResponse> lista = service.listarPorMaquina(MAQUINA_ID, COMPANY_ID);

        assertEquals(1, lista.size());
        verify(repository, times(1)).findByMaquinaIdAndCompanyId(MAQUINA_ID, COMPANY_ID);
    }
}