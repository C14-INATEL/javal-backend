package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.Maquina;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MaquinaRepositoryTest {

    @Autowired
    private MaquinaRepository repository;

    @Test
    void deveSalvarMaquina() {
        Maquina maquina = new Maquina("Máquina Teste", "CNC", 100);

        Maquina salva = repository.save(maquina);

        assertNotNull(salva.getId());
        assertEquals("Máquina Teste", salva.getNome());
    }
}