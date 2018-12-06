Write-Host "Hello World from $Env:AGENT_NAME."
Write-Host "My ID is $Env:AGENT_ID."
Write-Host "AGENT_WORKFOLDER contents:"
gci $Env:AGENT_WORKFOLDER
Write-Host "AGENT_BUILDDIRECTORY contents:"
gci $Env:AGENT_BUILDDIRECTORY
Write-Host "BUILD_SOURCESDIRECTORY contents:"
gci $Env:BUILD_SOURCESDIRECTORY
Write-Host "Over and out."

$url = "https://download.java.net/java/GA/jdk11/13/GPL/openjdk-11.0.1_windows-x64_bin.zip"
$output = "$Env:AGENT_BUILDDIRECTORY\openjdk-11.0.1_windows-x64_bin.zip"

Write-Host "JDK DOWNLOAD STARTED"

(New-Object System.Net.WebClient).DownloadFile($url, $output)

Write-Host "JDK DOWNLOAD COMPLETE"

Write-Host "AGENT_BUILDDIRECTORY contents again:"
gci $Env:AGENT_BUILDDIRECTORY
