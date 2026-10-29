package data.device.api

/**
 * Real iOS toolchain discovery output captured on this host on 2026-10-19, committed
 * **verbatim** -- byte-for-byte what each command wrote to stdout, trailing spaces included.
 *
 * Inline literals, not `src/test/resources`: no resources directory exists anywhere in
 * this repo and loading one across Android/iOS/desktop would need wiring this spec does not
 * budget for. Same convention as [UiAutomatorDumpFixtures][VIEW_BASED_SETTINGS_DUMP].
 *
 * No escaping was needed -- neither payload contains a `${'$'}`, a triple quote, a backslash
 * or a CR, so each raw string is byte-faithful as written. Verify that claim before
 * hand-editing anything here; the point of these fixtures is that they are NOT hand-written.
 *
 * Host: Xcode 26.3, four installed runtimes -- iOS 17.5 (21F79), 18.1 (22B81), 18.5 (22F77)
 * and 26.3 (23D8133). `idb` at `~/.local/bin/idb` (it has no `--version` flag -- it answers
 * with its usage block), `idb_companion` at `/opt/homebrew/bin/idb_companion`, which reports
 * `{"build_time":"18:47:58","build_date":"Oct 8 2026"}`. NO simulator was booted, which is
 * what makes these two payloads the measured half of the simulator-not-booted reproduction.
 *
 * Three quirks a parser must survive, all present below and none of them invented:
 * 1. The SAME NAME appears under MORE THAN ONE RUNTIME -- `iPhone 16` exists under iOS 18.1
 *    AND iOS 18.5, `iPad Pro 11-inch (M4)` under all four. A name is therefore not a key.
 *    (The plan predicted the `iPhone 16` duplicate across 17.5/18.1; the real host has it
 *    across 18.1/18.5, because 17.5 predates the iPhone 16. The SHAPE holds, the runtimes
 *    differ -- see [SIMCTL_LIST_DEVICES_AVAILABLE].)
 * 2. The same name appears TWICE UNDER ONE RUNTIME with two different UDIDs -- iOS 17.5 has
 *    two `iPhone 15 Pro`, two `iPhone 15`, two `iPad Air 11-inch (M2)` and more. Only the
 *    UDID identifies a simulator.
 * 3. Device names contain parentheses -- `iPhone SE (3rd generation)`, `iPad mini (A17 Pro)`.
 *    A parser that takes the LAST parenthesised group as the state and the one before it as
 *    the UDID works; one that takes the FIRST does not.
 */

/**
 * Raw stdout of `xcrun simctl list devices available`.
 *
 * Sections are `-- <runtime> --` headers; every device line is indented by four spaces and
 * ends with a state in parentheses followed by ONE TRAILING SPACE -- `(Shutdown) `. No line
 * says `(Booted)` and no `-- Unavailable: ... --` section is present: `available` filters
 * unavailable runtimes out, and this host has none to begin with.
 */
