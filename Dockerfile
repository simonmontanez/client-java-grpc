FROM gcr.io/google_appengine/openjdk8

EXPOSE 50051

ADD ./build/dependency/client-java-grpc /client-java-grpc

ENTRYPOINT exec ./client-java-grpc/bin/client-java-grpc