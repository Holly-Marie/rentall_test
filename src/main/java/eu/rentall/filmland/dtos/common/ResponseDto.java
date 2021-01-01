package eu.rentall.filmland.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.IllegalFormatException;

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

  /**
   * @param status         the status/result of the request
   * @param messagePattern a message describing the outcome of the request
   * @param messageArgs    If a format string contains an illegal syntax, a format specifier that is incompatible with the given arguments,
   *                       insufficient arguments given the format string, or other illegal conditions.
   *                       For specification of all possible formatting errors, see the Details section of the formatter class specification.
   */
  public ResponseDto(String status, String messagePattern, Object... messageArgs) throws IllegalFormatException {
    this.status = status;
    this.message = messagePattern != null && messageArgs != null ? String.format(messagePattern, messageArgs) : messagePattern;
  }
}
