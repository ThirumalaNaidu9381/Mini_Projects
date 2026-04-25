import streamlit as st
import spacy
from PyPDF2 import PdfReader
import re

# Load NLP model
@st.cache_resource
def load_nlp():
    try:
        return spacy.load("en_core_web_sm")
    except OSError:
        # Fallback will be handled in the extraction logic if model is not downloaded
        return None

nlp = load_nlp()

# A comprehensive list of skills to look for in resumes and job descriptions
KNOWN_SKILLS = {
    "python", "java", "c++", "c", "c#", "javascript", "typescript", "ruby", "go", "rust", "php", "swift", "kotlin",
    "sql", "mysql", "postgresql", "mongodb", "nosql", "oracle", "redis", "elasticsearch",
    "html", "css", "react", "angular", "vue", "node.js", "django", "flask", "spring", "fastapi", "bootstrap", "tailwind",
    "aws", "azure", "gcp", "docker", "kubernetes", "jenkins", "git", "github", "gitlab", "ci/cd", "terraform", "ansible",
    "machine learning", "deep learning", "nlp", "computer vision", "tensorflow", "pytorch", "scikit-learn", "pandas", "numpy", "matplotlib",
    "data analysis", "data science", "data engineering", "big data", "hadoop", "spark", "kafka",
    "agile", "scrum", "kanban", "leadership", "communication", "problem solving", "teamwork", "project management",
    "linux", "bash", "shell scripting", "networking", "security", "cryptography", "api", "rest", "graphql"
}

def extract_text_from_pdf(file):
    reader = PdfReader(file)
    text = ""
    for page in reader.pages:
        extracted = page.extract_text()
        if extracted:
            text += extracted + " "
    return text

def extract_skills(text):
    if not nlp:
        # Fallback to basic regex extraction if spacy fails to load
        words = set(re.findall(r'\b\w+\b', text.lower()))
        return words.intersection(KNOWN_SKILLS)
    
    doc = nlp(text.lower())
    extracted_skills = set()
    
    # Check for direct word matches (tokens)
    for token in doc:
        if token.text in KNOWN_SKILLS:
            extracted_skills.add(token.text)
            
    # Check for multi-word phrases (e.g. "machine learning")
    for chunk in doc.noun_chunks:
        chunk_text = chunk.text.strip()
        if chunk_text in KNOWN_SKILLS:
            extracted_skills.add(chunk_text)
            
    return extracted_skills

st.set_page_config(page_title="Resume Analyzer & Job Matcher", page_icon="📄", layout="wide")

st.title("📄 Resume Analyzer & Job Matcher")
st.markdown("Upload your resume and paste a job description to see how well you match the role! We use NLP to extract skills and provide actionable insights.")

if not nlp:
    st.warning("⚠️ SpaCy model 'en_core_web_sm' is not loaded. Falling back to basic keyword matching. For better results, ensure the model is downloaded.")

col1, col2 = st.columns(2)

with col1:
    st.subheader("1. Upload Resume")
    uploaded_file = st.file_uploader("Upload PDF or Text file", type=["pdf", "txt"])

with col2:
    st.subheader("2. Job Description")
    job_desc = st.text_area("Paste the job description here", height=200, placeholder="e.g. We are looking for a Software Engineer with experience in Python, AWS, and Docker...")

if st.button("Analyze Match", type="primary"):
    if uploaded_file and job_desc:
        with st.spinner("Analyzing documents using NLP..."):
            # Extract resume text
            if uploaded_file.name.endswith('.pdf'):
                resume_text = extract_text_from_pdf(uploaded_file)
            else:
                resume_text = uploaded_file.read().decode('utf-8')
            
            # Extract skills
            resume_skills = extract_skills(resume_text)
            job_skills = extract_skills(job_desc)
            
            # Match Logic
            if not job_skills:
                st.warning("Could not identify any key technical/business skills in the job description. Please ensure it contains standard keywords.")
            else:
                matched_skills = resume_skills.intersection(job_skills)
                missing_skills = job_skills - resume_skills
                
                match_score = (len(matched_skills) / len(job_skills)) * 100
                
                st.divider()
                st.subheader("📊 Match Results")
                
                # Display Score
                score_color = "green" if match_score >= 75 else "orange" if match_score >= 50 else "red"
                st.markdown(f"### Match Score: <span style='color:{score_color}'>{match_score:.1f}%</span>", unsafe_allow_html=True)
                st.progress(int(match_score))
                
                col_res1, col_res2 = st.columns(2)
                
                with col_res1:
                    st.success(f"✅ Matched Skills ({len(matched_skills)})")
                    if matched_skills:
                        for skill in sorted(matched_skills):
                            st.write(f"- {skill.title()}")
                    else:
                        st.write("No matching skills found.")
                        
                with col_res2:
                    st.error(f"❌ Missing Skills ({len(missing_skills)})")
                    if missing_skills:
                        for skill in sorted(missing_skills):
                            st.write(f"- {skill.title()}")
                    else:
                        st.write("You match all identified skills! Great job!")
                        
                st.divider()
                st.subheader("💡 Suggestions for Improvement")
                if match_score >= 80:
                    st.info("🚀 Excellent match! Your resume is highly tailored for this role. Ensure your bullet points highlight quantifiable achievements using these exact keywords.")
                elif match_score >= 50:
                    st.warning("⚠️ Good start, but you can improve. Consider updating your resume to explicitly include the missing skills listed above. If you don't know them, they might be good learning opportunities!")
                else:
                    st.error("📉 Low match. This role might require a different skill set. Review the missing skills. If you possess them, add them to your resume using the exact terminology from the job description.")
                    
                with st.expander("View All Skills Extracted From Your Resume"):
                    if resume_skills:
                        st.write(", ".join([s.title() for s in sorted(resume_skills)]))
                    else:
                        st.write("No recognized skills extracted.")
    else:
        st.error("Please provide both a resume and a job description.")
