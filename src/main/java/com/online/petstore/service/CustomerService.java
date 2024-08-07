package com.online.petstore.service;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.online.petstore.model.CustomerInfo;

@Component
public class CustomerService implements Validator {

   private EmailValidator emailValidator = EmailValidator.getInstance();

   // This validator only checks for the CustomerForm.
   @Override
   public boolean supports(Class<?> clazz) {
      return clazz == CustomerInfo.class;
   }

   @Override
   public void validate(Object target, Errors errors) {
      CustomerInfo custInfo = (CustomerInfo) target;

      // Check the fields of CustomerForm.
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotEmpty.customerForm.name");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "NotEmpty.customerForm.email");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "address", "NotEmpty.customerForm.address");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phone", "NotEmpty.customerForm.phone");

      if (!emailValidator.isValid(custInfo.getEmail())) {
         errors.rejectValue("email", "Pattern.customerForm.email");
      }
   }

}
