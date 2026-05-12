param(
    [string]$WorkDir = "$PSScriptRoot\training-work",
    [switch]$CopyToAndroidAssets,
    [switch]$SkipInstall,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$ExtraArgs
)

$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

if (-not $SkipInstall) {
    python -m pip install -r requirements-training.txt
}

$argsList = @("train_krishiscan.py", "--work-dir", $WorkDir)
if ($CopyToAndroidAssets) {
    $argsList += "--copy-to-android-assets"
}
if ($ExtraArgs) {
    $argsList += $ExtraArgs
}

python @argsList
