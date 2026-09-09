$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api'
$recipeId = 6

$steps = Invoke-RestMethod "$base/recipes/$recipeId/steps"
$defsStep = Invoke-RestMethod "$base/parameter-definitions?stepType=STEP"
$defsPre = Invoke-RestMethod "$base/parameter-definitions?stepType=PRESTEP"

$valueTypeByDef = @{}
foreach ($d in $defsStep) { $valueTypeByDef[[string]$d.id] = $d.valueType }
foreach ($d in $defsPre) { $valueTypeByDef[[string]$d.id] = $d.valueType }

$updated = 0
$checkedNumeric = 0
$rand = [System.Random]::new()

foreach ($s in $steps) {
    $params = Invoke-RestMethod "$base/steps/$($s.id)/parameters"
    foreach ($p in $params) {
        $defId = [string]$p.definitionId
        if (-not $valueTypeByDef.ContainsKey($defId)) { continue }
        if ($valueTypeByDef[$defId] -ne 'NUMBER') { continue }

        $checkedNumeric++
        $newValue = $rand.Next(0, 1001)

        $payload = $p
        $payload.valueJson = [string]$newValue
        $payload.selectedOptionId = $null

        Invoke-RestMethod -Method Put -Uri "$base/step-parameters/$($p.id)" -ContentType 'application/json' -Body ($payload | ConvertTo-Json -Depth 12 -Compress) | Out-Null
        $updated++
    }
}

# Quick validation pass
$outOfRange = 0
$nullValues = 0
foreach ($s in $steps) {
    $params = Invoke-RestMethod "$base/steps/$($s.id)/parameters"
    foreach ($p in $params) {
        $defId = [string]$p.definitionId
        if (-not $valueTypeByDef.ContainsKey($defId)) { continue }
        if ($valueTypeByDef[$defId] -ne 'NUMBER') { continue }

        if ($null -eq $p.valueJson -or "$($p.valueJson)".Trim().Length -eq 0) {
            $nullValues++
            continue
        }

        $n = 0
        if (-not [int]::TryParse("$($p.valueJson)", [ref]$n)) {
            $outOfRange++
            continue
        }
        if ($n -lt 0 -or $n -gt 1000) { $outOfRange++ }
    }
}

Write-Output "RECIPE_ID=$recipeId"
Write-Output "NUMERIC_PARAMS_FOUND=$checkedNumeric"
Write-Output "UPDATED_NUMERIC_PARAMS=$updated"
Write-Output "VALIDATION_NULL_VALUES=$nullValues"
Write-Output "VALIDATION_OUT_OF_RANGE=$outOfRange"
