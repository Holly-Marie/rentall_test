package eu.rentall.filmland.dtos.authentication;

import eu.rentall.filmland.dtos.common.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data transfer object representing the response to a login request.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 14:32
 */
@Data
@AllArgsConstructor
public class LoginResponseDto extends ResponseDto {
  private Object token;

  public LoginResponseDto(Object token, String status, String messagePattern) {
    super(status, messagePattern);
    this.token = token;
  }

  public LoginResponseDto(Object token, String status, String messagePattern, Object... messageArgs) {
    super(status, messagePattern, messageArgs);
    this.token = token;
  }
}
