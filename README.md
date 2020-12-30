# Assessment test Rent-All

## Prerequisite

- docker and docker-compose need to be available (to run the Keycloak server)

## Start up
- in the project folder where the docker-compose file resides run:
    - docker-compose up -d
    - the Keycloak admin console can be reached at http://localhost:8081/auth
      - user: admin
      - password: admin
    - a realm 'filmland' with a client 'filmland-backend' is set up, which has two users
    - roles could be setup and assigned
    - Identity Providers could be set up for social logins via Google, Facebook and so on
- build and start the backend server

## Use
- a successful login attempt returns a field: `token`
    - this is the JSON string containing the response from Keycloak (usually the login would be addressed directly to keycloak)
    - it contains a field: `access_token`
    - this token needs to be presented as a Bearer Token with each request to other REST endpoints
    
## Notes
- in this limited project users would need to be created at the backend somehow
- in a real application a Keycloak UserFederationProvider would need to be implemented to create backend users when users register with Keycloak