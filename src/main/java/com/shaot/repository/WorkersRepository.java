package com.shaot.repository;

import com.shaot.model.Worker;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkersRepository extends MongoRepository<Worker, Long>{	
	Optional<Worker> findWorkerById(long id);
}
