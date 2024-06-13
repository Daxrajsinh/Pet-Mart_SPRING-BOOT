package com.example.demo.controller;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.example.demo.dao.AccountDAO;
import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Account;
import com.example.demo.entity.Product;
import com.example.demo.form.ProductForm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Transactional
public class AdminController {

   @Autowired
   private ProductDAO productDAO;
   
   @Autowired
   private AccountDAO accountDAO;

   @Autowired
   private BCryptPasswordEncoder passwordEncoder;
   // GET: Show Login Page
   @GetMapping({ "/admin/login" })
   public String login(Model model) {

      return "login";
   }
   @GetMapping({ "/admin/signup" })
   public String signup(Model model) {
      return "signup";
   }
   
   @PostMapping("/admin/account")
   public String saveAccount(Model model,
                             @RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam("confirmPassword") String confirmPassword,
                             @RequestParam("role") String role,
                             final RedirectAttributes redirectAttributes) {

       if (!password.equals(confirmPassword)) {
           model.addAttribute("errorMessage", "Passwords do not match.");
           // Show signup form.
           return "signup";
       }

       try {
           Account existingAccount = accountDAO.findAccount(username);
           if (existingAccount != null) {
               model.addAttribute("errorMessage", "Username already exists. Please choose another one.");
               // Show signup form with error message
               return "signup";
           }

           Account account = new Account();
           account.setUserName(username);
           account.setEncrytedPassword(passwordEncoder.encode(password));
           account.setUserRole(role);
           accountDAO.saveAccount(account);
       } catch (Exception e) {
           Throwable rootCause = ExceptionUtils.getRootCause(e);
           String message = rootCause.getMessage();
           model.addAttribute("errorMessage", message);
           // Show signup form with error message
           return "signup";
       }

       return "redirect:/admin/login";
   }


   
   // GET: Show product.
   @GetMapping({ "/admin/product" })
   public String product(Model model, @RequestParam(value = "code", defaultValue = "") String code) {
      ProductForm productForm = null;

      if (code != null && code.length() > 0) {
         Product product = productDAO.findProduct(code);
         if (product != null) {
            productForm = new ProductForm(product);
         }
      }
      if (productForm == null) {
         productForm = new ProductForm();
         productForm.setNewProduct(true);
      }
      model.addAttribute("productForm", productForm);
      return "product";
   }

   // POST: Save product
   @PostMapping({ "/admin/product" })
   public String productSave(Model model, //
         @ModelAttribute("productForm") @Validated ProductForm productForm, //
         BindingResult result, //
         final RedirectAttributes redirectAttributes) {

      if (result.hasErrors()) {
         return "product";
      }
      try {
         productDAO.save(productForm);
      } catch (Exception e) {
         Throwable rootCause = ExceptionUtils.getRootCause(e);
         String message = rootCause.getMessage();
         model.addAttribute("errorMessage", message);
         // Show product form.
         return "product";
      }

      return "redirect:/productList";
   }
   
   
   @GetMapping({ "/admin/product/delete" })
   public String deleteProduct(Model model, @ModelAttribute("productForm") ProductForm productForm,
           final RedirectAttributes redirectAttributes) {
       try {
           productDAO.delete(productForm);
           redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
       } catch (Exception e) {
           Throwable rootCause = ExceptionUtils.getRootCause(e);
           String message = rootCause.getMessage();
           model.addAttribute("errorMessage", message);
       }

       return "redirect:/productList";
   }
}
