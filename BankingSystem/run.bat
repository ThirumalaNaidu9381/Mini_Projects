@echo off
echo Compiling FinSafe Banking System...
mkdir bin 2>nul
javac -cp "lib/*" -d bin src/com/banking/model/*.java src/com/banking/service/*.java src/com/banking/server/*.java src/com/banking/Main.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful!
echo Starting Server...
java -cp "bin;lib/*" com.banking.Main
pause
