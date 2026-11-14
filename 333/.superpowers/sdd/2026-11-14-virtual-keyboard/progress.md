# SDD ledger — plan: docs/superpowers/plans/2026-11-14-virtual-keyboard.md

Setup: Ruling: Git worktree and SDD helper scripts are unavailable because the bundle has an empty `.git` directory, not a repository — isolate all new work under `patches/freej2me/` and defer replacement of the runtime JAR until verified installation — cost if wrong: edits are isolated by path and backup rather than by Git history.

Setup: Ruling: GitHub is unreachable from the execution sandbox, so a full upstream checkout cannot be vendored — maintain source-level replacements for only the exact baseline classes, compiled against and overlaid onto a checksum-pinned copy of the JAR; do not patch bytecode — cost if wrong: unobserved private behavior in replaced classes could differ, mitigated by public API comparison, characterization tests, and preservation of the original JAR.

Pre-flight: Task 1 produces `VirtualKeyboardModel`; Tasks 2-4 consume the same constructor, enums, getters, and activation result — interfaces match.

Pre-flight: Task 2 produces `VirtualKeyboardView.render` and `keyBounds`; Tasks 3-4 consume `render`, while bounds remain test-only — interfaces match.

Pre-flight: Tasks 3-4 produce source-compatible `Form` and `TextBox` overlays; Task 5 packages those compiled classes without changing their interface — interfaces match.

Task 1: Ruling: initial verification exposed non-reproducible ZIP entry timestamps — pin overlay entry timestamps with `jar --date` and retain a two-build hash regression test — cost if wrong: identical sources could produce unverifiable release hashes.

Task 1: complete (no Git range; tests: model PASS 17/17 and reproducible-build PASS; build exit 0; base `9a947c5...`, artifact `2f4c780...`).

Task 2: Ruling: desktop renderer tests initially attempted X11 because the test runner omitted the launcher's headless JVM flag — run all tests with `-Djava.awt.headless=true`, matching `Emus/JAVA/launch.sh` — cost if wrong: host-only AWT failures would hide renderer behavior.

Task 2: complete (no Git range; tests: model PASS 17/17, view PASS 7/7 across 128x128 through 640x360, reproducible-build PASS; JAR contains model/view overlays).

Task 3: Ruling: raw `javap` diff includes only a new `Compiled from` metadata line because local `javac` emits SourceFile information — normalize that non-API line before comparing public members — cost if wrong: a genuine public signature change could be overlooked, mitigated by diffing every remaining line.

Task 3: complete (no Git range; tests: model PASS 17/17, view PASS 7/7, Form PASS 7/7; normalized public API diff empty).

Task 4: Ruling: declared-method diff shows `TextBox.keyPressed(int)` and `notifySetCurrent()` because the patch overrides two existing public `Displayable` methods — treat inherited signatures as the effective API, with no removal or signature change — cost if wrong: reflection that inspects only declared methods will see two additional overrides, while normal Java callers remain binary-compatible.

Task 4: complete (no Git range; tests: model PASS 17/17, view PASS 7/7, Form PASS 7/7, TextBox PASS 9/9; reproducible-build PASS).

Task 5: Ruling: JShell's default JDI engine and default preferences path are blocked by the sandbox — use `--execution local` with temporary preferences for the smoke class-load verification — cost if wrong: host validation could fail for sandbox reasons rather than linkage; the local engine loaded the class without `VerifyError` or `NoSuchMethodError`.

Task 5: complete (no Git range; pre/post-install suite PASS 40 behavior tests + packaging, reproducible-build PASS, `-Xverify:all` PASS, smoke class load PASS, unzip PASS; installed `2622f5e...`, backup `9a947c5...`, SDL unchanged `1b3cffb...`).

Final review: self-review (Native execution; subagent review not authorized by current delegation rules).

Final: fixed stale compiled classes entering later builds — `test-clean-build.sh` RED→GREEN, final suite green.

Final: fixed `--installed` behavioral tests loading `build/classes` instead of the installed JAR — `RuntimeOriginTest` RED→GREEN, installed suite green.

Final: fixed installer regression fixture selecting the patched active JAR after installation — `test-install.sh` RED→GREEN both before/after installed state.

Final: minor (deferred): alternate legacy `phone:n/s/m` mappings contain ambiguous negative key codes before a TextBox receives its first OK; default packaged `phone:p` D-pad/OK mapping is fully covered.

Final: minor (deferred): long Form labels/values can visually overlap at the narrowest resolution, though the full-screen keyboard remains bounded and usable.

Final: minor (deferred): a closed TextBox draws long committed text on one line rather than wrapping it.

Final: Ruling: no branch integration menu or SDD workspace deletion is possible because this bundle has no Git repository; preserve the ledger as the only durable execution record and leave the installed artifact in place — cost if wrong: there is no commit-level rollback, mitigated by the checksum-pinned `.pre-virtual-keyboard.bak`.
