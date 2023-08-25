package com.shaot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.shaot.model.Company;

public interface CompaniesRepository extends MongoRepository<Company, Long> {
	Optional<Company> findCompanyById(long id);
	Optional<Company> findCompanyByMail(String mail);
	List<Company> findAll();
}
