FROM python:3.11-slim

WORKDIR /app

COPY . .

RUN pip install paho-mqtt confluent-kafka python-dotenv

ENTRYPOINT ["python", "telemetry.py"]