internal const val SIMCTL_LIST_DEVICES_AVAILABLE: String = """== Devices ==
-- iOS 17.5 --
    iPhone 15 Pro (77C17238-5B51-4206-9A58-CA601AF51E7E) (Shutdown) 
    iPhone 15 Pro (4210645D-322E-43DB-B6FB-7E4F388D1DF5) (Shutdown) 
    iPhone 15 Pro Max (98DBDA97-34DE-4652-933B-E3A348D40687) (Shutdown) 
    iPhone 15 Pro Max (D89E58B3-0B21-41CC-A9DF-F6E38DA22DCA) (Shutdown) 
    iPhone 15 (5CFB518E-2D13-4A76-ADA6-8234DF234EF7) (Shutdown) 
    iPhone 15 (68D2823C-F382-4C4E-877D-0A29F56FD47A) (Shutdown) 
    iPhone 15 Plus (C4BD6AC6-9F71-402D-BC15-E7377253B053) (Shutdown) 
    iPhone 15 Plus (98EAC1B6-882F-48CE-9E33-F6C5E9BB0FE0) (Shutdown) 
    iPhone SE (3rd generation) (27E8D85C-C23A-4590-8AB7-2D9507A2F35C) (Shutdown) 
    iPhone SE (3rd generation) (FFE14503-792C-4500-96B2-623DF571FF09) (Shutdown) 
    iPad Pro 11-inch (M4) (865DB6BF-801B-4EFC-A11F-721C9770DB66) (Shutdown) 
    iPad Pro 11-inch (M4) (ADBAE3FE-92BA-4C1D-91DE-EFA5AEB787C1) (Shutdown) 
    iPad Pro 13-inch (M4) (B0B5D60B-8C4E-47D3-96CC-CA7CF0308753) (Shutdown) 
    iPad Pro 13-inch (M4) (9567B6A9-047A-4587-A10F-525D561601E9) (Shutdown) 
    iPad Air 11-inch (M2) (6F1A7E2D-2CDE-4FBE-88E7-34C9328F20C4) (Shutdown) 
    iPad Air 11-inch (M2) (AAE4BCC3-4783-4438-9CA7-71667BADBBE5) (Shutdown) 
    iPad Air 13-inch (M2) (FF94CD04-F9D9-435A-9D3B-9F08D336D2B5) (Shutdown) 
    iPad Air 13-inch (M2) (CAC20292-3055-4B3B-92EE-BECDDADB7EC1) (Shutdown) 
    iPad (10th generation) (209E0106-A82E-4C84-9058-59DEBD20B037) (Shutdown) 
    iPad (10th generation) (0CE8BC7C-8E6B-4FA8-A9D1-C5C61AACC946) (Shutdown) 
    iPad mini (6th generation) (6F20E331-1692-4938-B491-92C6C2B8DADD) (Shutdown) 
-- iOS 18.1 --
    iPhone 16 Pro (08F37FE6-AC58-4976-AE2B-4B7A980F8D7F) (Shutdown) 
    iPhone 16 Pro Max (6B5F435E-AF11-4C42-B2E3-8FFD24D1AF64) (Shutdown) 
    iPhone 16 (5F20FFD2-3B90-42DA-9227-26725B78EA58) (Shutdown) 
    iPhone 16 Plus (6D100525-3C2F-4989-8EF4-E1BCCF346DBB) (Shutdown) 
    iPhone SE (3rd generation) (3DA2880B-CEF0-4FF2-912A-9D29F5F5667D) (Shutdown) 
    iPad Pro 11-inch (M4) (FA8351AE-8033-495C-BA87-9B072B9141EA) (Shutdown) 
    iPad Pro 13-inch (M4) (3227FEE7-9089-41CA-83E7-28223362C155) (Shutdown) 
    iPad Air 11-inch (M2) (9F4C90A5-8A71-4EE5-9E0C-4C917E1F46B5) (Shutdown) 
    iPad Air 13-inch (M2) (D32A6923-6194-4A6A-951E-33EAC1E84F37) (Shutdown) 
    iPad mini (A17 Pro) (6DC67602-F428-4731-AED2-5B2936EA5715) (Shutdown) 
    iPad (10th generation) (F58C10ED-FCD1-4D11-A26C-8A338CBEEA60) (Shutdown) 
-- iOS 18.5 --
    iPhone 16 Pro (0CF85EBA-0C01-4E30-89BA-8DF4D2293AE9) (Shutdown) 
    iPhone 16 Pro Max (79F75075-4405-4712-BACA-1118701F7D32) (Shutdown) 
    iPhone 16e (73B83F89-9B46-473B-85F3-71CC61E4688E) (Shutdown) 
    iPhone 16 (1122A235-8B04-4139-B652-88BDCF4727C4) (Shutdown) 
    iPhone 16 Plus (446A4CE5-DE51-4008-BA78-1060ED89E541) (Shutdown) 
    iPad Pro 11-inch (M4) (41A2E426-A7DD-4380-9E91-FA46A9BE8124) (Shutdown) 
    iPad Pro 13-inch (M4) (D7D53914-E44B-434A-8E8A-989C5F0AA1D6) (Shutdown) 
    iPad mini (A17 Pro) (A37CFB7E-39CC-490C-8AAB-7B38A206C8D7) (Shutdown) 
    iPad (A16) (0764116C-6504-429C-A2D1-E290024CA358) (Shutdown) 
    iPad Air 13-inch (M3) (6E0BF003-763E-499B-9ABC-D2E9BBB3BBA3) (Shutdown) 
    iPad Air 11-inch (M3) (07346815-3946-493C-8CF9-B1F8D7D72621) (Shutdown) 
-- iOS 26.3 --
    iPhone 17 Pro (CE5DE0A3-61D7-4186-BF0D-7EC5360C3573) (Shutdown) 
    iPhone 17 Pro Max (495B63CB-89F5-49E1-8626-E801E9F0E234) (Shutdown) 
    iPhone Air (554ED76A-6C36-4046-83BB-E316CD387DA1) (Shutdown) 
    iPhone 17 (B041CD5C-D279-4F6C-B7F1-69B6C991412D) (Shutdown) 
    iPhone 16e (E386D0BB-4A15-46BC-B933-00F7B485359B) (Shutdown) 
    iPad Pro 13-inch (M5) (73706957-A27C-41CB-8DAC-7B45FC7FE1D7) (Shutdown) 
    iPad Pro 11-inch (M5) (7C36DF04-3C33-4DDA-B298-8413702EAC3D) (Shutdown) 
    iPad mini (A17 Pro) (AD07D465-28C9-41BC-BDC0-B23486C41131) (Shutdown) 
    iPad (A16) (D36E9299-AC34-424A-9A19-6A89D2F28295) (Shutdown) 
    iPad Air 13-inch (M3) (4CC578CA-A262-4BEF-AF51-DD5141BB4B80) (Shutdown) 
    iPad Air 11-inch (M3) (99936AC8-57C9-41D7-A46C-8E3E11B56C84) (Shutdown) 
"""

