package com.example.demo;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

public interface KeyValueRepo extends ReactiveCassandraRepository<KeyValue, String> {
}
