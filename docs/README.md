# Distributed Job Scheduler

A lightweight, high-performance distributed job scheduling system being built to handle delayed jobs, background processing, and real-time job orchestration.

## 📝 Project Overview
This project is an exploratory implementation of a Distributed Job Scheduler, developed to handle interdependent jobs using Directed Acyclic Graphs (DAGs). Traditional schedulers like `cron` or `Spring @Scheduled` lack native distributed fault-tolerance, while tools like Apache Airflow can be too heavyweight for simple microservices. This project aims to find the middle ground.

## 🎯 Current Objectives
- Orchestrate jobs based on DAG dependencies.
- Ensure distributed execution across nodes.
- Implement fault tolerance and retry mechanisms.

## 🛠️ Planned Tech Stack
- **Backend:** Java, Spring Boot
- **Message Broker:** Apache Kafka
- **State/Caching:** Redis
- **Containerization:** Docker & Docker Compose

## 🏗️ Architecture (Work in Progress)
The system will rely on a Spring Boot REST API to ingest jobs, push them to a Kafka queue, and process them via a pool of worker nodes. Redis will be utilized to track job states and ensure idempotency.

*More detailed architecture diagrams and API contracts will be added as the project progresses.*

## 📂 Repository Structure
- `/runner`: Core Spring Boot scheduling logic and Kafka consumers.
- `/infrastructure`: Docker Compose, Logstash, and Prometheus configurations.
- `/express-cron-client`: External cron triggers.
- `/webhook-mock-server`: For testing webhook notifications.
- `/docs`: System design and API documentation.