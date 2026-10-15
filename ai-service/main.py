from fastapi import FastAPI, UploadFile, File
from pydantic import BaseModel
from typing import Optional
import uuid

app = FastAPI(title="Nursery CRM AI Service")

class DiagnosticResult(BaseModel):
    id: str
    cv_prediction: Optional[str] = None
    cv_confidence: Optional[float] = None
    needs_review: bool = True
    message: str

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/diagnose", response_model=DiagnosticResult)
def diagnose_leaf(file: UploadFile = File(...)):
    """
    Stub endpoint for leaf-photo diagnostics.
    Always returns needs_review=true until a real model exists.
    """
    return DiagnosticResult(
        id=str(uuid.uuid4()),
        cv_prediction=None,
        cv_confidence=None,
        needs_review=True,
        message="No trained model available — routing to human review queue."
    )
