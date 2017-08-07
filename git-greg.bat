@echo off
if "%~1" neq "" (
  2>nul >nul findstr /rc:"^ *:%~1\>" "%~f0" && (
    shift /1
    goto %1
  ) || (
    >&2 echo ERROR: routine %~1 not found
  )
) else >&2 echo ERROR: missing routine
exit /b

:clone
echo executing :clone
git -c http.sslVerify=false clone https://github.com/zg2pro/spring-rest-basis.git
exit /b

:refresh
echo executing :refresh
git -c http.sslVerify=false pull --rebase
exit /b

:commit
echo executing :commit
git commit -m "from Montreuil"
git -c http.sslVerify=false push origin master
exit /b

