package com.tutorial.gRPC.service;

import com.employees.EmpServiceGrpc;
import com.employees.empRequest;
import com.employees.empResponse;
import com.tutorial.gRPC.entity.Employe;
import com.tutorial.gRPC.repo.EmployeeRepo;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

@GrpcService
public class EmployeServices extends EmpServiceGrpc.EmpServiceImplBase {
    @Autowired
    private EmployeeRepo employeeRepo;

    @Override
    public void createEmp(empRequest request, StreamObserver<empResponse> responseObserver) {
        Employe saved = employeeRepo.save(Employe.builder()
                .name(request.getName())
                .email(request.getEmail()).build());

        responseObserver.onNext(empResponse.newBuilder().
                         setId(saved.getId())
                         .setEmail(saved.getEmail())
                         .setName(saved.getName())
                         .build());
        responseObserver.onCompleted();


    }
}
