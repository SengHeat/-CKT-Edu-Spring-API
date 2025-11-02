package com.ckt.api.user.model.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "jobs")
public class Job {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String queue;
    @Column(columnDefinition = "text")
    private String payload;
    private int attempts;
    private Instant availableAt = Instant.now();
    private Instant reservedAt;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate(){ this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQueue() { return queue; }
    public void setQueue(String queue) { this.queue = queue; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
    public Instant getReservedAt() { return reservedAt; }
    public void setReservedAt(Instant reservedAt) { this.reservedAt = reservedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
