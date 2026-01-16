package com.bibliotech.bibliotech.strategies;

import com.bibliotech.bibliotech.clients.PdfMicroserviceClient;
import com.bibliotech.bibliotech.dtos.pdf.TurmasDados;
import com.bibliotech.bibliotech.dtos.pdf.PdfRequest;
import com.bibliotech.bibliotech.interfaces.PdfExportStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TurmasMaisLeitorasPdfStrategy implements PdfExportStrategy {
    private final PdfMicroserviceClient client;

    public TurmasMaisLeitorasPdfStrategy(PdfMicroserviceClient client) {
        this.client = client;
    }

    @Override
    public byte[] exportar(com.bibliotech.bibliotech.dtos.pdf.DadosRelatorio dados) throws Exception {
        if (!(dados instanceof TurmasDados t)) {
            throw new IllegalArgumentException("Dados incompatíveis para TurmasMaisLeitorasPdfStrategy");
        }
        var linhas = t.items().stream()
                .map(i -> List.of(i.serie(), i.turma(), i.quantidadeLeiturasTurma(), i.nomeAluno(), i.quantidadeLeiturasAluno()))
                .collect(Collectors.toList());
        var secao = Map.<String,Object>of(
                "componente", "tabela",
                "titulo", "Turmas Mais Leitoras",
                "colunas", List.of("Série", "Turma", "QtdTurma", "Aluno", "QtdAluno"),
                "linhas", linhas
        );
        var payload = Map.<String,Object>of("secoes", List.of(secao));
        var req = new PdfRequest("builder", "turmas-mais-leitoras.pdf", payload);
        return client.generatePdf(req);
    }
}  