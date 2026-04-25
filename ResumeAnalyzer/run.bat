@echo off
echo Setting up Resume Analyzer ^& Job Matcher...

:: Check if pip is available
python -m pip --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Python or pip is not installed or not in PATH!
    pause
    exit /b %ERRORLEVEL%
)

echo Installing dependencies...
python -m pip install -r requirements.txt

echo Downloading spaCy English model...
python -m spacy download en_core_web_sm

echo Starting Streamlit app...
python -m streamlit run app.py

pause
