package eu.rentall.filmland.dtos.authentication;

import lombok.Data;

/**
 * Data transfer object holding the credentials presented for a login attempt.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 10:02
 */
@Data
public class UserCredentialDto {
  private String email;
  private String password;
}
