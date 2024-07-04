package com.online.petstore.controller;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.online.petstore.dao.AccountDAO;
import com.online.petstore.dao.OrderDAO;
import com.online.petstore.dao.ProductDAO;
import com.online.petstore.entity.Account;
import com.online.petstore.entity.Product;
import com.online.petstore.form.ProductForm;
import com.online.petstore.form.UserForm;
import com.online.petstore.model.OrderDetailInfo;
import com.online.petstore.model.OrderInfo;
import com.online.petstore.validator.ProductFormValidator;
import com.online.petstore.validator.UserFormValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Transactional
public class AdminController {
	
	@Autowired
	private OrderDAO orderDAO;
	
   @Autowired
   private ProductDAO productDAO;
   
   @Autowired
   private AccountDAO accountDAO;
   
   @Autowired
   private ProductFormValidator productFormValidator;
   
   @Autowired
   private UserFormValidator userFormValidator;
   
   @InitBinder
   public void myInitBinder(WebDataBinder dataBinder) {
	   Object target = dataBinder.getTarget();
       if (target == null) {
           return;
       }
       System.out.println("Target=" + target);

       if (target.getClass() == ProductForm.class) {
           dataBinder.setValidator(productFormValidator);
       } else if (target.getClass() == UserForm.class) {
           dataBinder.setValidator(userFormValidator);
       }
   }
   
   @Autowired
   private BCryptPasswordEncoder passwordEncoder;
   // GET: Show Login Page
   @GetMapping({ "/admin/login" })
   public String login(Model model) {

      return "login";
   }
   @GetMapping({ "/admin/signup" })
   public String signup(Model model) {
	   model.addAttribute("userForm", new UserForm());
      return "signup";
   }
   
   @GetMapping({ "/admin/accountInfo" })
   public String accountInfo(Model model) {

      UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      System.out.println(userDetails.getPassword());
      System.out.println(userDetails.getUsername());
      System.out.println(userDetails.isEnabled());

      model.addAttribute("userDetails", userDetails);
      return "accountInfo";
   }
   
   @GetMapping({ "/admin/orderList" })
   public String orderList(Model model, //
         @RequestParam(value = "page", defaultValue = "1") String pageStr) {
//      int page = 1;
//      try {
//         page = Integer.parseInt(pageStr);
//      } catch (Exception e) {
//      }

//      PaginationResult<OrderInfo> paginationResult //
//            = orderDAO.listOrderInfo(page, MAX_RESULT, MAX_NAVIGATION_PAGE);

//      model.addAttribute("paginationResult", paginationResult);
      return "orderList";
   }

   @GetMapping({ "/admin/order" })
   public String orderView(Model model, @RequestParam("orderId") String orderId) {
      OrderInfo orderInfo = null;
      if (orderId != null) {
         orderInfo = this.orderDAO.getOrderInfo(orderId);
      }
      if (orderInfo == null) {
         return "redirect:/admin/orderList";
      }
      List<OrderDetailInfo> details = this.orderDAO.listOrderDetailInfos(orderId);
      orderInfo.setDetails(details);

      model.addAttribute("orderInfo", orderInfo);

      return "order";
   }
   
   @PostMapping("/admin/account")
   public String saveAccount(Model model,
                             @ModelAttribute("userForm") @Validated UserForm userForm,
                             BindingResult result,
                             final RedirectAttributes redirectAttributes) {

	   if (result.hasErrors()) {
           return "signup"; // Return signup page with validation errors
       }

       try {
           Account account = new Account();
           account.setUserName(userForm.getUsername());
           account.setEncrytedPassword(passwordEncoder.encode(userForm.getPassword()));
           account.setUserRole("ROLE_USER"); // Default role
           accountDAO.saveAccount(account);
       } catch (Exception e) {
           String message = "Error occurred during account creation.";
           model.addAttribute("errorMessage", message);
           return "signup"; // Return signup page with error message
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
