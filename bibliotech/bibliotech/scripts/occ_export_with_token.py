#!/usr/bin/env python3
"""
Uso: python scripts/occ_export_with_token.py --token <JWT> [--base-url http://localhost:8090] [--registrada-por 1]

O script:
 - cria um aluno (POST /alunos)
 - registra uma ocorrência (POST /ocorrencias)
 - solicita /ocorrencias/export/pdf e salva o arquivo localmente

Retorna código 0 em sucesso, imprime mensagens e o caminho do PDF salvo.
"""

import argparse
import requests
import sys
from datetime import datetime


def create_student(base, token, nome, email, id_turma=None):
    url = f"{base}/alunos"
    headers = {"Authorization": f"Bearer {token}"}
    payload = {"nome": nome, "email": email}
    if id_turma is not None:
        payload["idTurma"] = id_turma
    r = requests.post(url, json=payload, headers=headers)
    r.raise_for_status()
    return r.json()


def create_turma(base, token, serie=3, turma_name='A', ano=2025, ativo=True):
    url = f"{base}/turmas"
    headers = {"Authorization": f"Bearer {token}"}
    payload = {"serie": serie, "turma": turma_name, "anoDeEntrada": ano, "ativo": ativo}
    r = requests.post(url, json=payload, headers=headers)
    r.raise_for_status()
    return r.json()


def list_turmas(base, token, serie=None, turma=None, anoDeEntrada=None):
    url = f"{base}/turmas/filtrar"
    headers = {"Authorization": f"Bearer {token}"}
    params = {}
    if serie is not None:
        params['serie'] = serie
    if turma is not None:
        params['turma'] = turma
    if anoDeEntrada is not None:
        params['anoDeEntrada'] = anoDeEntrada
    r = requests.get(url, headers=headers, params=params)
    r.raise_for_status()
    return r.json()


def register_occurrence(base, token, id_aluno, registradaPor, detalhes):
    url = f"{base}/ocorrencias"
    headers = {"Authorization": f"Bearer {token}"}
    payload = {"idAluno": id_aluno, "registradaPor": registradaPor, "detalhes": detalhes}
    r = requests.post(url, json=payload, headers=headers)
    r.raise_for_status()
    return r.json()


def export_pdf(base, token, out_file):
    url = f"{base}/ocorrencias/export/pdf"
    headers = {"Authorization": f"Bearer {token}", "Accept": "application/pdf"}
    r = requests.get(url, headers=headers, stream=True)
    r.raise_for_status()
    with open(out_file, "wb") as f:
        for chunk in r.iter_content(8192):
            f.write(chunk)
    return out_file


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--token", required=True, help="JWT token with admin privileges")
    parser.add_argument("--base-url", default="http://localhost:8090", help="Base URL do backend")
    parser.add_argument("--registrada-por", type=int, default=1, help="ID do usuário que registra a ocorrência")
    args = parser.parse_args()

    token = args.token.strip()
    base = args.base_url.rstrip('/')
    ts = datetime.utcnow().strftime("%Y%m%d%H%M%S")

    try:
        print("Criando aluno de teste...")
        try:
            aluno = create_student(base, token, f"Aluno Token {ts}", f"aluno.token.{ts}@local")
        except requests.HTTPError as e:
            # If creation failed due to missing turma, create a turma and retry
            status = None
            body_text = None
            if e.response is not None:
                status = e.response.status_code
                try:
                    body_text = e.response.text
                except Exception:
                    body_text = None
            if status == 400 and body_text and 'turma' in body_text.lower():
                print("Criação de aluno falhou por falta de turma; tentando criar uma turma automaticamente...")
                try:
                    turma = create_turma(base, token)
                    print("Turma criada:", turma)
                    turma_id = turma.get('id')
                except requests.HTTPError as e2:
                    # If turma already exists, fetch existing turmas
                    print("Falha ao criar turma (possivelmente já existe); buscando turmas existentes...")
                    try:
                        turmas = list_turmas(base, token)
                        if len(turmas) == 0:
                            print("Nenhuma turma encontrada, re-raise original error.")
                            raise
                        turma_id = turmas[0].get('id')
                        print("Usando turma existente id=", turma_id)
                    except Exception:
                        raise

                print("Tentando criar aluno novamente com idTurma=", turma_id)
                aluno = create_student(base, token, f"Aluno Token {ts}", f"aluno.token.{ts}@local", id_turma=turma_id)
            else:
                raise

        print("Aluno criado:", aluno)

        print("Registrando ocorrência...")
        occ = register_occurrence(base, token, aluno.get('id'), args.registrada_por, f"Ocorrencia via token {ts}")
        print("Ocorrência registrada:", occ)

        out_file = f"out_ocorrencias_token_{ts}.pdf"
        print("Baixando PDF para:", out_file)
        export_pdf(base, token, out_file)
        print("PDF salvo em:", out_file)
        sys.exit(0)

    except requests.HTTPError as e:
        print("Erro HTTP:", e, file=sys.stderr)
        if e.response is not None:
            try:
                print(e.response.status_code, e.response.text, file=sys.stderr)
            except Exception:
                pass
        sys.exit(2)
    except Exception as e:
        print("Erro:", e, file=sys.stderr)
        sys.exit(3)

if __name__ == '__main__':
    main()