/**
 * Raw stdout of `idb list-targets`.
 *
 * Seven ` | `-separated columns: name, UDID, state, target type, OS version, architecture
 * (`None` for a shutdown simulator) and companion status. Flat -- no runtime headers -- and
 * sorted by NAME, so the runtime lives in column five rather than in a section above.
 * Every row reads `No Companion Connected` because no `idb_companion` was running; the
 * command still exited 0 and still listed every simulator, so a missing companion is not an
 * error path the discovery parser has to handle.
 */
internal const val IDB_LIST_TARGETS: String = """iPad (10th generation) | 0CE8BC7C-8E6B-4FA8-A9D1-C5C61AACC946 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad (10th generation) | 209E0106-A82E-4C84-9058-59DEBD20B037 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad (10th generation) | F58C10ED-FCD1-4D11-A26C-8A338CBEEA60 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPad (A16) | 0764116C-6504-429C-A2D1-E290024CA358 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPad (A16) | D36E9299-AC34-424A-9A19-6A89D2F28295 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPad Air 11-inch (M2) | 6F1A7E2D-2CDE-4FBE-88E7-34C9328F20C4 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Air 11-inch (M2) | AAE4BCC3-4783-4438-9CA7-71667BADBBE5 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Air 11-inch (M2) | 9F4C90A5-8A71-4EE5-9E0C-4C917E1F46B5 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPad Air 11-inch (M3) | 07346815-3946-493C-8CF9-B1F8D7D72621 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPad Air 11-inch (M3) | 99936AC8-57C9-41D7-A46C-8E3E11B56C84 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPad Air 13-inch (M2) | CAC20292-3055-4B3B-92EE-BECDDADB7EC1 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Air 13-inch (M2) | FF94CD04-F9D9-435A-9D3B-9F08D336D2B5 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Air 13-inch (M2) | D32A6923-6194-4A6A-951E-33EAC1E84F37 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPad Air 13-inch (M3) | 6E0BF003-763E-499B-9ABC-D2E9BBB3BBA3 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPad Air 13-inch (M3) | 4CC578CA-A262-4BEF-AF51-DD5141BB4B80 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPad Pro 11-inch (M4) | 865DB6BF-801B-4EFC-A11F-721C9770DB66 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Pro 11-inch (M4) | ADBAE3FE-92BA-4C1D-91DE-EFA5AEB787C1 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Pro 11-inch (M4) | FA8351AE-8033-495C-BA87-9B072B9141EA | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPad Pro 11-inch (M4) | 41A2E426-A7DD-4380-9E91-FA46A9BE8124 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPad Pro 11-inch (M5) | 7C36DF04-3C33-4DDA-B298-8413702EAC3D | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPad Pro 13-inch (M4) | 9567B6A9-047A-4587-A10F-525D561601E9 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Pro 13-inch (M4) | B0B5D60B-8C4E-47D3-96CC-CA7CF0308753 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad Pro 13-inch (M4) | 3227FEE7-9089-41CA-83E7-28223362C155 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPad Pro 13-inch (M4) | D7D53914-E44B-434A-8E8A-989C5F0AA1D6 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPad Pro 13-inch (M5) | 73706957-A27C-41CB-8DAC-7B45FC7FE1D7 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPad mini (6th generation) | 6F20E331-1692-4938-B491-92C6C2B8DADD | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPad mini (A17 Pro) | 6DC67602-F428-4731-AED2-5B2936EA5715 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPad mini (A17 Pro) | A37CFB7E-39CC-490C-8AAB-7B38A206C8D7 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPad mini (A17 Pro) | AD07D465-28C9-41BC-BDC0-B23486C41131 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPhone 15 | 5CFB518E-2D13-4A76-ADA6-8234DF234EF7 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 | 68D2823C-F382-4C4E-877D-0A29F56FD47A | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 Plus | 98EAC1B6-882F-48CE-9E33-F6C5E9BB0FE0 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 Plus | C4BD6AC6-9F71-402D-BC15-E7377253B053 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 Pro | 4210645D-322E-43DB-B6FB-7E4F388D1DF5 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 Pro | 77C17238-5B51-4206-9A58-CA601AF51E7E | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 Pro Max | 98DBDA97-34DE-4652-933B-E3A348D40687 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 15 Pro Max | D89E58B3-0B21-41CC-A9DF-F6E38DA22DCA | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone 16 | 5F20FFD2-3B90-42DA-9227-26725B78EA58 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPhone 16 | 1122A235-8B04-4139-B652-88BDCF4727C4 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPhone 16 Plus | 6D100525-3C2F-4989-8EF4-E1BCCF346DBB | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPhone 16 Plus | 446A4CE5-DE51-4008-BA78-1060ED89E541 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPhone 16 Pro | 08F37FE6-AC58-4976-AE2B-4B7A980F8D7F | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPhone 16 Pro | 0CF85EBA-0C01-4E30-89BA-8DF4D2293AE9 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPhone 16 Pro Max | 6B5F435E-AF11-4C42-B2E3-8FFD24D1AF64 | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
iPhone 16 Pro Max | 79F75075-4405-4712-BACA-1118701F7D32 | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPhone 16e | 73B83F89-9B46-473B-85F3-71CC61E4688E | Shutdown | simulator | iOS 18.5 | None | No Companion Connected
iPhone 16e | E386D0BB-4A15-46BC-B933-00F7B485359B | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPhone 17 | B041CD5C-D279-4F6C-B7F1-69B6C991412D | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPhone 17 Pro | CE5DE0A3-61D7-4186-BF0D-7EC5360C3573 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPhone 17 Pro Max | 495B63CB-89F5-49E1-8626-E801E9F0E234 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPhone Air | 554ED76A-6C36-4046-83BB-E316CD387DA1 | Shutdown | simulator | iOS 26.3 | None | No Companion Connected
iPhone SE (3rd generation) | 27E8D85C-C23A-4590-8AB7-2D9507A2F35C | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone SE (3rd generation) | FFE14503-792C-4500-96B2-623DF571FF09 | Shutdown | simulator | iOS 17.5 | None | No Companion Connected
iPhone SE (3rd generation) | 3DA2880B-CEF0-4FF2-912A-9D29F5F5667D | Shutdown | simulator | iOS 18.1 | None | No Companion Connected
"""
