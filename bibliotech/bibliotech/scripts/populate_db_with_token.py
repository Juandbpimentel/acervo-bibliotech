#!/usr/bin/env python3
"""
Populate the database with sample data using a bibliotecario JWT.

Actions performed (configurable):
 - ensure there is at least one `secao` and one `estanteprateleira`
 - create N books and add exemplars
 - create M students
 - create K loans and immediately conclude them
 - create P frequency records for students

Usage:
  python scripts/populate_db_with_token.py --token <JWT> [--base-url http://localhost:8090] [--books 3] [--students 5] [--loans 5] [--freqs 5]

This script only fills sample data and prints created IDs.
"""

import argparse
import sys
import requests
import random
from datetime import datetime, timezone


def auth_headers(token):
    return {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}


def get_or_create_secao(base, token):
    url = f"{base}/secoes"
    headers = auth_headers(token)
    r = requests.get(url, headers=headers)
    r.raise_for_status()
    sec = r.json()
    if len(sec) > 0:
        print("Usando secao existente id=", sec[0].get('id'))
        return sec[0].get('id')

    # create default
    payload = {"nome": "Secao Script", "descricao": "Criada pelo script de populacao"}
    r = requests.post(url, json=payload, headers=headers)
    r.raise_for_status()
    sec = r.json()
    print("Secao criada id=", sec.get('id'))
    return sec.get('id')


def get_or_create_estante(base, token):
    url = f"{base}/estanteprateleira"
    headers = auth_headers(token)
    r = requests.get(url, headers=headers)
    r.raise_for_status()
    est = r.json()
    if len(est) > 0:
        print("Usando estanteprateleira existente id=", est[0].get('id'))
        return est[0].get('id')

    payload = {"estante": "A", "prateleira": 1}
    r = requests.post(url, json=payload, headers=headers)
    r.raise_for_status()
    created = r.json()
    print("Estante/Prateleira criada id=", created.get('id'))
    return created.get('id')


def create_book(base, token, titulo, isbn, qtd_exemplares, secao_id, estante_id):
    url = f"{base}/livros"
    headers = auth_headers(token)
    payload = {
        "titulo": titulo,
        "isbn": isbn,
        "ativo": True,
        "qtdExemplares": qtd_exemplares,
        "autores": [{"nome": "Autor Script"}],
        "generos": [{"genero": "Geral"}],
        "idSecao": secao_id,
        "idEstanteprateleira": estante_id
    }
    r = requests.post(url, json=payload, headers=headers)
    r.raise_for_status()
    livro = r.json()
    print(f"Livro criado id={livro.get('id')} titulo={titulo}")
    # create exemplares
    url_ex = f"{base}/livros/exemplares"
    payload_ex = {"idLivro": livro.get('id'), "idSecao": secao_id, "qtdExemplares": qtd_exemplares, "idEstanteprateleira": estante_id}
    r2 = requests.post(url_ex, json=payload_ex, headers=headers)
    r2.raise_for_status()
    exemplares = r2.json()
    print(f"Exemplares criados for livro id={livro.get('id')}: count={len(exemplares)}")
    return livro.get('id')


def create_student(base, token, nome, email, id_turma=None):
    url = f"{base}/alunos"
    headers = auth_headers(token)
    payload = {"nome": nome, "email": email}
    if id_turma is not None:
        payload["idTurma"] = id_turma
    r = requests.post(url, json=payload, headers=headers)
    r.raise_for_status()
    aluno = r.json()
    print("Aluno criado id=", aluno.get('id'))
    return aluno.get('id')


def find_available_exemplar_for_book(base, token, idLivro):
    url = f"{base}/livros/exemplares/{idLivro}"
    headers = auth_headers(token)
    r = requests.get(url, headers=headers)
    r.raise_for_status()
    exemplares = r.json()
    for ex in exemplares:
        if ex.get('situacao') == 'disponivel':
            return ex.get('id')
    return None


def create_loan(base, token, idAluno, idExemplar):
    url = f"{base}/emprestimos"
    headers = auth_headers(token)
    payload = {"idAluno": idAluno, "idExemplar": idExemplar, "observacao": "Criado por script"}
    r = requests.post(url, json=payload, headers=headers)
    r.raise_for_status()
    emprest = r.json()
    print("Emprestimo criado id=", emprest.get('id'))
    return emprest.get('id')


def conclude_loan(base, token, idEmprestimo):
    url = f"{base}/emprestimos/concluir/{idEmprestimo}"
    headers = auth_headers(token)
    payload = {"extraviado": False, "observacao": "Concluido por script"}
    r = requests.patch(url, json=payload, headers=headers)
    r.raise_for_status()
    print("Emprestimo concluido id=", idEmprestimo)


