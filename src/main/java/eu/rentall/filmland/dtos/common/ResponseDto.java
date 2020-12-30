package eu.rentall.filmland.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing the response to a user request.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 10:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {
  // TODO consider using a boolean or enum instead of a string as the design specified, that would be more clear for the consumer of the API
  private String status;

  private String message;

  public ResponseDto(String status, String messagePattern, Object... messageArgs) {
    this.status = status;
    this.message = messagePattern != null && messageArgs != null ? String.format(messagePattern, messageArgs) : messagePattern;
  }
}
