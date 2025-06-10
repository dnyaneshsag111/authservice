package com.examle.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

@Getter
public class UserException extends ErrorResponseException {

  public UserException(HttpStatus status, String errorCode, Object[] errorArguments) {
    super(status, ProblemDetail.forStatus(status), null, errorCode, errorArguments);
  }

  public UserException(HttpStatus status, ProblemDetail problemDetail, String errorCode, Object[] errorArguments) {
    super(status, problemDetail, null, errorCode, errorArguments);
  }

  public UserException(HttpStatus status, String errorCode) {
    super(status, ProblemDetail.forStatus(status), null, errorCode, null);
  }

  public UserException(String errorCode) {
    super(HttpStatus.BAD_REQUEST, ProblemDetail.forStatus(HttpStatus.BAD_REQUEST), null, errorCode, null);
  }


}
