package com.bibliotech.bibliotech.strategies;

import com.bibliotech.bibliotech.clients.PdfMicroserviceClient;
import com.bibliotech.bibliotech.dtos.pdf.AlunosMaisLeitoresDados;
import com.bibliotech.bibliotech.dtos.pdf.PdfRequest;
import com.bibliotech.bibliotech.interfaces.PdfExportStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AlunosMaisLeitoresPdfStrategy implements PdfExportStrategy {
    private final PdfMicroserviceClient client;

    public AlunosMaisLeitoresPdfStrategy(PdfMicroserviceClient client) {
        this.client = client;
    }

    @Override
    public byte[] exportar(com.bibliotech.bibliotech.dtos.pdf.DadosRelatorio dados) throws Exception {
        if (!(dados instanceof AlunosMaisLeitoresDados a)) {
            throw new IllegalArgumentException("Dados incompatíveis para AlunosMaisLeitoresPdfStrategy");
        }

        var linhas = a.items().stream()
                .map(i -> List.of(i.nome(), i.serie(), i.turma(), i.quantidadeLeituras()))
                .collect(Collectors.toList());

        var secao = Map.<String,Object>of(
                "componente", "tabela",
                "titulo", "Alunos Mais Leitores",
                "colunas", List.of("Nome", "Série", "Turma", " Leituras"),
                "linhas", linhas
        );

        var payload = Map.<String,Object>of("secoes", List.of(secao));
        var req = new PdfRequest("builder", "alunos-mais-leitores.pdf", payload);
        return client.generatePdf(req);
    }
}