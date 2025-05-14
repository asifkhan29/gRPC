package com.tutorial.gRPC.repo;

import com.tutorial.gRPC.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepo extends JpaRepository<Employe, Integer> {
}
