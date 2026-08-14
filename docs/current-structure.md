# Pre-refactor layout snapshot

Captured before package-by-feature moves. Phase 1 renamed the live package to `com.bank.observability.logsgateway`; this tree is the pre-rename snapshot.

```
.
├── README.md
├── docs/current-structure.md
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── bank
│   │   │           └── observability
│   │   │               └── logcontroller
│   │   │                   ├── LogControllerApplication.java
│   │   │                   ├── config
│   │   │                   │   ├── AsyncConfig.java
│   │   │                   │   ├── AwsS3Properties.java
│   │   │                   │   ├── HttpClientConfig.java
│   │   │                   │   ├── KafkaTopicProperties.java
│   │   │                   │   ├── OpenSearchProperties.java
│   │   │                   │   ├── RequestLoggingFilter.java
│   │   │                   │   └── S3ClientConfig.java
│   │   │                   ├── consumer
│   │   │                   │   ├── OpenSearchHttpClient.java
│   │   │                   │   ├── OpenSearchIndexer.java
│   │   │                   │   └── S3AuditArchiver.java
│   │   │                   ├── controller
│   │   │                   │   ├── GlobalExceptionHandler.java
│   │   │                   │   └── IngestionController.java
│   │   │                   ├── model
│   │   │                   │   └── LogPayload.java
│   │   │                   └── service
│   │   │                       ├── LogIngestionService.java
│   │   │                       ├── LogRoutingService.java
│   │   │                       └── PiiMaskingService.java
│   │   └── resources
│   │       └── application.yml
│   └── test
│       └── java
│           └── com
│               └── bank
│                   └── observability
│                       └── logcontroller
│                           ├── consumer
│                           │   ├── OpenSearchIndexerTest.java
│                           │   └── S3AuditArchiverTest.java
│                           ├── controller
│                           │   └── IngestionControllerTest.java
│                           ├── model
│                           │   └── LogPayloadTest.java
│                           └── service
│                               ├── LogIngestionServiceTest.java
│                               ├── LogRoutingServiceTest.java
│                               └── PiiMaskingServiceTest.java
└── target
    ├── classes
    │   ├── META-INF
    │   │   └── spring-configuration-metadata.json
    │   ├── application.yml
    │   └── com
    │       └── bank
    │           └── observability
    │               └── logcontroller
    │                   ├── LogControllerApplication.class
    │                   ├── config
    │                   │   ├── AsyncConfig.class
    │                   │   ├── AwsS3Properties$S3.class
    │                   │   ├── AwsS3Properties.class
    │                   │   ├── HttpClientConfig.class
    │                   │   ├── KafkaTopicProperties.class
    │                   │   ├── OpenSearchProperties.class
    │                   │   ├── RequestLoggingFilter.class
    │                   │   └── S3ClientConfig.class
    │                   ├── consumer
    │                   │   ├── OpenSearchHttpClient.class
    │                   │   ├── OpenSearchIndexer.class
    │                   │   └── S3AuditArchiver.class
    │                   ├── controller
    │                   │   ├── GlobalExceptionHandler.class
    │                   │   └── IngestionController.class
    │                   ├── model
    │                   │   ├── LogPayload$LogPayloadBuilder.class
    │                   │   └── LogPayload.class
    │                   └── service
    │                       ├── LogIngestionService.class
    │                       ├── LogRoutingService.class
    │                       └── PiiMaskingService.class
    ├── generated-sources
    │   └── annotations
    ├── generated-test-sources
    │   └── test-annotations
    ├── maven-status
    │   └── maven-compiler-plugin
    │       ├── compile
    │       │   └── default-compile
    │       │       ├── createdFiles.lst
    │       │       └── inputFiles.lst
    │       └── testCompile
    │           └── default-testCompile
    │               ├── createdFiles.lst
    │               └── inputFiles.lst
    ├── surefire-reports
    │   ├── TEST-com.bank.observability.logcontroller.consumer.OpenSearchIndexerTest.xml
    │   ├── TEST-com.bank.observability.logcontroller.consumer.S3AuditArchiverTest.xml
    │   ├── TEST-com.bank.observability.logcontroller.controller.IngestionControllerTest.xml
    │   ├── TEST-com.bank.observability.logcontroller.model.LogPayloadTest.xml
    │   ├── TEST-com.bank.observability.logcontroller.service.LogIngestionServiceTest.xml
    │   ├── TEST-com.bank.observability.logcontroller.service.LogRoutingServiceTest.xml
    │   ├── TEST-com.bank.observability.logcontroller.service.PiiMaskingServiceTest.xml
    │   ├── com.bank.observability.logcontroller.consumer.OpenSearchIndexerTest.txt
    │   ├── com.bank.observability.logcontroller.consumer.S3AuditArchiverTest.txt
    │   ├── com.bank.observability.logcontroller.controller.IngestionControllerTest.txt
    │   ├── com.bank.observability.logcontroller.model.LogPayloadTest.txt
    │   ├── com.bank.observability.logcontroller.service.LogIngestionServiceTest.txt
    │   ├── com.bank.observability.logcontroller.service.LogRoutingServiceTest.txt
    │   └── com.bank.observability.logcontroller.service.PiiMaskingServiceTest.txt
    └── test-classes
        └── com
            └── bank
                └── observability
                    └── logcontroller
                        ├── consumer
                        │   ├── OpenSearchIndexerTest.class
                        │   └── S3AuditArchiverTest.class
                        ├── controller
                        │   └── IngestionControllerTest.class
                        ├── model
                        │   └── LogPayloadTest.class
                        └── service
                            ├── LogIngestionServiceTest.class
                            ├── LogRoutingServiceTest.class
                            └── PiiMaskingServiceTest.class

56 directories, 77 files
```
