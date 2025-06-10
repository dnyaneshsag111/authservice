package com.examle.authservice.controller;

import com.examle.authservice.exception.UserException;
import com.examle.authservice.properties.KeycloakProperties;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

  Keycloak keycloak;
  KeycloakProperties keycloakProperties;

  RealmResource realmResource = null;
  UsersResource usersResource = null;

  public UserController(Keycloak keycloak, KeycloakProperties keycloakProperties) {
    this.keycloak = keycloak;
    this.keycloakProperties = keycloakProperties;
    this.realmResource = keycloak.realm(keycloakProperties.getRealm());
    this.usersResource = realmResource.users();
  }
  // Get users
  public UsersResource getUsersResource() {
    usersResource = realmResource.users();
    return usersResource;
  }

  // Get realm
  public RealmResource getRealmResource()  {
    realmResource = keycloak.realm(keycloakProperties.getRealm());
    return realmResource;
  }


  @PostMapping("/createUser")
  public ResponseEntity<Object> createUser(@RequestBody UserRepresentation userRepresentation,
      @RequestBody CredentialRepresentation credentialRepresentation) {
    if (credentialRepresentation != null) {
      credentialRepresentation.setTemporary(false);
      credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
      userRepresentation.setCredentials(List.of(credentialRepresentation));
    }
    Response response = usersResource.create(userRepresentation);
    if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
      return ResponseEntity.ok("User created successfully");
    } else {
      log.error("Failed to create user: {}", response.getStatusInfo().getReasonPhrase());
      throw new UserException("Failed to create user");
    }
  }

    @GetMapping("/searchByUsername")
  public ResponseEntity<Object> searchByUsername(@RequestParam String username, @RequestParam(required = false) boolean exact) {
    return ResponseEntity.ok(usersResource.searchByUsername(username, exact));
  }

  @GetMapping("/findUserByUUID")
  public ResponseEntity<UserRepresentation> findUserByUUID(@RequestParam String uuid) {
    return ResponseEntity.ok(
        Optional.ofNullable(usersResource.get(uuid))
            .map(UserResource::toRepresentation)
            .orElseThrow(() -> new UserException("User not found!"))
    );
  }

  @PostMapping("/updateUserWithCredentials")
  public ResponseEntity<UserRepresentation> updateUserWithCredentials(@RequestBody UserRepresentation userToUpdate,
      @RequestBody CredentialRepresentation credentialRepresentation) {
    String userId = (userToUpdate.getId() == null || userToUpdate.getId().isEmpty())
        ? usersResource.search(userToUpdate.getUsername()).get(0).getId()
        : userToUpdate.getId();
    UserResource userResource = realmResource.users().get(userId);
    UserRepresentation userBeforeUpdate = userResource.toRepresentation();
    if (credentialRepresentation != null) {
      credentialRepresentation.setTemporary(false);
      credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
      userBeforeUpdate.setCredentials(List.of(credentialRepresentation));
    }
    BeanUtils.copyProperties(userToUpdate, userBeforeUpdate);
    userResource.update(userBeforeUpdate);
    log.info("User updated with basic infos!");
    return ResponseEntity.ok(findUserByUUID(userId).getBody());
  }

  @PostMapping("/addRealmRolesToUser")
  public ResponseEntity<Void> addRealmRolesToUser(@RequestParam String uuid, @RequestBody List<String> roles) {
    List<RoleRepresentation> rolesToAdd = roles.stream()
        .map(role -> getRealmResource().roles().get(role).toRepresentation())
        .toList();
    getUsersResource().get(uuid).roles().realmLevel().add(rolesToAdd);
    log.info("Realm roles added to user: {}", roles);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/addClientRolesToUser")
  public ResponseEntity<Void> addClientRolesToUser(@RequestParam String uuid,
      @RequestParam String clientId,
      @RequestBody List<String> roles) {
    if (!isClientAppExists(clientId)) {
      throw new UserException(HttpStatus.NOT_FOUND, "Client with ID {0} does not exist", new Object[]{clientId});
    }
    List<RoleRepresentation> rolesToAdd = roles.stream()
        .map(role -> getRealmResource().clients().get(clientId).roles().get(role).toRepresentation())
        .toList();
    getUsersResource().get(uuid).roles().clientLevel(clientId).add(rolesToAdd);
    log.info("Client roles added to user: {}", roles);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/deleteUserByUUID")
  public ResponseEntity<Boolean> deleteUserByUUID(@RequestParam String uuid) {
    Response.StatusType status = usersResource.delete(uuid).getStatusInfo();
    if (status.getStatusCode() == Response.Status.NO_CONTENT.getStatusCode()) {
      log.info("User deleted successfully!");
      return ResponseEntity.ok(true);
    } else {
      log.error("Failed to delete user: {}", status.getReasonPhrase());
      return ResponseEntity.ok(false);
    }
  }

  @PostMapping("/addRealmRoles")
  public ResponseEntity<Boolean> addRealmRoles(@RequestBody List<String> roles) {
    List<String> existingRoles = getRealmResource().roles().list().stream()
        .map(RoleRepresentation::getName)
        .toList();
    roles.stream()
        .filter(role -> !existingRoles.contains(role))
        .map(role -> {
          RoleRepresentation rp = new RoleRepresentation();
          rp.setName(role);
          return rp;
        })
        .forEach(rp -> getRealmResource().roles().create(rp));
    log.info("Realm roles added: {}", roles);
    return ResponseEntity.ok(true);
  }

  @PostMapping("/addClientRoles")
  public ResponseEntity<Boolean> addClientRoles(@RequestParam String clientId, @RequestBody List<String> roles) {
    if (!isClientAppExists(clientId)) {
      throw new UserException(HttpStatus.NOT_FOUND, "Client with ID {0} does not exist", new Object[]{clientId});
    }
    List<String> existingRoles = getRealmResource().clients().get(clientId).roles().list().stream()
        .map(RoleRepresentation::getName)
        .toList();
    roles.stream()
        .filter(role -> !existingRoles.contains(role))
        .map(role -> {
          RoleRepresentation rp = new RoleRepresentation();
          rp.setName(role);
          return rp;
        })
        .forEach(rp -> getRealmResource().clients().get(clientId).roles().create(rp));
    log.info("Client roles added: {}", roles);
    return ResponseEntity.ok(true);
  }

  /**
   * Check if ClientApp exist
   *
   * @param clientId ClientApp UUID
   * @return
   */
  private boolean isClientAppExists(String clientId) {
    return Optional.ofNullable(getRealmResource().clients().get(clientId))
        .map(client -> true)
        .orElseGet(() -> {
          log.error("Client with UUID {} not found!", clientId);
          return false;
        });
  }


}
