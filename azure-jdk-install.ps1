# Download JDK 11
$url = "https://download.java.net/java/GA/jdk11/13/GPL/openjdk-11.0.1_windows-x64_bin.zip"
$output = "$Env:AGENT_TOOLSDIRECTORY\openjdk-11.0.1_windows-x64_bin.zip"

Write-Host "JDK Download started ..."
(New-Object System.Net.WebClient).DownloadFile($url, $output)
Write-Host "JDK Download complete!"
