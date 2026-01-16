#!/usr/bin/env python3
"""
Simple Java metrics collector (heuristic):
- LOC: non-blank non-comment lines
- CC (cyclomatic): 1 + count of decision keywords per method
- LCOM (Henderson-Sellers/LCOM1 variant): P-Q if P>Q else 0 (P: method pairs no shared fields, Q: method pairs share fields)
- CBO: number of unique external type names referenced (heuristic: imports + types in fields/method signatures)

Usage: python scripts/metrics_java.py --src src/main/java --out reports/metrics_report.md
"""
import re
import sys
import json
from pathlib import Path
from collections import defaultdict
import argparse

DECISION_KEYWORDS = [r"\bif\b", r"\bfor\b", r"\bwhile\b", r"\bcase\b", r"\bcatch\b", r"\belse if\b", r"\?", r"\breturn\b(?=.*if)|\bthrow\b"]
EXTRA_DECISIONS = [r"&&", r"\|\|"]

PRIMITIVES = set(['int','long','short','byte','boolean','char','float','double','void'])

pkg_re = re.compile(r'^\s*package\s+([\w\.]+)\s*;')
import_re = re.compile(r'^\s*import\s+([\w\.\*]+)\s*;')
class_re = re.compile(r'\b(class|interface|enum)\s+(\w+)')
field_re = re.compile(r'^(?:\s*(public|protected|private|static|final|transient|volatile)\s+)+\s*([\w<>, \[\]\.]+)\s+(\w+)\s*(=|;).*')
method_sig_re = re.compile(r'^(?:\s*(public|protected|private|static|final|synchronized|native|abstract)\s+)*\s*([\w<>, \[\]\.]+)\s+(\w+)\s*\(([^)]*)\)\s*(throws[^{]*)?\{?')


def remove_comments(code: str) -> str:
    # remove /* */ and // comments
    code = re.sub(r'/\*.*?\*/', '', code, flags=re.S)
    code = re.sub(r'//.*', '', code)
    return code


def loc_count(code: str) -> int:
    code = remove_comments(code)
    lines = [l for l in code.splitlines() if l.strip() != '']
    return len(lines)


def extract_imports(code: str):
    imports = set()
    for line in code.splitlines():
        m = import_re.match(line)
        if m:
            imports.add(m.group(1).strip())
    return imports


def extract_package(code: str):
    for line in code.splitlines():
        m = pkg_re.match(line)
        if m:
            return m.group(1)
    return None


def split_into_top_level_types(code: str):
    # naive: find each top-level class/interface/enum and its body by counting braces
    results = []
    tokens = re.finditer(r'\b(class|interface|enum)\b', code)
    for t in tokens:
        start = t.start()
        m = class_re.search(code, pos=max(0, start-50))
        if not m:
            continue
        name = m.group(2)
        # find opening brace after this
        brace_pos = code.find('{', m.end())
        if brace_pos == -1:
            continue
        # scan to matching brace
        depth = 0
        i = brace_pos
        end = None
        while i < len(code):
            c = code[i]
            if c == '{':
                depth += 1
            elif c == '}':
                depth -= 1
                if depth == 0:
                    end = i
                    break
            i += 1
        if end:
            body = code[m.start():end+1]
            results.append((name, body))
    return results


def extract_fields(class_body: str):
    fields = {}
    for line in class_body.splitlines():
        line = line.strip()
        if line.startswith('@'):
            continue
        m = field_re.match(line)
        if m:
            type_str = m.group(2).strip()
            name = m.group(3).strip()
            fields[name] = type_str
    return fields


def extract_methods(class_body: str):
    methods = {}
    lines = class_body.splitlines()
    i = 0
    while i < len(lines):
        line = lines[i]
        # merge lines until we see a '{'
        sig_lines = line
        j = i
        while '{' not in lines[j] and j+1 < len(lines):
            j += 1
            sig_lines += ' ' + lines[j]
        m = method_sig_re.search(sig_lines)
        if m:
            method_name = m.group(3)
            # capture body by counting braces
            # find first { after the sig
            rest = '\n'.join(lines[j:])
            idx = rest.find('{')
            if idx == -1:
                i = j+1
                continue
            k = idx
            depth = 0
            end_idx = None
            while k < len(rest):
                if rest[k] == '{':
                    depth += 1
                elif rest[k] == '}':
                    depth -= 1
                    if depth == 0:
                        end_idx = k
                        break
                k += 1
            if end_idx is not None:
                body = rest[idx:end_idx+1]
                methods[method_name + '@' + str(i)] = body
                # advance i by the number of lines consumed
                consumed = rest[:end_idx+1].count('\n')
                i = j + consumed
            else:
                i = j + 1
        else:
            i += 1
    return methods


def method_cyclomatic_complexity(body: str) -> int:
    count = 0
    for kw in DECISION_KEYWORDS:
        count += len(re.findall(kw, body))
    for kw in EXTRA_DECISIONS:
        count += body.count(kw)
    return max(1, 1 + count)


