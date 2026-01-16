#!/usr/bin/env python3
"""
Script para testar fluxo de ocorrências + export PDF.

Fluxo:
 1. Cria usuário com cargo 'aluno_monitor'.
 2. Faz login e exibe o usuário criado.
 3. Aguarda (poll) até o campo `cargo` do usuário ser alterado para 'bibliotecario' (ou até você apertar Enter).
 4. Após detectar promoção, faz login novamente, cria um aluno, registra uma ocorrência e solicita /ocorrencias/export/pdf (salva arquivo localmente).

Uso:
  python scripts/occ_export_test.py --base-url http://localhost:8090

Opções importantes:
  --no-poll    : não poll; aguarda por confirmação manual (pressione Enter quando já tiver editado o banco)
  --timeout    : timeout em segundos para o polling (default 600)
  --interval   : intervalo de polling em segundos (default 5)

"""

import argparse
import time
import sys
import requests
from datetime import datetime


def create_user(base, nome, cargo, email, senha):
    url = f"{base}/usuarios"
    payload = {"nome": nome, "cargo": cargo, "email": email, "senha": senha}
    r = requests.post(url, json=payload)
    r.raise_for_status()
    return r.json()


def login(base, email, senha):
    url = f"{base}/usuarios/login"
    r = requests.post(url, json={"email": email, "senha": senha})
    r.raise_for_status()
    return r.json().get("token")


def get_user(base, user_id, token):
    url = f"{base}/usuarios/{user_id}"
    headers = {"Authorization": f"Bearer {token}"}
    r = requests.get(url, headers=headers)
    r.raise_for_status()
    return r.json()


def create_student(base, token, nome, email):
    url = f"{base}/alunos"
    headers = {"Authorization": f"Bearer {token}"}
    payload = {"nome": nome, "email": email}
    r = requests.post(url, json=payload, headers=headers)
    r.raise_for_status()
    return r.json()


def register_occurrence(base, token, id_aluno, registradaPor, detalhes):
    url = f"{base}/ocorrencias"
    headers = {"Authorization": f"Bearer {token}"}
    payload = {"idAluno": id_aluno, "registradaPor": registradaPor, "detalhes": detalhes}
    r = requests.post(url, json=payload, headers=headers)
    r.raise_for_status()
    return r.json()


def export_ocorrencias_pdf(base, token, out_file):
    url = f"{base}/ocorrencias/export/pdf"
    headers = {"Authorization": f"Bearer {token}", "Accept": "application/pdf"}
    r = requests.get(url, headers=headers, stream=True)
    r.raise_for_status()
    with open(out_file, "wb") as f:
        for chunk in r.iter_content(chunk_size=8192):
            f.write(chunk)
    return out_file


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://localhost:8090", help="Base URL do backend")
    parser.add_argument("--no-poll", action="store_true", help="Não realizar polling; esperar confirmação manual (Enter) para continuar)")
    parser.add_argument("--timeout", type=int, default=600, help="Timeout do polling em segundos (default 600)")
    parser.add_argument("--interval", type=int, default=5, help="Intervalo entre polls em segundos (default 5)")
    args = parser.parse_args()

    base = args.base_url.rstrip('/')

    ts = datetime.utcnow().strftime("%Y%m%d%H%M%S")
    nome = f"Teste Export {ts}"
    email = f"teste.export.{ts}@local"
    senha = "senha123"
    cargo = "aluno_monitor"

    print(f"Criando usuário '{nome}' com cargo '{cargo}' em {base} ...")
    try:
        user = create_user(base, nome, cargo, email, senha)
    except requests.HTTPError as e:
        print("Falha ao criar usuário:", e, file=sys.stderr)
        if e.response is not None:
            print(e.response.text, file=sys.stderr)
        sys.exit(1)

    user_id = user.get("id")
    print("Usuário criado:")
    print(user)
    print("")

    print("Fazendo login com o usuário criado (para poder consultar dados autenticados)...")
    try:
        token = login(base, email, senha)
    except requests.HTTPError as e:
        print("Falha ao fazer login:", e, file=sys.stderr)
        if e.response is not None:
            print(e.response.text, file=sys.stderr)
        sys.exit(2)

    print("Login OK. Token recebido (primeiros 12 chars):", token[:12])
    print("")

    print("Próximo passo: altere o cargo do usuário no banco para 'bibliotecario' (por exemplo via SQL UPDATE no DB).")
    print(f"User id = {user_id}. Email = {email}")
    print("")

    if args.no_poll:
        input("Quando terminar de alterar o banco para bibliotecario, pressione Enter para continuar...\n")
    else:
        print(f"Iniciando polling (timeout={args.timeout}s, interval={args.interval}s) para detectar cargo='bibliotecario'...\n")
        start = time.time()
        while True:
            try:
                info = get_user(base, user_id, token)
                cargo_atual = info.get('cargo')
                print(f"Cargo atual: {cargo_atual}")
                if cargo_atual == 'bibliotecario':
                    print("Detectado cargo bibliotecario. Continuando...")
                    break
            except requests.HTTPError as e:
                print("Erro ao consultar usuário (continuarei tentando):", e)
            if time.time() - start > args.timeout:
                print("Timeout atingido no polling. Saindo com erro.")
                sys.exit(10)
            time.sleep(args.interval)

    # Re-login to get token with new cargo claims
    print("Efetuando novo login para obter token atualizado...")
    try:
        token = login(base, email, senha)
    except requests.HTTPError as e:
        print("Falha ao fazer login após promoção:", e, file=sys.stderr)
        if e.response is not None:
            print(e.response.text, file=sys.stderr)
        sys.exit(3)
    print("Token novo recebido (primeiros 12 chars):", token[:12])

    # Create student
    print("Criando aluno de teste...")
    try:
        aluno = create_student(base, token, f"Aluno Teste {ts}", f"aluno.{ts}@local")
    except requests.HTTPError as e:
        print("Falha ao criar aluno:", e, file=sys.stderr)
        if e.response is not None:
            print(e.response.text, file=sys.stderr)
        sys.exit(4)
    print("Aluno criado:", aluno)

    # Register occurrence
    print("Registrando ocorrência...")
    detalhes = f"Ocorrencia de teste automatizada - {ts}"
    try:
        occ = register_occurrence(base, token, aluno.get('id'), user_id, detalhes)
    except requests.HTTPError as e:
        print("Falha ao registrar ocorrência:", e, file=sys.stderr)
        if e.response is not None:
            print(e.response.text, file=sys.stderr)
        sys.exit(5)
    print("Ocorrência registrada:", occ)

    # Export PDF
    out_file = f"out_ocorrencias_test_{ts}.pdf"
    print("Solicitando exportação de PDF e salvando em:", out_file)
    try:
        export_ocorrencias_pdf(base, token, out_file)
    except requests.HTTPError as e:
        print("Falha ao exportar PDF:", e, file=sys.stderr)
        if e.response is not None:
            print(e.response.text, file=sys.stderr)
        sys.exit(6)

    print("Exportação concluída. Arquivo salvo:", out_file)
    print("Teste completo. Verifique o PDF gerado e informe se deseja que eu faça mais validações.")


if __name__ == '__main__':
    main()
