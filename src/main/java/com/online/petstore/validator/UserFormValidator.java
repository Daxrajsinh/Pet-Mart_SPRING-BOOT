package com.online.petstore.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.online.petstore.dao.AccountDAO;
import com.online.petstore.form.UserForm;

@Component
public class UserFormValidator implements Validator {

    @Autowired
    private AccountDAO accountDAO;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserForm userForm = (UserForm) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "NotEmpty.userForm.username");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty.userForm.password");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", "NotEmpty.userForm.confirmPassword");

        // Additional validations
        if (userForm.getPassword().length() < 6) {
            errors.rejectValue("password", "Size.userForm.password");
        }

        if (userForm.getUsername().length() < 6) {
            errors.rejectValue("username", "Size.userForm.username");
        }

        if (!userForm.getPassword().equals(userForm.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "Match.userForm.confirmPassword");
        }

        String username = userForm.getUsername();
        if (accountDAO.findAccount(username) != null) {
            errors.rejectValue("username", "Duplicate.userForm.username");
        }
    }
}