def analyze_file(path: Path):
    text = path.read_text(encoding='utf-8')
    pkg = extract_package(text)
    imports = extract_imports(text)
    code_no_comments = remove_comments(text)
    loc = loc_count(text)
    types = split_into_top_level_types(code_no_comments)
    class_results = []
    for name, body in types:
        fields = extract_fields(body)
        methods = extract_methods(body)
        method_field_usage = {}
        cc_list = []
        for mname, mbody in methods.items():
            # find which fields are referenced
            used = set()
            for fname in fields.keys():
                if re.search(r"\b" + re.escape(fname) + r"\b", mbody):
                    used.add(fname)
            method_field_usage[mname] = used
            cc = method_cyclomatic_complexity(mbody)
            cc_list.append(cc)
        # LCOM (P,Q)
        mnames = list(method_field_usage.keys())
        P = 0
        Q = 0
        for i in range(len(mnames)):
            for j in range(i+1, len(mnames)):
                if method_field_usage[mnames[i]].isdisjoint(method_field_usage[mnames[j]]):
                    P += 1
                else:
                    Q += 1
        lcom = P - Q if P > Q else 0
        # CBO: heuristic - count unique imported class names (exclude java.* and wildcard)
        deps = set()
        for im in imports:
            if im.startswith('java.'):
                continue
            # take last token
            if im.endswith('.*'):
                deps.add(im[:-2])
            else:
                deps.add(im.split('.')[-1])
        # Also add types from field type strings
        for t in fields.values():
            # split generics and arrays
            toks = re.findall(r"[A-Z][A-Za-z0-9_]+", t)
            for tk in toks:
                if tk not in PRIMITIVES:
                    deps.add(tk)
        # basic measure
        cbo = len(deps)
        class_results.append({
            'class': name,
            'package': pkg,
            'loc': loc,
            'fields': len(fields),
            'methods': len(methods),
            'cbo': cbo,
            'lcom': lcom,
            'avg_cc': (sum(cc_list)/len(cc_list)) if cc_list else 0,
            'max_cc': max(cc_list) if cc_list else 0,
            'method_ccs': cc_list
        })
    return class_results


def analyze_tree(src_root: Path):
    all_results = []
    for path in src_root.rglob('*.java'):
        try:
            res = analyze_file(path)
            if res:
                # attach file path
                for r in res:
                    r['file'] = str(path.relative_to(Path.cwd()))
                all_results.extend(res)
        except Exception as e:
            print(f"Error analyzing {path}: {e}", file=sys.stderr)
    return all_results


def aggregate(results):
    total_loc = sum(r['loc'] for r in results)
    total_classes = len(results)
    avg_cbo = sum(r['cbo'] for r in results)/total_classes if total_classes else 0
    avg_lcom = sum(r['lcom'] for r in results)/total_classes if total_classes else 0
    avg_cc = sum(r['avg_cc'] for r in results)/total_classes if total_classes else 0
    max_cc = max((r['max_cc'] for r in results), default=0)
    return {
        'total_classes': total_classes,
        'total_loc': total_loc,
        'avg_cbo': avg_cbo,
        'avg_lcom': avg_lcom,
        'avg_cc': avg_cc,
        'max_cc': max_cc
    }


def to_markdown(results, agg):
    lines = []
    lines.append('# Metrics Report')
    lines.append('')
    lines.append('## Summary')
    lines.append('')
    lines.append(f'- Total classes: {agg["total_classes"]}')
    lines.append(f'- Total LOC: {agg["total_loc"]}')
    lines.append(f'- Avg CBO: {agg["avg_cbo"]:.2f}')
    lines.append(f'- Avg LCOM: {agg["avg_lcom"]:.2f}')
    lines.append(f'- Avg CC: {agg["avg_cc"]:.2f}')
    lines.append(f'- Max CC: {agg["max_cc"]}')
    lines.append('')
    lines.append('## Per-class metrics')
    lines.append('')
    lines.append('| File | Class | LOC | Fields | Methods | CBO | LCOM | Avg CC | Max CC |')
    lines.append('|---|---:|---:|---:|---:|---:|---:|---:|---:|')
    for r in sorted(results, key=lambda x: x['file']):
        lines.append(f"| `{r['file']}` | {r['class']} | {r['loc']} | {r['fields']} | {r['methods']} | {r['cbo']} | {r['lcom']} | {r['avg_cc']:.2f} | {r['max_cc']} |")
    return '\n'.join(lines)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--src', default='src/main/java')
    parser.add_argument('--out', default='reports/metrics_report.md')
    parser.add_argument('--focus', nargs='*', help='Optional list of file paths (relative) to focus on')
    args = parser.parse_args()

    src = Path(args.src)
    if not src.exists():
        print(f"Source folder {src} not found", file=sys.stderr)
        sys.exit(1)
    results = analyze_tree(src)
    agg = aggregate(results)
    # write full report
    outp = Path(args.out)
    outp.parent.mkdir(parents=True, exist_ok=True)
    md = to_markdown(results, agg)
    outp.write_text(md, encoding='utf-8')
    print(f"Wrote report to {outp}")

    # If focus files provided, print a small table for them
    if args.focus:
        focus = set(args.focus)
        focused = [r for r in results if r['file'] in focus]
        if not focused:
            print('No focused files found in analysis.')
        else:
            print('\nFocused files metrics:')
            for r in focused:
                print(json.dumps(r, indent=2))

if __name__ == '__main__':
    main()
