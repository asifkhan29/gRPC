package com.tutorial.gRPC.service;


import com.employees.*;
import com.tutorial.gRPC.entity.Employe;
import com.tutorial.gRPC.repo.EmployeeRepo;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;

import java.util.ArrayList;
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

    @Override
    public void getAllEmpsStream(Empty request, StreamObserver<EmpResponse> responseObserver) {
        List<Employe> employee = employeeRepo.findAll();
        employee.forEach(emp ->
        {
            EmpResponse response = EmpResponse.newBuilder()
                    .setId(emp.getId())
                    .setName(emp.getName())
                    .setEmail(emp.getEmail())
                    .build();
            responseObserver.onNext(response);
            try {
                Thread.sleep(1000); // 1 second delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                responseObserver.onError(e); // Handle interruption
                return;
            }

        });
        responseObserver.onCompleted();

    }

    @Override
    public StreamObserver<EmpRequest> saveMulipleEmployeeAsStream(StreamObserver<UploadStatus> responseObserver) {
        List<Employe> emp = new ArrayList<>();


        return new StreamObserver<EmpRequest>() {
            @Override
            public void onNext(EmpRequest value) {
                Employe emp1 = Employe.builder().email(value.getEmail())
                        .name(value.getName())
                        .build();

                emp.add(emp1);

            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onNext(
                        UploadStatus.newBuilder().setMessage("Error saving employees / No Employee Saved / Please Try Again")
                                .setCount(0).build()
                );
                responseObserver.onCompleted();

            }

            @Override
            public void onCompleted() {
                employeeRepo.saveAll(emp);

                responseObserver.onNext(
                        UploadStatus.newBuilder().setMessage("Successfully saved employees")
                                .setCount(emp.size()).build()
                );
                responseObserver.onCompleted();

            }
        };
    }

    @Override
    public StreamObserver<EmpIdRequest> getEmployeeByIdStream(StreamObserver<EmpStreamResponse> responseObserver) {
        return new StreamObserver<EmpIdRequest>() {
            @Override
            public void onNext(EmpIdRequest value) {
                Optional<Employe> emp = employeeRepo.findById(value.getId());
                EmpStreamResponse response;
                if (emp.isPresent()) {
                    Employe employe = emp.get();
                    response = EmpStreamResponse.newBuilder().setId(0)
                            .setEmail(employe.getEmail())
                            .setName(employe.getName())
                            .setStatus("Employee Found")
                            .build();
                }
                else{
                    response = EmpStreamResponse.newBuilder()
                            .setStatus("No Employee Found")
                            .build();
                }
                responseObserver.onNext(response);

            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException("Error in fetching Empolyee / Please Restart the Connection");
            }

            @Override
            public void onCompleted() {

                responseObserver.onCompleted();

            }
        };
    }
}
