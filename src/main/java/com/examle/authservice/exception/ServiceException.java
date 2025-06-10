package com.examle.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

@Getter
public class ServiceException extends ErrorResponseException {

  public ServiceException(HttpStatus status, String errorCode, Object[] errorArguments) {
    super(status, ProblemDetail.forStatus(status), null, errorCode, errorArguments);
  }

  public ServiceException(HttpStatus status, ProblemDetail problemDetail, String errorCode, Object[] errorArguments) {
    super(status, problemDetail, null, errorCode, errorArguments);
  }

  public ServiceException(HttpStatus status, String errorCode) {
    super(status, ProblemDetail.forStatus(status), null, errorCode, null);
  }


}
