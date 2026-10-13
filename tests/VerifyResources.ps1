$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$res = Join-Path $root 'app/src/main/res'
$en = [xml](Get-Content -Raw -Encoding UTF8 (Join-Path $res 'values/strings.xml'))
$cn = [xml](Get-Content -Raw -Encoding UTF8 (Join-Path $res 'values-zh-rCN/strings.xml'))
$english = @{}
$chinese = @{}
foreach ($s in $en.resources.string) { $english[$s.name] = [string]$s.InnerText }
foreach ($s in $cn.resources.string) { $chinese[$s.name] = [string]$s.InnerText }
if (@(Compare-Object @($english.Keys) @($chinese.Keys)).Count) { throw 'Translation keys differ' }
foreach ($key in $english.Keys) {
    $a = [regex]::Matches($english[$key], '%\d+\$[sd]') | ForEach-Object Value
    $b = [regex]::Matches($chinese[$key], '%\d+\$[sd]') | ForEach-Object Value
    if (($a -join ',') -ne ($b -join ',')) { throw "Placeholder mismatch: $key" }
}
foreach ($locale in @('values-zh-rSG', 'values-b+zh+Hans')) {
    $variant = [xml](Get-Content -Raw -Encoding UTF8 (Join-Path $res "$locale/strings.xml"))
    foreach ($s in $variant.resources.string) {
        if ($chinese[$s.name] -ne [string]$s.InnerText) { throw "Translation mismatch: $locale/$($s.name)" }
    }
}
Write-Output "PASS: $($english.Count) matching translation keys and placeholders"
