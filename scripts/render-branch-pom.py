#!/usr/bin/env python3
from __future__ import annotations

import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
POM = ROOT / "pom.xml"

MATRIX = {
    "2.3.x": {"parent": "2.3.12.RELEASE", "java": "1.8", "starter": "2.3.x.20260630-SNAPSHOT", "ext": "1.0.x.20260630-SNAPSHOT", "metrics": "1.0.x.20260630-SNAPSHOT"},
    "2.4.x": {"parent": "2.4.13", "java": "1.8", "starter": "2.4.x.20260630-SNAPSHOT", "ext": "1.0.x.20260630-SNAPSHOT", "metrics": "1.0.x.20260630-SNAPSHOT"},
    "2.5.x": {"parent": "2.5.15", "java": "1.8", "starter": "2.5.x.20260630-SNAPSHOT", "ext": "1.0.x.20260630-SNAPSHOT", "metrics": "1.0.x.20260630-SNAPSHOT"},
    "2.6.x": {"parent": "2.6.15", "java": "1.8", "starter": "2.6.x.20260630-SNAPSHOT", "ext": "1.0.x.20260630-SNAPSHOT", "metrics": "1.0.x.20260630-SNAPSHOT"},
    "2.7.x": {"parent": "2.7.18", "java": "1.8", "starter": "2.7.x.20260630-SNAPSHOT", "ext": "1.0.x.20260630-SNAPSHOT", "metrics": "1.0.x.20260630-SNAPSHOT"},
    "3.0.x": {"parent": "3.0.13", "java": "17", "starter": "3.0.x.20260630-SNAPSHOT", "ext": "2.0.x.20260630-SNAPSHOT", "metrics": "2.0.x.20260630-SNAPSHOT"},
    "3.1.x": {"parent": "3.1.12", "java": "17", "starter": "3.1.x.20260630-SNAPSHOT", "ext": "2.0.x.20260630-SNAPSHOT", "metrics": "2.0.x.20260630-SNAPSHOT"},
    "3.2.x": {"parent": "3.2.12", "java": "17", "starter": "3.2.x.20260630-SNAPSHOT", "ext": "2.0.x.20260630-SNAPSHOT", "metrics": "2.0.x.20260630-SNAPSHOT"},
    "3.3.x": {"parent": "3.3.13", "java": "17", "starter": "3.3.x.20260630-SNAPSHOT", "ext": "2.0.x.20260630-SNAPSHOT", "metrics": "2.0.x.20260630-SNAPSHOT"},
    "3.4.x": {"parent": "3.4.13", "java": "17", "starter": "3.4.x.20260630-SNAPSHOT", "ext": "2.0.x.20260630-SNAPSHOT", "metrics": "2.0.x.20260630-SNAPSHOT"},
    "3.5.x": {"parent": "3.5.16", "java": "17", "starter": "3.5.x.20260630-SNAPSHOT", "ext": "2.0.x.20260630-SNAPSHOT", "metrics": "2.0.x.20260630-SNAPSHOT"},
    "4.0.x": {"parent": "4.0.7", "java": "21", "starter": "4.0.x.20260630-SNAPSHOT", "ext": "3.0.x.20260630-SNAPSHOT", "metrics": "3.0.x.20260630-SNAPSHOT"},
    "4.1.x": {"parent": "4.1.0", "java": "21", "starter": "4.1.x.20260630-SNAPSHOT", "ext": "3.0.x.20260630-SNAPSHOT", "metrics": "3.0.x.20260630-SNAPSHOT"},
}


def replace_once(text: str, pattern: str, repl: str) -> str:
    return re.sub(pattern, repl, text, count=1, flags=re.MULTILINE)


def main() -> int:
    if len(sys.argv) != 2 or sys.argv[1] not in MATRIX:
        print("usage: python3 scripts/render-branch-pom.py <2.3.x|...|4.1.x>")
        return 1
    cfg = MATRIX[sys.argv[1]]
    text = POM.read_text(encoding="utf-8")
    text = replace_once(text, r"(<artifactId>spring-boot-starter-parent</artifactId>\s*<version>).*?(</version>)", rf"\g<1>{cfg['parent']}\2")
    text = replace_once(text, r"(<artifactId>okhttp3-spring-boot-starter</artifactId>\s*<description>.*?</description>\s*<version>).*?(</version>)", rf"\g<1>{cfg['starter']}\2")
    text = replace_once(text, r"<java\.version>.*?</java\.version>", f"<java.version>{cfg['java']}</java.version>")
    text = replace_once(text, r"<maven\.compiler\.source>.*?</maven\.compiler\.source>", f"<maven.compiler.source>${{java.version}}</maven.compiler.source>")
    text = replace_once(text, r"<maven\.compiler\.target>.*?</maven\.compiler\.target>", f"<maven.compiler.target>${{java.version}}</maven.compiler.target>")
    text = replace_once(text, r"<okhttp-extension\.version>.*?</okhttp-extension\.version>", f"<okhttp-extension.version>{cfg['ext']}</okhttp-extension.version>")
    text = replace_once(text, r"<okhttp-metrics\.version>.*?</okhttp-metrics\.version>", f"<okhttp-metrics.version>{cfg['metrics']}</okhttp-metrics.version>")
    POM.write_text(text, encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
