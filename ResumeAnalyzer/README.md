# Resume Analyzer & Job Matcher

## Project Overview

The Resume Analyzer & Job Matcher is a Python-based application designed to automate the process of resume screening and candidate evaluation. Utilizing Natural Language Processing (NLP), the application extracts critical technical and business skills from applicant resumes and compares them against specific job descriptions. By quantifying the alignment between candidate qualifications and role requirements, this tool provides actionable, data-driven insights to both recruiters and job seekers.

The system is built to streamline the preliminary applicant tracking process, offering immediate scoring metrics, identifying skill gaps, and improving the efficiency of candidate assessment workflows.

## Key Features

- **Automated Skill Extraction**: Leverages advanced NLP tokenization to identify and extract relevant technical skills and industry-standard keywords from unstructured text.
- **Match Scoring Algorithm**: Calculates an objective percentage score indicating the precise alignment between a candidate's resume and the target job description.
- **Actionable Insights Reporting**: Generates a comprehensive breakdown categorized into matched skills and missing requirements, allowing for rapid candidate evaluation.
- **Interactive User Interface**: Provides a streamlined, web-based dashboard for seamless document uploading and immediate results visualization.
- **Multi-Format Document Parsing**: Natively supports text extraction from PDF documents and standard text files.

## Technology Stack

- **Python 3.x**: Core application logic and processing.
- **Streamlit**: Web application framework utilized for the presentation layer.
- **spaCy**: Industrial-strength NLP library employed for Named Entity Recognition (NER) and tokenization.
- **PyPDF2**: Library used for robust text extraction from PDF files.

## Prerequisites

Ensure the following dependencies are installed in the host environment prior to deployment:
- Python 3.8 or higher
- `pip` package manager

## Installation & Setup

1. **Clone or Download the Repository**
   Ensure all project files are located within the target directory.

2. **Install Dependencies**
   Open a terminal session and install the required Python packages:
   ```bash
   pip install -r requirements.txt
   ```

3. **Download the NLP Language Model**
   The application requires the English core web model for spaCy to perform accurate text analysis. Execute the following command:
   ```bash
   python -m spacy download en_core_web_sm
   ```

## Usage

### Standard Execution
To launch the application server manually via the terminal, navigate to the project directory and execute:

```bash
streamlit run app.py
```
The application will start a local web server and automatically open the interface in the default web browser.

### Automated Execution (Windows Environments)
For Windows environments, a batch execution script is provided. Double-clicking the `run.bat` file will automatically handle dependency installation, model downloading, and application execution in a single step.

## Application Architecture Overview

The system is structured into four primary logical components:
- **Data Ingestion Layer**: Manages file uploads and parses raw text from standard formats (`.pdf`, `.txt`) using `PyPDF2`.
- **NLP Processing Engine**: Utilizes `spaCy` to process the raw text, filtering out noise and isolating predefined technical keywords and phrases.
- **Matching & Analytics Engine**: Executes set-theory operations (intersections and differences) on the extracted skill datasets to compute the overall match percentage.
- **Presentation Layer**: Built with `Streamlit` to handle user interactions, render upload forms, and display the analytical results in an easily digestible visual format.
