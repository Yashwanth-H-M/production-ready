# Canopy AI Service

## Purpose

The AI service is a Python 3.12 and FastAPI boundary for future plant-health assistance. It is deliberately separated from the Spring backend so model runtime, evaluation, media handling, and future inference scaling can evolve without placing machine-learning concerns in the primary tenant and compliance service.

## Current behavior

The current implementation is a safety-oriented stub. It returns advisory diagnostic output with `needs_review=true` for every result. It does not claim trained plant-pathology performance, it does not authorize treatment or shipment, and it does not create or alter tenant records directly.

> Diagnostic output is a review signal, not a crop decision. A human operator remains responsible for every operational or compliance action.

## Run locally

```bash
python -m venv .venv
# Activate .venv in the current shell.
pip install -r requirements.txt
uvicorn main:app --reload
```

The local service normally listens on port `8000`. When using the repository Compose stack, use the internal service hostname rather than exposing the service publicly.

## Integration boundary

| Responsibility | AI service | Spring backend |
|---|---|---|
| Model/advisory output | Produces review-required suggestion data | Receives or persists advisory fields through controlled workflows. |
| Tenant authorization | Never selects a tenant from model input | Derives tenant context from a verified principal. |
| Compliance/shipment decision | Never authorizes | Retains human-controlled business policy. |
| Operational data ownership | Does not directly write database records | Owns tenant-aware persistence. |
| Media governance | Future integration must define it | Must enforce authorization and storage policy before reference persistence. |

## Requirements before a real model

A production model integration needs labeled and governed data, evaluation thresholds, model-version traceability, confidence calibration, error monitoring, privacy and retention rules for images, and a review workflow that remains mandatory. It must also define request authentication, rate controls, input validation, failure behavior, and audit logging without recording sensitive images or tokens in application logs.

No model result may bypass the constraints documented in [../SECURITY.md](../SECURITY.md), [../DATA_MODEL.md](../DATA_MODEL.md), or [../ROADMAP.md](../ROADMAP.md).
