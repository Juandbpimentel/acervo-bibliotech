package com.bibliotech.bibliotech.strategies;

import com.bibliotech.bibliotech.clients.PdfMicroserviceClient;
import com.bibliotech.bibliotech.dtos.pdf.LivrosDados;
import com.bibliotech.bibliotech.dtos.pdf.PdfRequest;
import com.bibliotech.bibliotech.interfaces.PdfExportStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LivrosMaisLidosPdfStrategy implements PdfExportStrategy {
    private final PdfMicroserviceClient client;

    public LivrosMaisLidosPdfStrategy(PdfMicroserviceClient client) {
        this.client = client;
    }

    @Override
    public byte[] exportar(com.bibliotech.bibliotech.dtos.pdf.DadosRelatorio dados) throws Exception {
        if (!(dados instanceof LivrosDados l)) {
            throw new IllegalArgumentException("Dados incompatíveis para LivrosMaisLidosPdfStrategy");
        }
        var linhas = l.items().stream()
                .map(i -> List.of(i.titulo(), i.quantidadeEmprestimos()))
                .collect(Collectors.toList());

        var secao = Map.<String,Object>of(
                "componente", "tabela",
                "titulo", "Livros Mais Lidos",
                "colunas", List.of("Título", "Empréstimos"),
                "linhas", linhas
        );

        var payload = Map.<String,Object>of("secoes", List.of(secao));
        var req = new PdfRequest("builder", "livros-mais-lidos.pdf", payload);
        return client.generatePdf(req);
    }
}  