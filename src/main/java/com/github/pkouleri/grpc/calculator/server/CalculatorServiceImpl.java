package com.github.pkouleri.grpc.calculator.server;

import com.proto.calculator.*;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        SumResponse sumResponse = SumResponse.newBuilder()
                .setSumResult(request.getFirstNumber() + request.getSecondNumber())
                .build();

        responseObserver.onNext(sumResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecomposition(PrimeNumberDecompositionRequest request, StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
        int divisor = 2;
        int number = request.getNumber();

        while (number > 1) {
            if (number % divisor == 0) {
                number = number/divisor;

                responseObserver.onNext(
                        PrimeNumberDecompositionResponse.newBuilder()
                                .setPrimeFactor(divisor)
                                .build());
            } else {
                divisor = divisor + 1;
            }
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
        StreamObserver<ComputeAverageRequest> requestObserver = new StreamObserver<ComputeAverageRequest>() {
            int count = 0;
            int sum = 0;

            @Override
            public void onNext(ComputeAverageRequest value) {
                // we got a request from the client
                count++;
                sum += value.getNumber();
            }

            @Override
            public void onError(Throwable t) {
                // we got an error from the client

            }

            @Override
            public void onCompleted() {
                // client is done sending requests, we now want to respond
                int average = sum / count;
                responseObserver.onNext(ComputeAverageResponse.newBuilder()
                        .setAverage(average)
                        .build());
                responseObserver.onCompleted();
            }
        };

        return requestObserver;
    }
}
