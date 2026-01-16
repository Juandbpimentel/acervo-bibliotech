package com.bibliotech.bibliotech.services;

import com.bibliotech.bibliotech.dtos.pdf.AcervoDados;
import com.bibliotech.bibliotech.dtos.pdf.AlunosMaisLeitoresDados;
import com.bibliotech.bibliotech.dtos.pdf.FrequenciaDados;
import com.bibliotech.bibliotech.dtos.response.AlunoLeiturasDTO;
import com.bibliotech.bibliotech.models.FrequenciaAlunos;
import com.bibliotech.bibliotech.strategies.AcervoPdfStrategy;
import com.bibliotech.bibliotech.strategies.AlunosMaisLeitoresPdfStrategy;
import com.bibliotech.bibliotech.strategies.FrequenciaPdfStrategy;
import com.bibliotech.bibliotech.strategies.OcorrenciasPdfStrategy;
import com.bibliotech.bibliotech.strategies.LivrosMaisLidosPdfStrategy;
import com.bibliotech.bibliotech.strategies.TurmasMaisLeitorasPdfStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PdfExportServiceTest {

    @Mock
    AlunosMaisLeitoresPdfStrategy alunosStrategy;

    @Mock
    FrequenciaPdfStrategy frequenciaStrategy;

    @Mock
    AcervoPdfStrategy acervoStrategy;

    @Mock
    OcorrenciasPdfStrategy ocorrenciasStrategy;

    @Mock
    LivrosMaisLidosPdfStrategy livrosStrategy;

    @Mock
    TurmasMaisLeitorasPdfStrategy turmasStrategy;

    PdfExportService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new PdfExportService(alunosStrategy, frequenciaStrategy, acervoStrategy, ocorrenciasStrategy, livrosStrategy, turmasStrategy);
    }

    @Test
    void exportAlunosMaisLeitores_callsStrategyAndReturnsBytes() throws Exception {
        var dto = new AlunoLeiturasDTO("João", 1, "A", 5L);
        byte[] fake = "pdf".getBytes();
        when(alunosStrategy.exportar(any(AlunosMaisLeitoresDados.class))).thenReturn(fake);

        byte[] result = service.exportAlunosMaisLeitores(List.of(dto));
        assertArrayEquals(fake, result);

        ArgumentCaptor<AlunosMaisLeitoresDados> captor = ArgumentCaptor.forClass(AlunosMaisLeitoresDados.class);
        verify(alunosStrategy).exportar(captor.capture());
        assertEquals(1, captor.getValue().items().size());
    }

    @Test
    void exportFrequencia_callsStrategyAndReturnsBytes() throws Exception {
        var aluno = new com.bibliotech.bibliotech.models.Aluno();
        aluno.setNome("Ana");
        var freq = new FrequenciaAlunos();
        freq.setAluno(aluno);
        freq.setAtividade("LEITURA");
        freq.setData(LocalDate.of(2026,1,15));

        byte[] fake = "pdf".getBytes();
        when(frequenciaStrategy.exportar(any(FrequenciaDados.class))).thenReturn(fake);

        byte[] result = service.exportFrequenciaAlunosToPdf(List.of(freq));
        assertArrayEquals(fake, result);

        ArgumentCaptor<FrequenciaDados> captor = ArgumentCaptor.forClass(FrequenciaDados.class);
        verify(frequenciaStrategy).exportar(captor.capture());
        assertEquals(1, captor.getValue().items().size());
    }

    @Test
    void exportRelatorioAcervo_callsStrategyAndReturnsBytes() throws Exception {
        var dto = new com.bibliotech.bibliotech.dtos.response.RelatorioAcervoDTO("X", 2, "Y");
        byte[] fake = "pdf".getBytes();
        when(acervoStrategy.exportar(any(AcervoDados.class))).thenReturn(fake);

        byte[] result = service.exportRelatorioAcervo(List.of(dto));
        assertArrayEquals(fake, result);

        ArgumentCaptor<AcervoDados> captor = ArgumentCaptor.forClass(AcervoDados.class);
        verify(acervoStrategy).exportar(captor.capture());
        assertEquals(1, captor.getValue().items().size());
    }

    @Test
    void exportLivrosMaisLidos_callsStrategyAndReturnsBytes() throws Exception {
        var dto = new com.bibliotech.bibliotech.dtos.response.LivrosMaisLidosDTO("T", 7L);
        byte[] fake = "pdf".getBytes();
        when(livrosStrategy.exportar(any(com.bibliotech.bibliotech.dtos.pdf.LivrosDados.class))).thenReturn(fake);

        byte[] result = service.exportLivrosMaisLidos(List.of(dto));
        assertArrayEquals(fake, result);
        verify(livrosStrategy).exportar(any());
    }

    @Test
    void exportTurmasMaisLeitoras_callsStrategyAndReturnsBytes() throws Exception {
        var dto = new com.bibliotech.bibliotech.dtos.response.TurmaLeiturasDTO(1, "A", 3L, "Aluno", 2L);
        byte[] fake = "pdf".getBytes();
        when(turmasStrategy.exportar(any(com.bibliotech.bibliotech.dtos.pdf.TurmasDados.class))).thenReturn(fake);

        byte[] result = service.exportTurmasMaisLeitoras(List.of(dto));
        assertArrayEquals(fake, result);
        verify(turmasStrategy).exportar(any());
    }

    @Test
    void exportOcorrencias_callsStrategyAndReturnsBytes() throws Exception {
        var aluno = new com.bibliotech.bibliotech.models.Aluno();
        aluno.setNome("Carlos");
        var user = new com.bibliotech.bibliotech.models.Usuario();
        user.setNome("Prof");
        var occ = new com.bibliotech.bibliotech.models.Ocorrencia();
        occ.setAluno(aluno); occ.setRegistradaPor(user); occ.setDetalhes("X"); occ.setData(java.time.LocalDate.of(2026,1,15));

        byte[] fake = "pdf".getBytes();
        when(ocorrenciasStrategy.exportar(any(com.bibliotech.bibliotech.dtos.pdf.OcorrenciasDados.class))).thenReturn(fake);

        byte[] result = service.exportOcorrenciasToPdf(List.of(occ));
        assertArrayEquals(fake, result);
        verify(ocorrenciasStrategy).exportar(any());
    }
}
