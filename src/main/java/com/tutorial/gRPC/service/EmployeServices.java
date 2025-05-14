package com.tutorial.gRPC.service;


import com.employees.*;
import com.tutorial.gRPC.entity.Employe;
import com.tutorial.gRPC.repo.EmployeeRepo;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@GrpcService
public class EmployeServices extends EmpServiceGrpc.EmpServiceImplBase {
    @Autowired
    private EmployeeRepo employeeRepo;

    @Override
    public void createEmp(EmpRequest request, StreamObserver<EmpResponse> responseObserver) {
        Employe saved = employeeRepo.save(Employe.builder()
                .name(request.getName())
                .email(request.getEmail()).build());

        responseObserver.onNext(EmpResponse.newBuilder().
                         setId(saved.getId())
                         .setEmail(saved.getEmail())
                         .setName(saved.getName())
                         .build());
        responseObserver.onCompleted();


    }


    @Override
    public void getEmpById(EmpIdRequest request, StreamObserver<EmpResponse> responseObserver) {
        Optional<Employe> optional = employeeRepo.findById(request.getId());

        if (optional.isPresent()) {
            Employe emp = optional.get();
            EmpResponse response = EmpResponse.newBuilder()
                    .setId(emp.getId())
                    .setName(emp.getName())
                    .setEmail(emp.getEmail())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("Employee not found with ID: " + request.getId()));
        }
    }

    @Override
    public void updateEmp(EmpRequest request, StreamObserver<EmpResponse> responseObserver) {
        Optional<Employe> optional = employeeRepo.findById(request.getId());

        if (optional.isPresent()) {
            Employe emp = optional.get();
            emp.setName(request.getName());
            emp.setEmail(request.getEmail());

            Employe updated = employeeRepo.save(emp);

            EmpResponse response = EmpResponse.newBuilder()
                    .setId(updated.getId())
                    .setName(updated.getName())
                    .setEmail(updated.getEmail())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("Cannot update. Employee not found with ID: " + request.getId()));
        }
    }

    @Override
    public void deleteEmp(EmpIdRequest request, StreamObserver<DeleteResponse> responseObserver) {
        Optional<Employe> optional = employeeRepo.findById(request.getId());

        if (optional.isPresent()) {
            employeeRepo.deleteById(request.getId());
            DeleteResponse response = DeleteResponse.newBuilder()
                    .setMessage("Employee deleted successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("Cannot delete. Employee not found with ID: " + request.getId()));
        }
    }

    @Override
    public void getAllEmps(Empty request, StreamObserver<EmpListResponse> responseObserver) {
        List<Employe> employees = employeeRepo.findAll();

        List<EmpResponse> empResponses = employees.stream()
                .map(emp -> EmpResponse.newBuilder()
                        .setId(emp.getId())
                        .setName(emp.getName())
                        .setEmail(emp.getEmail())
                        .build())
                .collect(Collectors.toList());

        EmpListResponse response = EmpListResponse.newBuilder()
                .addAllEmployees(empResponses)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
