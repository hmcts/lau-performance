LAU Performance Tests
Gatling performance tests for Log and Audit

Purpose
The L&A Case Audit microservice covers case activity (which includes case creations, updates and views). There are 3 types of seacrhes for this repo: The 'Case Activity' search brings back the user actions (such as View, Update,Create Etc) whereas the 'Case Searches' search shows how many cases the user accessed during their search. The 'Logon Results' search shows the results from each time the User log ins during a set time period. The deletion search shows the cases that have been deleted by the case disposer within a set time period.

Overview
lau-performance • lau-frontend • lau-idam-backend • lau-case-backend • lau-shared-infrastructure

Getting Started
This is repository for the LAU Performance Tests

Step1: Clone the repo to your local/VM to run
Step2: cd into the lau-performance directory
Step3: Run the test with the command ./gradlew gatlingRun

Users
The Users in the Users.csv file have cft-audit-investigator role to access the audit search (you can also see deleted cases changing the activity to delete) and have the cft-audit-investigator and cft-service-logs roles to access the audit and deletion searches. The UsersDeletion users has the cft-service-logs role to just access the deletion search.

Building and deploying the application
Building the application
The project uses Gradle as a build tool. It already contains ./gradlew wrapper script, so there's no need to install gradle.

To build the project execute the following command:

  ./gradlew build
Running the application
The LAUSimulation runs all the searches.

To run locally: - Performance test against the perftest environment:

./gradlew gatlingRun
Flags: - Debug (single-user mode): -Ddebug=on e.g. ./gradlew gatlingRun -Ddebug=on - Run against AAT: Denv=aat e.g.:

./gradlew gatlingRun -Denv=aat
