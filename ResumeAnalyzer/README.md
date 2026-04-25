# 📄 Resume Analyzer & Job Matcher

Hey there! Thanks for checking out the **Resume Analyzer & Job Matcher**. 

I built this tool because tailoring a resume for every single job application can be incredibly tedious. Often, we get rejected by automated Applicant Tracking Systems (ATS) simply because we missed a few critical keywords. I wanted to build something straightforward that takes the guesswork out of the process—using Natural Language Processing (NLP) to tell you exactly what your resume is missing compared to a specific job description.

Whether you're a fresh graduate tweaking your first resume or an experienced professional looking to pivot, I hope this makes your job hunt a little easier.

## ✨ What It Does

- **Smart Skill Extraction**: You can upload your resume (PDF or TXT) and paste in a job description. The application uses `spaCy` to intelligently extract technical and business skills.
- **Match Scoring**: It gives you a clear, honest percentage score indicating how well your resume matches the job requirements.
- **Actionable Insights**: Instead of a generic "good job," it explicitly lists the skills you successfully matched and the ones you are currently missing.
- **Tailored Feedback**: Depending on your match score, it provides practical suggestions on how you might want to adjust your resume before hitting submit.

## 🛠️ Tech Stack

I kept the stack modern but simple to ensure it's easy to run and extend:
- **Python 3**: The backbone of the application.
- **Streamlit**: For the clean, interactive, and fast web interface.
- **spaCy**: The NLP engine doing the heavy lifting for keyword and entity extraction.
- **PyPDF2**: For reliably parsing text out of uploaded PDF files.

## 🚀 Getting Started

I've set this up so you can run it locally with minimal friction. Make sure you have **Python 3.8+** installed.

### The "One-Click" Way (Windows)
If you're on Windows, I wrote a quick script to handle everything for you. Just double-click the `run.bat` file! 
It will automatically:
1. Install all required dependencies.
2. Download the necessary `spaCy` NLP model (`en_core_web_sm`).
3. Boot up the Streamlit server and open the app in your default browser.

### Manual Installation
If you're on macOS/Linux or prefer using the terminal, it's just three quick commands:

1. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```
2. **Download the NLP model**:
   ```bash
   python -m spacy download en_core_web_sm
   ```
3. **Start the application**:
   ```bash
   streamlit run app.py
   ```

## 🧪 Testing It Out

If you just want to see how it works without uploading your own personal resume, no problem. I've included a `sample_resume.txt` and a `sample_job_desc.txt` in the repository. Feel free to use those to test the matching logic out of the box!

---
*If you find this helpful, feel free to fork it, tweak it, or just use it to land that dream job. Happy hunting!*
