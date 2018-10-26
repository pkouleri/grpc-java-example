package com.github.pkouleri.grpc.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        //sum(channel);
        //decomposePrime(channel);
        computeAverage(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    public static void main(String[] args) {
        new CalculatorClient().run();
    }

    private void computeAverage(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<ComputeAverageRequest> requestObserver = asyncClient.computeAverage(
                new StreamObserver<ComputeAverageResponse>() {

                    @Override
                    public void onNext(ComputeAverageResponse value) {
                        // we received a result from the server
                        System.out.println("The average is: " + value.getAverage());
                    }

                    @Override
                    public void onError(Throwable t) {
                        // server sent an error
                    }

                    @Override
                    public void onCompleted() {
                        // the server response is complete
                        latch.countDown();
                    }
                });

        for (int i=0; i< 10000; i++) {
            requestObserver.onNext(ComputeAverageRequest.newBuilder()
                    .setNumber(i)
                    .build());
        }

        requestObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void decomposePrime(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub client = CalculatorServiceGrpc.newBlockingStub(channel);

        client.primeNumberDecomposition(PrimeNumberDecompositionRequest.newBuilder()
                .setNumber(120)
                .build())
                .forEachRemaining(primeNumberDecompositionResponse -> {
                    System.out.print(primeNumberDecompositionResponse.getPrimeFactor() + " ");
                });
    }

    private void sum(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub client = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest sumRequest = SumRequest.newBuilder()
                .setFirstNumber(10)
                .setSecondNumber(3)
                .build();

        SumResponse sumResponse = client.sum(sumRequest);
        System.out.println(sumResponse.getSumResult());
    }
}
