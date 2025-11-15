FROM python:3.11-slim

WORKDIR /app

COPY mock.py .

RUN pip install paho-mqtt

ENTRYPOINT ["python", "mock.py"]