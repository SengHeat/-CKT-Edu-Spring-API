package com.api.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "cache_entries")
public class CacheEntry {
    @Id
    @Column(length = 191)
    private String key;
    @Column(columnDefinition = "text")
    private String value;
    private Instant expiration;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Instant getExpiration() { return expiration; }
    public void setExpiration(Instant expiration) { this.expiration = expiration; }
}
