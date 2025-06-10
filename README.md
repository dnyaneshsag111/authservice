# Auth Service

## Overview
This project is a Spring Boot-based authentication service that integrates with Keycloak for user management and authentication. It provides APIs for login, user creation, and user search functionalities.

## Features
- **Login API**: Authenticate users using Keycloak's token endpoint.
- **Create User API**: Create new users in Keycloak.
- **Search User API**: Fetch users by attributes from Keycloak.
- **Delete User API**: Delete users by UUID in Keycloak.
## Technologies Used
- **Java**: Programming language.
- **Spring Boot**: Framework for building RESTful APIs.
- **Keycloak**: Identity and access management.
- **Maven**: Build tool.
- **Docker**: Containerization for Keycloak setup.
- **PostgreSQL**: Database for Keycloak.

## Prerequisites
- Java 21
- Maven 3.8 or higher
- Docker or Podman installed
- Keycloak Admin Client configured
- PostgreSQL running 

## Local Setup Instructions 

### Keycloak Configuration
1. Start Keycloak using the provided `podman-compose.yml` file: 
   ```bash
   podman-compose up -d

### Access Keycloak Admin Console

1. **Open the Admin Console**  
   Open your browser and navigate to `http://localhost:8080/auth/admin/` to access the Keycloak admin console.

2. **Login with Admin Credentials**
   - **Username**: `admin`
   - **Password**: `admin`

3. **Create a New Realm**
   - Navigate to the **Realms** section and click **Add Realm**.
   - Enter `auth` as the realm name and save.

4. **Create a New Client**
   - Go to the **Clients** section within the `auth` realm.
   - Click **Create Client**.
   - Enter `auth-realm-client` as the client ID.
   - Set the **Access Type** to `confidential`.
   - Configure the redirect URI as needed (e.g., `http://localhost:8082/*`).

5. **Configure Client Credentials**
   - Go to the **Credentials** tab of the client and copy the **Client Secret**.
   - client id will be your client name
   - This will be used in the application properties.
   - client's capability config should look like this
     ![Example Image](https://raw.githubusercontent.com/hrushipn/demoFiles/main/rawReadme/capability%20config.png)
   - Since we have enabled service account roles we need to add all the roles under service account roles tab (Client -> select client name -> service account role tab)
     ![Example Image](https://raw.githubusercontent.com/hrushipn/demoFiles/main/rawReadme/service-account-roles.png)

6. **Create a User**
   - Navigate to the **Users** section and click **Add User**.
   - Enter the username (e.g., `rushi`), email, and other details.
     ![Example Image](https://raw.githubusercontent.com/hrushipn/demoFiles/main/rawReadme/img_6.png)

7. **Set User Password**
   - After creating the user, go to the **Credentials** tab.
   - Set a password (e.g., `test`) and ensure to toggle the **Temporary** switch off.
     ![Example Image](https://raw.githubusercontent.com/hrushipn/demoFiles/main/rawReadme/img_7.png)

8. **Assign Roles to User**
    - Under user tab Go to the **Role Mappings** tab.
      - click on assign roles and assign the roles you want to assign to the user.
        ![Example Image](https://raw.githubusercontent.com/hrushipn/demoFiles/main/rawReadme/img_8.png)
    



### Curls for Testing APIs

#### Token API directly to the keycloak
```bash
   curl --location 'http://localhost:8080/realms/auth/protocol/openid-connect/token' \
   --header 'Content-Type: application/x-www-form-urlencoded' \
   --data-urlencode 'grant_type=password' \
   --data-urlencode 'client_id=auth-realm-client' \
   --data-urlencode 'username=rushi' \
   --data-urlencode 'password=test' \
   --data-urlencode 'client_secret=rfK4dzeJ9usq5bxFp8HzcE8Qnvts2Md0'
```
#### Login API
```bash
curl --location 'http://localhost:8082/auth/login' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmNURPNlJDaWlvanBFRVo2RWt1dDBuc01ycUVDWWdWTkFRQm5mRDNQcVNBIn0.eyJleHAiOjE3NDk0Nzc0MDIsImlhdCI6MTc0OTQ3NzEwMiwianRpIjoib25ydHJvOmU4NGEwOTY4LTNjMWQtNGRkZS04ZjUzLWI5YmFkZTIzY2RlZCIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9yZWFsbXMvYXV0aCIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI3MmZkNzMxZC0zNWM2LTRlNjktYjU2ZC04NTdjNmIzMjkxZmYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhdXRoLXJlYWxtLWNsaWVudCIsInNpZCI6ImE1NTNkYjRkLTA1MDYtNDUxNi04MTE4LTlmN2E3Y2Y5MDhiMiIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1hdXRoIiwicmVhbG1yb2xldGVzdCIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhdXRoLXJlYWxtLWNsaWVudCI6eyJyb2xlcyI6WyJ0ZXN0Il19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJSdXNoaWtlc2ggTiIsInByZWZlcnJlZF91c2VybmFtZSI6InJ1c2hpIiwiZ2l2ZW5fbmFtZSI6IlJ1c2hpa2VzaCIsImZhbWlseV9uYW1lIjoiTiIsImVtYWlsIjoicnVzaGlAZ21haWwuY29tIn0.D_GwHNul1AQC6y7FLQPelHMPPppEgd_SIQGIhA0MPDTzd3aR_SVceRC_OkB8M8KrcMa92Rv-q73JbDmMXLAJSS8B6ilC-q3uo1lxoMf96euQCL4ilH07JfTLkZZc8XG3k8UBgVoH78ixgMnPl5DPB0f017m_FbDRQQUVQ3y15ESMwG33p1H2J7ebIhydoihLFPbUO1g5dG4CxAMX-ky7lPlrbZWPp7CaDRQsusaLDB3nXYa3Q_o9tWz4oKjNgIeGx8gigWQB7Srk4hatqU6k_3N2tZkbf3XpznkFLSzdUJ8q7TG85rTPPAEM4kljqUVizsd0wNw0jNgvxeXSaSqx4A' \
--data '{
  "username": "rushi",
  "password": "test"
}' 
```
