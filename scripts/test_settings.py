"""Run isolated settings regression tests without Maven or external libraries."""
import os
from pathlib import Path
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
JAVA = os.environ.get("JAVA", "java")
JAVAC = shutil.which("javac")
compiler = [JAVAC] if JAVAC else [JAVA, "-m", "jdk.compiler/com.sun.tools.javac.Main"]
scenarios = ["defaults", "hex", "rgb", *[f"bad-file-{i}" for i in range(5)],
             "invalid-write", "null-color", "reload-invalid", "event", "plain-setting"]

with tempfile.TemporaryDirectory(prefix="settings-tests-") as temporary:
    root = Path(temporary)
    classes = root / "classes"
    classes.mkdir()
    subprocess.run(compiler + ["-d", str(classes),
                   str(ROOT / "src/main/java/com/crfmanagement/settings/SettingsManager.java"),
                   str(ROOT / "tests/SettingsManagerRegressionTest.java")], check=True)
    failed = 0
    for scenario in scenarios:
        work = root / scenario
        work.mkdir()
        result = subprocess.run([JAVA, "-Djava.awt.headless=true", "-cp", str(classes),
                                 "SettingsManagerRegressionTest", scenario], cwd=work)
        failed += result.returncode != 0
    print(f"{len(scenarios) - failed}/{len(scenarios)} passed; {failed} failed")
    raise SystemExit(bool(failed))
