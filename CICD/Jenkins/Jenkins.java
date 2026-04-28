JENKINS
========================================== 

Jenkins is a tool used to make CICD possible


Dependancy: 
    2 GB ram
    Java should be installed in the system where Jenkins master is being setup (Jenkins is written in JAVA) 

Install Jenkins from : https://www.jenkins.io/doc/book/installing/ 
                        (also include commands to install java)

Plugins:
    all the needed plugins must be installed in Jenkins that is needed for production (eg: production machine login, docker)
    1. production machine login (publish over ssh) 
    2. SonarQube (sonarqube scanner)
    3. Docker (Docker, docker commons, docker pipeline, docker API, docker-build-setup)
    4. JDK (Eclipse Temurin installer)
    5. Node (NodeJs)
    6. pipeline stage view // to see visually the pipeline 


Types of pipeline:
    1. freestyle (no script-- only in development or small application)
    2. pipeline (using script using Groovy)  -- used in production

Tool configuration:
    Give name to tools (this names should be used in the script otherwise jenkins will not understand which is the tools)
    eg: for jdk 'jdk17'
        nodejs 'node18'

system configuration:
    This is where ssh communication to the prod machine is setup using (pem key, ip , username)



==========================================
        Setup Jenkins
==========================================

1. Launch t2.medium EC2 machine and open port 8080

2. Install Java first from the below site

3. Go to site https://www.jenkins.io/doc/book/installing/ for installation guide

    //check it after installation "systemctl status jenkins"

4. Access the jenkins --> <public ip>:8080

5. cat /var/lib/jenkins/secrets/initialAdminPassword //to get the initial pass

6. Setup jenkins

7. Install plugins for connecting to the prod server (publish over ssh)

8. Go to System configuration and add the details of the prod server (pem key, ip , username)

==========================================
        Freestyle Pipeline
==========================================

1. On jenkins home page create new project by using "New Item"

2. Install plugins for connecting to the prod server (publish over ssh)

3. Go to System configuration and add the details of the prod server (pem key, ip , username)



==========================================
         Pipeline (Groovy script)
==========================================

---> Setup sonar cube using docker in jenkins host machine  // used to check the application codes for security vulnerabilities

1. install docker in jenkins host machine

2. docker run -d -p 9000:9000 --name sonar sonarqube:lts   //community based image (already available)

3. Login to sonar cube <hostip>:9000 // user name and password is "admin"




---> Setup Trivy  // scan the filesystems and docker image 

sudo apt-get install wget apt-transport-https gnupg lsb-release -y
wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | gpg --dearmor | sudo tee /usr/share/keyrings/trivy.gpg > /dev/null
echo "deb [signed-by=/usr/share/keyrings/trivy.gpg] https://aquasecurity.github.io/trivy-repo/deb $(lsb_release -sc) main" | sudo tee -a /etc/apt/sources.list.d/trivy.list
sudo apt-get update
sudo apt-get install trivy -y