def create_frequency(base, token, idAluno, registradaPor, atividade="lendo"):
    url = f"{base}/frequencia-alunos"
    headers = auth_headers(token)
    payload = {"idAluno": idAluno, "registradaPor": registradaPor, "atividade": atividade}
    r = requests.post(url, json=payload, headers=headers)
    r.raise_for_status()
    f = r.json()
    print("Frequencia criada id=", f.get('id'))
    return f.get('id')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--token", required=True)
    parser.add_argument("--base-url", default="http://localhost:8090")
    parser.add_argument("--books", type=int, default=3)
    parser.add_argument("--students", type=int, default=5)
    parser.add_argument("--loans", type=int, default=5)
    parser.add_argument("--freqs", type=int, default=5)
    args = parser.parse_args()

    token = args.token.strip()
    base = args.base_url.rstrip('/')

    try:
        secao_id = get_or_create_secao(base, token)
        est_id = get_or_create_estante(base, token)

        # create books
        livro_ids = []
        for i in range(1, args.books + 1):
            titulo = f"Livro Script {i} {datetime.now(timezone.utc).strftime('%Y%m%d%H%M%S')}"
            # ISBN must be max 13 characters (validation rule). Generate a 13-digit numeric ISBN.
            isbn = f"{random.randint(10**12, 10**13 - 1)}"
            try:
                livro_id = create_book(base, token, titulo, isbn, 1, secao_id, est_id)
                livro_ids.append(livro_id)
            except requests.HTTPError as e:
                print(f"Falha ao criar livro {titulo}: {e}")
                if e.response is not None:
                    try:
                        print(e.response.status_code, e.response.text)
                    except Exception:
                        pass
                print("Pulando este livro e continuando...")
                continue

        # ensure turma exists and create students
        def list_turmas(base, token):
            url = f"{base}/turmas/filtrar"
            headers = auth_headers(token)
            r = requests.get(url, headers=headers)
            r.raise_for_status()
            return r.json()

        def create_turma(base, token, serie=3, turma_name='A', ano=2025, ativo=True):
            url = f"{base}/turmas"
            headers = auth_headers(token)
            payload = {"serie": serie, "turma": turma_name, "anoDeEntrada": ano, "ativo": ativo}
            r = requests.post(url, json=payload, headers=headers)
            r.raise_for_status()
            return r.json()

        def get_or_create_turma(base, token):
            turmas = list_turmas(base, token)
            if len(turmas) > 0:
                print("Usando turma existente id=", turmas[0].get('id'))
                return turmas[0].get('id')
            t = create_turma(base, token)
            print("Turma criada id=", t.get('id'))
            return t.get('id')

        turma_id = get_or_create_turma(base, token)

        student_ids = []
        for i in range(1, args.students + 1):
            nome = f"Aluno Script {i}"
            email = f"aluno.script.{i}.{random.randint(1000,9999)}@local"
            try:
                sid = create_student(base, token, nome, email, id_turma=turma_id)
                student_ids.append(sid)
            except requests.HTTPError as e:
                print(f"Falha ao criar aluno {nome}: {e}")
                if e.response is not None:
                    try:
                        print(e.response.status_code, e.response.text)
                    except Exception:
                        pass
                print("Pulando este aluno e continuando...")
                continue

        # create loans and conclude them
        loan_ids = []
        loan_count = min(args.loans, len(student_ids), len(livro_ids))
        for i in range(loan_count):
            aluno = student_ids[i]
            livro = livro_ids[i % len(livro_ids)]
            exemplar = find_available_exemplar_for_book(base, token, livro)
            if not exemplar:
                print(f"Nenhum exemplar disponivel para livro {livro}; pulando...")
                continue
            emp_id = create_loan(base, token, aluno, exemplar)
            loan_ids.append(emp_id)
            # conclude immediately
            conclude_loan(base, token, emp_id)

        # create frequency entries (registradaPor default 1)
        for i in range(args.freqs):
            aluno = student_ids[i % len(student_ids)]
            create_frequency(base, token, aluno, 1, atividade=random.choice(['lendo','outros','estudo_individual']))

        print("População concluída. Resumo:")
        print(f" livros={len(livro_ids)}, alunos={len(student_ids)}, emprestimos={len(loan_ids)}, frequencias={args.freqs}")

    except requests.HTTPError as e:
        print("Erro HTTP:", e)
        if e.response is not None:
            try:
                print(e.response.status_code, e.response.text)
            except Exception:
                pass
        sys.exit(2)
    except Exception as e:
        print("Erro:", e)
        sys.exit(3)


if __name__ == '__main__':
    main()
