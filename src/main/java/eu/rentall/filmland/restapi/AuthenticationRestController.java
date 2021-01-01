package eu.rentall.filmland.restapi;

import eu.rentall.filmland.dtos.authentication.LoginResponseDto;
import eu.rentall.filmland.dtos.authentication.UserCredentialDto;
import eu.rentall.filmland.dtos.common.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for endpoints concerning authentication.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 10:06
 */
@Slf4j
@RequestMapping("/authentication")
@RestController
public class AuthenticationRestController {

  /**
   * Check if the login is valid.
   * @param userCredential the users credentials
   * @return result of the login attempt
   */
  @PostMapping("/login")
  public ResponseEntity<ResponseDto> login(@Valid @RequestBody UserCredentialDto userCredential) throws IOException {
    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost("http://localhost:8081/auth/realms/filmland/protocol/openid-connect/token");

    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("grant_type", "password"));
    params.add(new BasicNameValuePair("client_id", "filmland-backend"));
    params.add(new BasicNameValuePair("client_secret", "4e750b1f-87cb-4711-864f-060426a1861b")); // TODO move client secret to secure storage
    params.add(new BasicNameValuePair("username", userCredential.getEmail()));
    params.add(new BasicNameValuePair("password", userCredential.getPassword()));
    httpPost.setEntity(new UrlEncodedFormEntity(params));

    CloseableHttpResponse response = client.execute(httpPost);
    if(response.getStatusLine().getStatusCode() == 200) {
      byte[] responseBytes = response.getEntity().getContent().readAllBytes();
      String body = new String(responseBytes, StandardCharsets.UTF_8);
      log.info("user with email address '{}' logged in successfully", userCredential.getEmail());
      return new ResponseEntity<>(new LoginResponseDto(body, "Login successful", "User is successfully logged in."), HttpStatus.OK);
    } else {
      log.warn("user with email address '{}' made invalid login attempt", userCredential.getEmail());
      // don't reveal if the user name was not found or the password is incorrect, could give attackers a faster way in
      return new ResponseEntity<>(new ResponseDto("Login failed", "User credentials incorrect. Login refused!"), HttpStatus.UNAUTHORIZED);
    }
  }
}
